package se.dtime.service.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.UserRole;
import se.dtime.repository.UserRepository;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
class CustomOAuth2UserServiceTest {

    @Test
    void processAuthentikUserClaims_ShouldExtractAndStoreStandardClaims() {
        TestRepositoryState state = new TestRepositoryState();
        CustomOAuth2UserService service = new CustomOAuth2UserService(createRepository(state), true);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "sub", "authentik-user-1",
                        "email", "user1@example.com",
                        "given_name", "Alice",
                        "family_name", "Admin",
                        "groups", List.of("admin")
                ),
                "email"
        );

        OAuth2User result = service.processAuthentikUserClaims(oauth2User);
        UserPO saved = state.lastSaved;

        assertThat(saved.getExternalId()).isEqualTo("authentik-user-1");
        assertThat(saved.getEmail()).isEqualTo("user1@example.com");
        assertThat(saved.getFirstName()).isEqualTo("Alice");
        assertThat(saved.getLastName()).isEqualTo("Admin");
        assertThat(saved.getUserRole()).isEqualTo(UserRole.ADMIN);

        assertThat(result.<Long>getAttribute("local_user_id")).isEqualTo(101L);
        assertThat(result.<String>getAttribute("local_first_name")).isEqualTo("Alice");
        assertThat(result.<String>getAttribute("local_last_name")).isEqualTo("Admin");
        assertThat(result.<String>getAttribute("local_email")).isEqualTo("user1@example.com");
        assertThat(result.getAuthorities()).extracting("authority")
                .contains("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void processAuthentikUserClaims_ShouldFallbackNameAndKeepUserRole() {
        TestRepositoryState state = new TestRepositoryState();
        CustomOAuth2UserService service = new CustomOAuth2UserService(createRepository(state), true);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "sub", "authentik-user-2",
                        "email", "user2@example.com",
                        "name", "Bob Fullname",
                        "groups", List.of("USER")
                ),
                "email"
        );

        OAuth2User result = service.processAuthentikUserClaims(oauth2User);
        UserPO saved = state.lastSaved;

        assertThat(saved.getFirstName()).isEqualTo("Bob Fullname");
        assertThat(saved.getLastName()).isEqualTo("-");
        assertThat(saved.getUserRole()).isEqualTo(UserRole.USER);
        assertThat(result.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void processAuthentikUserClaims_ShouldTreatRealmAccessAdminAsAdmin() {
        TestRepositoryState state = new TestRepositoryState();
        CustomOAuth2UserService service = new CustomOAuth2UserService(createRepository(state), true);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "sub", "authentik-user-3",
                        "email", "user3@example.com",
                        "given_name", "Realm",
                        "family_name", "Admin",
                        "realm_access", Map.of("roles", List.of("viewer", "admin"))
                ),
                "email"
        );

        OAuth2User result = service.processAuthentikUserClaims(oauth2User);

        assertThat(result.getAuthorities()).extracting("authority")
                .contains("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void processAuthentikUserClaims_ShouldTreatRolesClaimAdminAsAdmin() {
        TestRepositoryState state = new TestRepositoryState();
        CustomOAuth2UserService service = new CustomOAuth2UserService(createRepository(state), true);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "sub", "authentik-user-roles-admin",
                        "email", "roles-admin@example.com",
                        "given_name", "Roles",
                        "family_name", "Admin",
                        "roles", List.of("viewer", "admin")
                ),
                "email"
        );

        OAuth2User result = service.processAuthentikUserClaims(oauth2User);

        assertThat(result.getAuthorities()).extracting("authority")
                .contains("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void processAuthentikUserClaims_ShouldTreatGroupPathEndingInAdminAsAdmin() {
        TestRepositoryState state = new TestRepositoryState();
        CustomOAuth2UserService service = new CustomOAuth2UserService(createRepository(state), true);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "sub", "authentik-user-group-path-admin",
                        "email", "group-admin@example.com",
                        "given_name", "Group",
                        "family_name", "Admin",
                        "groups", List.of("dtime/admin")
                ),
                "email"
        );

        OAuth2User result = service.processAuthentikUserClaims(oauth2User);

        assertThat(result.getAuthorities()).extracting("authority")
                .contains("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void processAuthentikUserClaims_ShouldFailWhenSubMissing() {
        TestRepositoryState state = new TestRepositoryState();
        CustomOAuth2UserService service = new CustomOAuth2UserService(createRepository(state), true);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of("email", "user@example.com"),
                "email"
        );

        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.processAuthentikUserClaims(oauth2User)
        );

        assertThat(exception.getError().getErrorCode()).isEqualTo("missing_sub");
    }

    @Test
    void processAuthentikUserClaims_ShouldFailWhenEmailMissing() {
        TestRepositoryState state = new TestRepositoryState();
        CustomOAuth2UserService service = new CustomOAuth2UserService(createRepository(state), true);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of("sub", "authentik-user-4"),
                "sub"
        );

        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.processAuthentikUserClaims(oauth2User)
        );

        assertThat(exception.getError().getErrorCode()).isEqualTo("missing_email");
    }

    @Test
    void processAuthentikUserClaims_ShouldFailWhenRequiredRoleMissing() {
        TestRepositoryState state = new TestRepositoryState();
        CustomOAuth2UserService service = new CustomOAuth2UserService(createRepository(state), true);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "sub", "authentik-default-admin",
                        "email", "ak-admin@example.com",
                        "given_name", "Authentik",
                        "family_name", "Admin"
                ),
                "email"
        );

        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.processAuthentikUserClaims(oauth2User)
        );

        assertThat(exception.getError().getErrorCode()).isEqualTo("missing_required_role");
    }

    private UserRepository createRepository(TestRepositoryState state) {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("findByExternalId".equals(methodName)) {
                        return state.byExternalId.get((String) args[0]);
                    }
                    if ("findByEmail".equals(methodName)) {
                        return state.byEmail.get((String) args[0]);
                    }
                    if ("save".equals(methodName)) {
                        UserPO user = (UserPO) args[0];
                        if (user.getId() == null) {
                            user.setId(state.nextId.getAndIncrement());
                        }
                        state.byExternalId.put(user.getExternalId(), user);
                        state.byEmail.put(user.getEmail(), user);
                        state.lastSaved = user;
                        return user;
                    }
                    throw new UnsupportedOperationException("Method not used in test: " + methodName);
                }
        );
    }

    private static class TestRepositoryState {
        private final Map<String, UserPO> byExternalId = new HashMap<>();
        private final Map<String, UserPO> byEmail = new HashMap<>();
        private final AtomicLong nextId = new AtomicLong(101);
        private UserPO lastSaved;
    }
}
