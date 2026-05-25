package se.dtime.config;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate for OAuth JWKS endpoints (Authentik can be slow under Docker CPU load).
 */
final class OAuthJwkRestTemplateFactory {

    private OAuthJwkRestTemplateFactory() {}

    static RestTemplate create(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }
}
