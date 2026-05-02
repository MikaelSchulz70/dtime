package se.dtime.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import static org.assertj.core.api.Assertions.assertThat;

class AuthentikEndSessionUriResolverTest {

    private static ClientRegistration registrationWithAuthorizationUri(String authorizationUri) {
        return ClientRegistration.withRegistrationId("authentik")
                .clientId("id")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/cb")
                .authorizationUri(authorizationUri)
                .tokenUri("http://localhost/token")
                .jwkSetUri("http://localhost/jwks")
                .userInfoUri("http://localhost/ui")
                .scope("openid")
                .build();
    }

    @Test
    void resolve_prefersConfiguredUri() {
        String configured = "https://idp.example/custom/logout";
        assertThat(AuthentikEndSessionUriResolver.resolve(configured,
                registrationWithAuthorizationUri("https://idp.example/application/o/x/authorize/")))
                .isEqualTo(configured);
    }

    @Test
    void derive_sluggedAuthentikAuthorizePath() {
        assertThat(AuthentikEndSessionUriResolver.deriveFromAuthorizationUri(
                "http://localhost:9000/application/o/dtime/authorize/"))
                .isEqualTo("http://localhost:9000/application/o/dtime/end-session/");
    }

    @Test
    void derive_genericAuthorizeSegment() {
        assertThat(AuthentikEndSessionUriResolver.deriveFromAuthorizationUri(
                "https://idp/o/foo/authorize/"))
                .isEqualTo("https://idp/o/foo/end-session/");
    }

    @Test
    void resolve_derivesFromRegistrationWhenPropertyBlank() {
        assertThat(AuthentikEndSessionUriResolver.resolve("",
                registrationWithAuthorizationUri("http://host/application/o/myapp/authorize/")))
                .isEqualTo("http://host/application/o/myapp/end-session/");
    }

    @Test
    void resolve_returnsNullWhenNothingToUse() {
        assertThat(AuthentikEndSessionUriResolver.resolve("", null)).isNull();
    }

    @Test
    void resolveFullInvalidationFlowUri_prefersConfiguredFlowUri() {
        String configured = "https://idp.example/if/flow/custom/";
        assertThat(AuthentikEndSessionUriResolver.resolveFullInvalidationFlowUri(
                configured,
                "https://ignored.example/application/o/foo/end-session/",
                registrationWithAuthorizationUri("https://ignored.example/application/o/foo/authorize/")))
                .isEqualTo(configured);
    }

    @Test
    void resolveFullInvalidationFlowUri_derivesFromConfiguredEndSessionUri() {
        assertThat(AuthentikEndSessionUriResolver.resolveFullInvalidationFlowUri(
                "",
                "https://idp.example/application/o/dtime/end-session/",
                null))
                .isNull();
    }

    @Test
    void resolveFullInvalidationFlowUri_derivesFromAuthorizationUri() {
        assertThat(AuthentikEndSessionUriResolver.resolveFullInvalidationFlowUri(
                "",
                "",
                registrationWithAuthorizationUri("http://localhost:9000/application/o/dtime/authorize/")))
                .isNull();
    }
}
