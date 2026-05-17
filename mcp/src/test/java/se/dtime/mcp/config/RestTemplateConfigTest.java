package se.dtime.mcp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.main.web-application-type=servlet",
        "mcp.backend.url=https://localhost:8443",
        "mcp.backend.token-uri=https://example.test/token",
        "mcp.backend.client-id=test",
        "mcp.backend.client-secret=test",
        "mcp.backend.validate-on-startup=false",
        "mcp.backend.trust-insecure-ssl=true"
})
class RestTemplateConfigTrustInsecureTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void usesApacheClientWhenTrustInsecureSslEnabled() {
        assertThat(restTemplate.getRequestFactory()).isInstanceOf(HttpComponentsClientHttpRequestFactory.class);
    }
}

@SpringBootTest
@TestPropertySource(properties = {
        "spring.main.web-application-type=servlet",
        "mcp.backend.url=http://localhost:8080",
        "mcp.backend.token-uri=https://example.test/token",
        "mcp.backend.client-id=test",
        "mcp.backend.client-secret=test",
        "mcp.backend.validate-on-startup=false",
        "mcp.backend.trust-insecure-ssl=false"
})
class RestTemplateConfigSimpleClientTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void usesSimpleClientForPlainHttpBackend() {
        assertThat(restTemplate.getRequestFactory()).isInstanceOf(SimpleClientHttpRequestFactory.class);
    }
}
