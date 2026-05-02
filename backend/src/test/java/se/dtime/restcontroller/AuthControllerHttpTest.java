package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level tests for {@link AuthController} without loading the full application context.
 */
class AuthControllerHttpTest {

    private MockMvc buildMockMvc(boolean oidcEnabled, ClientRegistrationRepository repository) {
        AuthController controller = new AuthController(mock(TokenBasedRememberMeServices.class));
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", oidcEnabled);
        ReflectionTestUtils.setField(controller, "clientRegistrationRepository", repository);
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void oidcStatus_returnsEnabledTrue() throws Exception {
        MockMvc mockMvc = buildMockMvc(true, mock(ClientRegistrationRepository.class));
        mockMvc.perform(get("/api/auth/oidc/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void oidcStatus_returnsEnabledFalseWhenPropertyOff() throws Exception {
        MockMvc mockMvc = buildMockMvc(false, mock(ClientRegistrationRepository.class));
        mockMvc.perform(get("/api/auth/oidc/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void oidcStatus_returnsEnabledFalseWhenRepositoryMissing() throws Exception {
        MockMvc mockMvc = buildMockMvc(true, null);
        mockMvc.perform(get("/api/auth/oidc/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void oidcFailure_returnsJsonBody() throws Exception {
        MockMvc mockMvc = buildMockMvc(true, mock(ClientRegistrationRepository.class));
        mockMvc.perform(get("/api/auth/oidc/failure").param("reason", "invalid_id_token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("oauth"))
                .andExpect(jsonPath("$.reason").value("invalid_id_token"));
    }

    @Test
    void oidcFailure_defaultsReason() throws Exception {
        MockMvc mockMvc = buildMockMvc(true, mock(ClientRegistrationRepository.class));
        mockMvc.perform(get("/api/auth/oidc/failure"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.reason").value("oauth_failure"));
    }

    @Test
    void switchUser_redirectsWhenOidcEnabled() throws Exception {
        MockMvc mockMvc = buildMockMvc(true, mock(ClientRegistrationRepository.class));
        mockMvc.perform(get("/api/auth/oidc/switch-user"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/oauth2/authorization/authentik"));
    }

    @Test
    void switchUser_redirectsWhenOidcDisabled() throws Exception {
        MockMvc mockMvc = buildMockMvc(false, mock(ClientRegistrationRepository.class));
        mockMvc.perform(get("/api/auth/oidc/switch-user"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/oauth2/authorization/authentik"));
    }
}
