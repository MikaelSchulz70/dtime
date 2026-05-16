package se.dtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
@EnableConfigurationProperties(AuthentikMachineJwtProperties.class)
@ConditionalOnProperty(prefix = "oauth.authentik.machine-jwt", name = "enabled", havingValue = "true")
public class AuthentikMachineJwtConfiguration {

    @Bean(name = "authentikMachineJwtDecoder")
    JwtDecoder authentikMachineJwtDecoder(
            AuthentikMachineJwtProperties machineJwtProperties,
            @Value("${oauth.authentik.jwk-set-uri:}") String defaultJwkSetUri) {
        String jwkSetUri = machineJwtProperties.getJwkSetUri();
        if (jwkSetUri == null || jwkSetUri.isBlank()) {
            jwkSetUri = defaultJwkSetUri;
        }
        if (jwkSetUri == null || jwkSetUri.isBlank()) {
            throw new IllegalStateException(
                    "oauth.authentik.machine-jwt.enabled is true but no JWKS URI is set "
                            + "(configure oauth.authentik.machine-jwt.jwk-set-uri or oauth.authentik.jwk-set-uri)");
        }
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean(name = "authentikMachineJwtAuthenticationConverter")
    JwtAuthenticationConverter authentikMachineJwtAuthenticationConverter(AuthentikMachineJwtProperties props) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new MachineJwtAuthoritiesConverter(props));
        return converter;
    }
}
