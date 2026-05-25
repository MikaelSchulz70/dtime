package se.dtime.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Prefetches JWKS from Authentik on startup so the first MCP Bearer request does not block on a cold fetch
 * when Authentik is slow (common under Docker CPU load with Ollama).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "oauth.authentik.machine-jwt", name = "jwk-warmup-on-startup", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "oauth.authentik.machine-jwt", name = "enabled", havingValue = "true")
public class MachineJwtJwkWarmupRunner implements ApplicationRunner {

    private final AuthentikMachineJwtProperties machineJwtProperties;

    @Value("${oauth.authentik.jwk-set-uri:}")
    private String defaultJwkSetUri;

    @Override
    public void run(ApplicationArguments args) {
        String jwkSetUri = resolveJwkSetUri();
        if (jwkSetUri.isBlank()) {
            return;
        }
        try {
            var restTemplate = OAuthJwkRestTemplateFactory.create(
                    machineJwtProperties.getJwkConnectTimeoutMs(),
                    machineJwtProperties.getJwkReadTimeoutMs());
            restTemplate.getForEntity(jwkSetUri, String.class);
            log.info("Machine JWT JWKS warmup OK: {}", jwkSetUri);
        } catch (Exception ex) {
            log.warn(
                    "Machine JWT JWKS warmup failed for {} — ensure Authentik is up (http://localhost:9000). "
                            + "MCP Bearer calls will fail until JWKS is reachable: {}",
                    jwkSetUri,
                    ex.getMessage());
        }
    }

    private String resolveJwkSetUri() {
        String uri = machineJwtProperties.getJwkSetUri();
        if (uri != null && !uri.isBlank()) {
            return uri.trim();
        }
        return defaultJwkSetUri != null ? defaultJwkSetUri.trim() : "";
    }
}
