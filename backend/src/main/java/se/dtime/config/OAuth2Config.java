package se.dtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
@ConditionalOnProperty(name = "oauth.authentik.enabled", havingValue = "true")
public class OAuth2Config {

    @Value("${oauth.authentik.client-id:}")
    private String authentikClientId;

    @Value("${oauth.authentik.client-secret:}")
    private String authentikClientSecret;

    @Value("${oauth.authentik.authorization-uri:}")
    private String authorizationUri;

    @Value("${oauth.authentik.token-uri:}")
    private String tokenUri;

    @Value("${oauth.authentik.user-info-uri:}")
    private String userInfoUri;

    @Value("${oauth.authentik.jwk-set-uri:}")
    private String jwkSetUri;

    @Bean
    @ConditionalOnProperty(name = "oauth.authentik.enabled", havingValue = "true")
    public ClientRegistrationRepository clientRegistrationRepository() {
        if (authentikClientId == null || authentikClientId.isEmpty()
                || authentikClientSecret == null || authentikClientSecret.isEmpty()) {
            throw new IllegalStateException("Authentik OAuth is enabled but client-id or client-secret is not provided");
        }

        ClientRegistration authentikClientRegistration = ClientRegistration.withRegistrationId("authentik")
                .clientId(authentikClientId)
                .clientSecret(authentikClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "email", "profile")
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .jwkSetUri(jwkSetUri)
                .userInfoUri(userInfoUri)
                .userNameAttributeName("sub")
                .clientName("Authentik")
                .build();

        return new InMemoryClientRegistrationRepository(authentikClientRegistration);
    }
}