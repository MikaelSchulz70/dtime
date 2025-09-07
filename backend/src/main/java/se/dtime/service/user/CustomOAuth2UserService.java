package se.dtime.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.repository.UserRepository;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 login attempt from provider: {}", registrationId);

        if ("google".equals(registrationId)) {
            return processGoogleUser(oauth2User);
        }

        throw new OAuth2AuthenticationException(
                new OAuth2Error("unsupported_provider", "Unsupported OAuth2 provider: " + registrationId, null)
        );
    }

    private OAuth2User processGoogleUser(OAuth2User oauth2User) throws OAuth2AuthenticationException {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        log.debug("Processing Google user: {} ({})", name, email);

        if (email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_email", "Email not provided by Google", null)
            );
        }

        // Check if user exists in our database
        UserPO user = userRepository.findByEmail(email);
        if (user == null) {
            log.warn("OAuth2 login attempt for non-existing user: {}", email);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("user_not_found",
                            "User with email " + email + " not found in the system. Please contact administrator.",
                            null)
            );
        }

        // Check if user is active
        if (user.getActivationStatus() != se.dtime.model.ActivationStatus.ACTIVE) {
            log.warn("OAuth2 login attempt for inactive user: {}", email);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("user_inactive",
                            "User account is inactive. Please contact administrator.",
                            null)
            );
        }

        log.info("Successful OAuth2 login for user: {}", email);
        return oauth2User;
    }
}