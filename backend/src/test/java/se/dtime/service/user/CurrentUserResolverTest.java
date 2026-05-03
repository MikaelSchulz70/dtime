package se.dtime.service.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.UserRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserResolverTest {

    @InjectMocks
    private CurrentUserResolver currentUserResolver;

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolveCurrentUser_withUserExt_usesId() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserExt userExt = new UserExt("u@x.com", "pwd", authorities, 5L, "F", "L");
        SecurityContextHolder.setContext(createSecurityContext(userExt, null));

        UserPO stored = new UserPO(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(stored));

        assertEquals(5L, currentUserResolver.resolveCurrentUser().getId());
        verify(userRepository).findById(5L);
    }

    @Test
    void resolveCurrentUser_withOauth2Sub_usesExternalIdLookup() {
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "oidc-sub-123", "email", ""),
                "sub"
        );
        SecurityContextHolder.setContext(createSecurityContext(oauth2User, "oidc-sub-123"));

        UserPO user = new UserPO(42L);
        when(userRepository.findByExternalId("oidc-sub-123")).thenReturn(user);

        assertEquals(42L, currentUserResolver.resolveCurrentUser().getId());
        verify(userRepository).findByExternalId("oidc-sub-123");
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void resolveCurrentUser_withOauth2NoSub_fallsBackToAuthNameEmail() {
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", ""),
                "email"
        );
        SecurityContextHolder.setContext(createSecurityContext(oauth2User, "user@example.com"));

        UserPO user = new UserPO(7L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(user);

        assertEquals(7L, currentUserResolver.resolveCurrentUser().getId());
        verify(userRepository).findByEmail(eq("user@example.com"));
    }

    @Test
    void resolveCurrentUser_unknownPrincipal_fallsBackToUserIdOne() {
        SecurityContextHolder.setContext(createSecurityContext("anonymous", "nobody@x.com"));
        when(userRepository.findByEmail("nobody@x.com")).thenReturn(null);
        UserPO u1 = new UserPO(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u1));

        assertEquals(1L, currentUserResolver.resolveCurrentUser().getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void resolveCurrentUser_userExtMissing_throws() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserExt userExt = new UserExt("u@x.com", "pwd", authorities, 99L, "F", "L");
        SecurityContextHolder.setContext(createSecurityContext(userExt, null));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> currentUserResolver.resolveCurrentUser());
    }

    private SecurityContext createSecurityContext(Object principal, String authName) {
        return new SecurityContext() {
            @Override
            public Authentication getAuthentication() {
                return new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return null;
                    }

                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getDetails() {
                        return null;
                    }

                    @Override
                    public Object getPrincipal() {
                        return principal;
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return false;
                    }

                    @Override
                    public void setAuthenticated(boolean b) throws IllegalArgumentException {

                    }

                    @Override
                    public String getName() {
                        return authName != null ? authName : (principal instanceof UserExt u ? u.getUsername() : "");
                    }
                };
            }

            @Override
            public void setAuthentication(Authentication authentication) {

            }
        };
    }
}
