package se.dtime.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;
import se.dtime.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final boolean requireAppRole;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   @Value("${oauth.authentik.require-app-role:true}") boolean requireAppRole) {
        this.userRepository = userRepository;
        this.requireAppRole = requireAppRole;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 login attempt from provider: {}", registrationId);

        if ("authentik".equals(registrationId)) {
            return processAuthentikUserClaims(oauth2User);
        }

        throw new OAuth2AuthenticationException(
                new OAuth2Error("unsupported_provider", "Unsupported OAuth2 provider: " + registrationId, null)
        );
    }

    OAuth2User processAuthentikUserClaims(OAuth2User oauth2User) throws OAuth2AuthenticationException {
        String sub = oauth2User.getAttribute("sub");
        String email = readEmail(oauth2User);
        String displayName = resolveDisplayName(oauth2User);

        if (sub == null || sub.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_sub", "sub claim not provided by OIDC provider", null)
            );
        }

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_email", "Email not provided by OIDC provider", null)
            );
        }
        if (requireAppRole && !containsRequiredAppRole(oauth2User)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_required_role",
                            "User is missing required Authentik role/group (USER or ADMIN).",
                            null)
            );
        }

        UserPO user = userRepository.findByExternalId(sub);
        if (user == null) {
            // First login with OIDC can match an already existing local user by email.
            user = userRepository.findByEmail(email);
        }
        LocalDateTime now = LocalDateTime.now();
        if (user == null) {
            user = new UserPO();
            user.setExternalId(sub);
            user.setActivationStatus(ActivationStatus.ACTIVE);
            user.setCreateDateTime(now);
            user.setCreatedBy(1L);
        } else if (user.getExternalId() == null || !sub.equals(user.getExternalId())) {
            // Link existing account to OIDC subject.
            user.setExternalId(sub);
        }

        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setUserRole(resolveUserRole(oauth2User));
        user.setUpdatedDateTime(now);
        user.setUpdatedBy(1L);
        userRepository.save(user);

        if (user.getActivationStatus() != ActivationStatus.ACTIVE) {
            log.warn("OAuth2 login attempt for inactive user: {}", email);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("user_inactive",
                            "User account is inactive. Please contact administrator.",
                            null)
            );
        }

        Set<GrantedAuthority> authorities = resolveAuthorities(oauth2User, user.getUserRole());
        Map<String, Object> attributes = new LinkedHashMap<>(oauth2User.getAttributes());
        attributes.put("local_user_id", user.getId());
        attributes.put("local_first_name", user.getFirstName());
        attributes.put("local_last_name", user.getLastName());
        attributes.put("local_email", user.getEmail());

        log.info("Successful OAuth2 login for user: {} (id={})", email, user.getId());
        return new DefaultOAuth2User(authorities, attributes, "email");
    }

    private String readEmail(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        String preferredUsername = oauth2User.getAttribute("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            // Some local Authentik setups don't include email in claims; keep user provisioning stable.
            return preferredUsername + "@authentik.local";
        }
        return email;
    }

    private String resolveDisplayName(OAuth2User oauth2User) {
        String name = oauth2User.getAttribute("name");
        if (name != null && !name.isBlank()) {
            return name.trim();
        }

        String preferredUsername = oauth2User.getAttribute("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername.trim();
        }

        String email = oauth2User.getAttribute("email");
        if (email != null && !email.isBlank()) {
            return email.trim();
        }

        return "Unknown";
    }

    private UserRole resolveUserRole(OAuth2User oauth2User) {
        return containsAdminRole(oauth2User) ? UserRole.ADMIN : UserRole.USER;
    }

    private Set<GrantedAuthority> resolveAuthorities(OAuth2User oauth2User, UserRole userRole) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (userRole == UserRole.ADMIN || containsAdminRole(oauth2User)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
    }

    private boolean containsAdminRole(OAuth2User oauth2User) {
        return containsRole(oauth2User.getAttribute("roles"), "admin")
                || containsRole(oauth2User.getAttribute("groups"), "admin")
                || containsRealmRole(oauth2User.getAttribute("realm_access"), "admin");
    }

    private boolean containsRequiredAppRole(OAuth2User oauth2User) {
        return containsAdminRole(oauth2User)
                || containsRole(oauth2User.getAttribute("roles"), "user")
                || containsRole(oauth2User.getAttribute("groups"), "user")
                || containsRealmRole(oauth2User.getAttribute("realm_access"), "user");
    }

    private boolean containsRealmRole(Object realmAccess, String roleToFind) {
        if (!(realmAccess instanceof Map<?, ?> realmMap)) {
            return false;
        }
        Object roles = realmMap.get("roles");
        return containsRole(roles, roleToFind);
    }

    private boolean containsRole(Object candidate, String roleToFind) {
        if (!(candidate instanceof Collection<?> collection)) {
            return false;
        }

        String expectedRole = roleToFind.toLowerCase();
        return collection.stream()
                .filter(item -> item != null)
                .map(Object::toString)
                .map(String::toLowerCase)
                .anyMatch(value -> value.equals(expectedRole) || value.endsWith("/" + expectedRole));
    }
}