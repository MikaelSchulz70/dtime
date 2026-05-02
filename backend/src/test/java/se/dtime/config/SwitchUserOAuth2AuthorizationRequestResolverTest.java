package se.dtime.config;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.assertj.core.api.Assertions.assertThat;

class SwitchUserOAuth2AuthorizationRequestResolverTest {

    private static InMemoryClientRegistrationRepository authentikRepository() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("authentik")
                .clientId("test-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "email")
                .authorizationUri("https://idp.example/oauth/authorize")
                .tokenUri("https://idp.example/oauth/token")
                .jwkSetUri("https://idp.example/.well-known/jwks.json")
                .userInfoUri("https://idp.example/userinfo")
                .userNameAttributeName("sub")
                .build();
        return new InMemoryClientRegistrationRepository(registration);
    }

    private static MockHttpServletRequest authorizationRequest(String query) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/authentik");
        request.setRequestURI("/oauth2/authorization/authentik");
        if (query != null && !query.isEmpty()) {
            request.setQueryString(query);
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    request.addParameter(kv[0], kv[1]);
                }
            }
        }
        return request;
    }

    @Test
    void isSwitchUserReauth_trueWhenQueryParamSet() {
        MockHttpServletRequest request = authorizationRequest("switch_user=1");
        assertThat(SwitchUserOAuth2AuthorizationRequestResolver.isSwitchUserReauth(request)).isTrue();
    }

    @Test
    void isSwitchUserReauth_trueWhenSessionFlagSet() {
        MockHttpServletRequest request = authorizationRequest("");
        HttpSession session = request.getSession(true);
        session.setAttribute(SwitchUserOAuth2AuthorizationRequestResolver.SESSION_SWITCH_USER_REAUTH, Boolean.TRUE);
        assertThat(SwitchUserOAuth2AuthorizationRequestResolver.isSwitchUserReauth(request)).isTrue();
    }

    @Test
    void isSwitchUserReauth_falseWhenNoSignal() {
        MockHttpServletRequest request = authorizationRequest("");
        assertThat(SwitchUserOAuth2AuthorizationRequestResolver.isSwitchUserReauth(request)).isFalse();
    }

    @Test
    void resolve_addsPromptAndMaxAge_whenSwitchUserQueryParam() {
        SwitchUserOAuth2AuthorizationRequestResolver resolver =
                new SwitchUserOAuth2AuthorizationRequestResolver(authentikRepository());
        MockHttpServletRequest request = authorizationRequest("switch_user=1");

        OAuth2AuthorizationRequest authRequest = resolver.resolve(request);

        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getAdditionalParameters())
                .containsEntry("prompt", "login")
                .containsEntry("max_age", "0");
        assertThat(authRequest.getAuthorizationRequestUri()).contains("prompt=login").contains("max_age=0");
    }

    @Test
    void resolve_removesSessionFlagAndAddsParams_whenSessionFlagSet() {
        SwitchUserOAuth2AuthorizationRequestResolver resolver =
                new SwitchUserOAuth2AuthorizationRequestResolver(authentikRepository());
        MockHttpServletRequest request = authorizationRequest("");
        request.getSession(true).setAttribute(SwitchUserOAuth2AuthorizationRequestResolver.SESSION_SWITCH_USER_REAUTH,
                Boolean.TRUE);

        OAuth2AuthorizationRequest authRequest = resolver.resolve(request);

        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getAdditionalParameters()).containsEntry("prompt", "login");
        assertThat(request.getSession(false).getAttribute(SwitchUserOAuth2AuthorizationRequestResolver.SESSION_SWITCH_USER_REAUTH))
                .isNull();
    }

    @Test
    void resolve_doesNotAddPrompt_whenNormalLogin() {
        SwitchUserOAuth2AuthorizationRequestResolver resolver =
                new SwitchUserOAuth2AuthorizationRequestResolver(authentikRepository());
        MockHttpServletRequest request = authorizationRequest("");

        OAuth2AuthorizationRequest authRequest = resolver.resolve(request);

        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getAdditionalParameters()).doesNotContainKey("prompt");
        assertThat(authRequest.getAuthorizationRequestUri()).doesNotContain("prompt=login");
    }

    @Test
    void resolve_withExplicitRegistrationId_addsReauthParamsWhenSwitchUser() {
        SwitchUserOAuth2AuthorizationRequestResolver resolver =
                new SwitchUserOAuth2AuthorizationRequestResolver(authentikRepository());
        MockHttpServletRequest request = authorizationRequest("switch_user=1");

        OAuth2AuthorizationRequest authRequest = resolver.resolve(request, "authentik");

        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getAdditionalParameters()).containsEntry("prompt", "login");
    }
}
