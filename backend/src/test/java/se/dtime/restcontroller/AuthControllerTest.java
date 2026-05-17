package se.dtime.restcontroller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import se.dtime.config.SwitchUserOAuth2AuthorizationRequestResolver;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @BeforeEach
    void clearSecurityContextBefore() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearSecurityContextAfter() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getOidcAuthStatus_enabledFalse_whenOidcDisabled() {
        AuthController controller = new AuthController(null);
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", false);
        ReflectionTestUtils.setField(controller, "clientRegistrationRepository", mock(ClientRegistrationRepository.class));

        assertThat(controller.getOidcAuthStatus()).containsEntry("enabled", false);
    }

    @Test
    void getOidcAuthStatus_enabledFalse_whenRepositoryMissing() {
        AuthController controller = new AuthController(null);
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", true);
        ReflectionTestUtils.setField(controller, "clientRegistrationRepository", null);

        assertThat(controller.getOidcAuthStatus()).containsEntry("enabled", false);
    }

    @Test
    void getOidcAuthStatus_enabledTrue_whenOidcEnabledAndRepositoryPresent() {
        AuthController controller = new AuthController(null);
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", true);
        ReflectionTestUtils.setField(controller, "clientRegistrationRepository", mock(ClientRegistrationRepository.class));

        assertThat(controller.getOidcAuthStatus()).containsEntry("enabled", true);
    }

    @Test
    void getOidcFailure_mapsReasonAndStatus() {
        AuthController controller = new AuthController(null);
        ResponseEntity<Map<String, Object>> response = controller.getOidcFailure("access_denied");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "oauth");
        assertThat(response.getBody()).containsEntry("reason", "access_denied");
    }

    @Test
    void getOidcFailure_defaultsReason() {
        AuthController controller = new AuthController(null);
        ResponseEntity<Map<String, Object>> response = controller.getOidcFailure(null);

        assertThat(response.getBody()).containsEntry("reason", "oauth_failure");
    }

    @Test
    void switchOidcUser_redirectsToOAuthLogin_whenOidcEnabled() throws Exception {
        TokenBasedRememberMeServices rememberMeServices = mock(TokenBasedRememberMeServices.class);
        AuthController controller = new AuthController(rememberMeServices);
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", true);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession oldSession = mock(HttpSession.class);
        HttpSession newSession = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(oldSession);
        when(request.getSession(true)).thenReturn(newSession);

        controller.switchOidcUser(request, response);

        verify(rememberMeServices).logout(eq(request), eq(response), isNull());
        verify(oldSession).invalidate();
        verify(newSession).setAttribute(SwitchUserOAuth2AuthorizationRequestResolver.SESSION_SWITCH_USER_REAUTH,
                Boolean.TRUE);
        verify(response).sendRedirect("/oauth2/authorization/authentik?switch_user=1");
    }

    @Test
    void switchOidcUser_passesCurrentAuthenticationToLogout() throws Exception {
        TokenBasedRememberMeServices rememberMeServices = mock(TokenBasedRememberMeServices.class);
        AuthController controller = new AuthController(rememberMeServices);
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", true);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "alice@example.com", "n/a", AuthorityUtils.createAuthorityList("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession oldSession = mock(HttpSession.class);
        HttpSession newSession = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(oldSession);
        when(request.getSession(true)).thenReturn(newSession);

        controller.switchOidcUser(request, response);

        verify(rememberMeServices).logout(eq(request), eq(response), eq(auth));
    }

    @Test
    void switchOidcUser_redirectsToLogin_whenOidcDisabled() throws Exception {
        TokenBasedRememberMeServices rememberMeServices = mock(TokenBasedRememberMeServices.class);
        AuthController controller = new AuthController(rememberMeServices);
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(false)).thenReturn(null);

        controller.switchOidcUser(request, response);

        verify(rememberMeServices).logout(eq(request), eq(response), isNull());
        verify(response).sendRedirect("/oauth2/authorization/authentik?switch_user=1");
    }

    @Test
    void switchOidcUser_skipsRememberMeLogoutWhenBeanMissing() throws Exception {
        AuthController controller = new AuthController(null);
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", true);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession newSession = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(null);
        when(request.getSession(true)).thenReturn(newSession);

        controller.switchOidcUser(request, response);

        verify(newSession).setAttribute(SwitchUserOAuth2AuthorizationRequestResolver.SESSION_SWITCH_USER_REAUTH,
                Boolean.TRUE);
        verify(response).sendRedirect("/oauth2/authorization/authentik?switch_user=1");
    }

    @Test
    void switchOidcUser_redirectsToAuthentikEndSession_whenIdpLogoutEnabled() throws Exception {
        TokenBasedRememberMeServices rememberMeServices = mock(TokenBasedRememberMeServices.class);
        AuthController controller = new AuthController(rememberMeServices);
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", true);
        ReflectionTestUtils.setField(controller, "switchUserIdpLogout", true);
        ReflectionTestUtils.setField(controller, "backendPublicBaseUrl", "https://localhost:8443");
        ReflectionTestUtils.setField(controller, "authentikEndSessionUri",
                "https://idp.example/application/o/dtime/end-session/");

        ClientRegistration registration = ClientRegistration.withRegistrationId("authentik")
                .clientId("the-client-id")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/cb")
                .authorizationUri("https://idp.example/application/o/dtime/authorize/")
                .tokenUri("https://idp.example/token")
                .jwkSetUri("https://idp.example/jwks")
                .userInfoUri("https://idp.example/ui")
                .scope("openid")
                .build();
        ClientRegistrationRepository repo = mock(ClientRegistrationRepository.class);
        when(repo.findByRegistrationId("authentik")).thenReturn(registration);
        ReflectionTestUtils.setField(controller, "clientRegistrationRepository", repo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession oldSession = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(oldSession);
        HttpServletResponse response = mock(HttpServletResponse.class);

        controller.switchOidcUser(request, response);

        org.mockito.ArgumentCaptor<String> redirect = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirect.capture());
        assertThat(redirect.getValue()).startsWith("https://idp.example/application/o/dtime/end-session/");
        assertThat(redirect.getValue()).contains("client_id=the-client-id");
        assertThat(redirect.getValue()).contains(
                "post_logout_redirect_uri=https://localhost:8443/api/auth/oidc/switch-user/resume");
        assertThat(redirect.getValue()).doesNotContain("next=");
        assertThat(redirect.getValue()).doesNotContain("switch_user");
    }

    @Test
    void switchOidcUser_redirectsToSpaContinueInDev_whenDevServerEnabled() throws Exception {
        TokenBasedRememberMeServices rememberMeServices = mock(TokenBasedRememberMeServices.class);
        AuthController controller = new AuthController(rememberMeServices);
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", true);
        ReflectionTestUtils.setField(controller, "switchUserIdpLogout", true);
        ReflectionTestUtils.setField(controller, "devServerEnabled", true);
        ReflectionTestUtils.setField(controller, "frontendDevServerUrl", "https://localhost:3000");
        ReflectionTestUtils.setField(controller, "backendPublicBaseUrl", "https://localhost:8443");
        ReflectionTestUtils.setField(controller, "authentikEndSessionUri",
                "https://idp.example/application/o/dtime/end-session/");

        ClientRegistration registration = ClientRegistration.withRegistrationId("authentik")
                .clientId("the-client-id")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/cb")
                .authorizationUri("https://idp.example/application/o/dtime/authorize/")
                .tokenUri("https://idp.example/token")
                .jwkSetUri("https://idp.example/jwks")
                .userInfoUri("https://idp.example/ui")
                .scope("openid")
                .build();
        ClientRegistrationRepository repo = mock(ClientRegistrationRepository.class);
        when(repo.findByRegistrationId("authentik")).thenReturn(registration);
        ReflectionTestUtils.setField(controller, "clientRegistrationRepository", repo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);

        controller.switchOidcUser(request, response);

        org.mockito.ArgumentCaptor<String> redirect = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirect.capture());
        assertThat(redirect.getValue()).contains(
                "post_logout_redirect_uri=https://localhost:8443/api/auth/oidc/switch-user/resume");
    }

    @Test
    void switchOidcUserContinue_setsFlagAndRedirectsToOAuthOnBackendBase() throws Exception {
        AuthController controller = new AuthController(null);
        ReflectionTestUtils.setField(controller, "backendPublicBaseUrl", "https://localhost:8443");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(session);
        HttpServletResponse response = mock(HttpServletResponse.class);

        controller.switchOidcUserContinue(request, response);

        verify(session).setAttribute(SwitchUserOAuth2AuthorizationRequestResolver.SESSION_SWITCH_USER_REAUTH,
                Boolean.TRUE);
        verify(response).sendRedirect("https://localhost:8443/oauth2/authorization/authentik?switch_user=1");
    }
}
