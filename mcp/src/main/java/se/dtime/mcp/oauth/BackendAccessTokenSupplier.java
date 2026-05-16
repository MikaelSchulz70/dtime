package se.dtime.mcp.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import se.dtime.mcp.config.BackendApiConfig;
import se.dtime.mcp.model.DtimeApiException;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Fetches OAuth2 access tokens via client_credentials from Authentik (or any RFC 6749-compatible token endpoint).
 */
@Slf4j
@Component
public class BackendAccessTokenSupplier {

    private static final Duration SKEW = Duration.ofSeconds(60);

    private final BackendApiConfig config;
    private final RestTemplate restTemplate;
    private final Clock clock = Clock.systemUTC();

    private volatile String cachedToken;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    public BackendAccessTokenSupplier(BackendApiConfig config, RestTemplate restTemplate) {
        this.config = Objects.requireNonNull(config);
        this.restTemplate = Objects.requireNonNull(restTemplate);
    }

    public synchronized void invalidate() {
        cachedToken = null;
        tokenExpiresAt = Instant.EPOCH;
    }

    public synchronized String getAccessTokenValue() {
        Instant now = clock.instant();
        if (cachedToken != null && now.isBefore(tokenExpiresAt.minus(SKEW))) {
            return cachedToken;
        }
        return fetchFreshToken(now);
    }

    private String fetchFreshToken(Instant now) {
        validateConfig();

        String resolvedTokenUri = UriComponentsBuilder.fromUriString(Objects.requireNonNull(config.getTokenUri()).trim())
                .encode(StandardCharsets.UTF_8)
                .build(true)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", Objects.requireNonNull(config.getClientId()).trim());
        body.add("client_secret", Objects.requireNonNull(config.getClientSecret()));

        String scope = config.getScope();
        if (scope != null && !scope.isBlank()) {
            body.add("scope", scope.trim());
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            // Map not JsonNode: Spring Framework 7 / Jackson 3 RestTemplate cannot deserialize
            // com.fasterxml.jackson.databind.JsonNode as a response body type.
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    resolvedTokenUri,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> response = responseEntity.getBody();
            Object accessToken = response != null ? response.get("access_token") : null;
            if (accessToken == null || accessToken.toString().isBlank()) {
                throw new DtimeApiException("token response missing access_token");
            }
            String token = accessToken.toString();
            Duration ttl = Duration.ofHours(1);
            Object expiresIn = response != null ? response.get("expires_in") : null;
            if (expiresIn instanceof Number n) {
                long sec = n.longValue();
                if (sec > 0) {
                    ttl = Duration.ofSeconds(sec);
                }
            }
            cachedToken = token;
            tokenExpiresAt = now.plus(ttl.isNegative() || ttl.isZero() ? Duration.ofHours(1) : ttl);
            log.debug("Obtained new access token (expires in {})", ttl);
            return token;
        } catch (Exception e) {
            log.error("Client credentials token request failed", e);
            throw new DtimeApiException("Token request failed: " + e.getMessage(), e);
        }
    }

    private void validateConfig() {
        if (config.getTokenUri() == null || config.getTokenUri().isBlank()) {
            throw new DtimeApiException("mcp.backend.token-uri is not set");
        }
        if (config.getClientId() == null || config.getClientId().isBlank()) {
            throw new DtimeApiException("mcp.backend.client-id is not set");
        }
        if (config.getClientSecret() == null || config.getClientSecret().isBlank()) {
            throw new DtimeApiException("mcp.backend.client-secret is not set");
        }
        if (config.getUrl() == null || config.getUrl().isBlank()) {
            throw new DtimeApiException("mcp.backend.url is not set");
        }
    }
}
