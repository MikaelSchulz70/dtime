package se.dtime.mcp.oauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import se.dtime.mcp.config.BackendApiConfig;
import se.dtime.mcp.model.DtimeApiException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackendAccessTokenSupplierTest {

    @Mock
    private RestTemplate restTemplate;

    private BackendApiConfig config;
    private BackendAccessTokenSupplier supplier;

    @BeforeEach
    void setUp() {
        config = new BackendApiConfig();
        config.setUrl("https://localhost:8443");
        config.setTokenUri("http://localhost:9000/application/o/token/");
        config.setClientId("mcp-client");
        config.setClientSecret("secret");
        supplier = new BackendAccessTokenSupplier(config, restTemplate);
    }

    @Test
    void getAccessTokenValue_fetchesAndCachesToken() {
        when(restTemplate.exchange(
                eq("http://localhost:9000/application/o/token/"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenReturn(ResponseEntity.ok(Map.of("access_token", "token-1", "expires_in", 3600)));

        assertThat(supplier.getAccessTokenValue()).isEqualTo("token-1");
        assertThat(supplier.getAccessTokenValue()).isEqualTo("token-1");

        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:9000/application/o/token/"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());
    }

    @Test
    void invalidate_forcesNewTokenFetch() {
        when(restTemplate.exchange(
                eq("http://localhost:9000/application/o/token/"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenReturn(ResponseEntity.ok(Map.of("access_token", "token-1", "expires_in", 3600)))
                .thenReturn(ResponseEntity.ok(Map.of("access_token", "token-2", "expires_in", 3600)));

        assertThat(supplier.getAccessTokenValue()).isEqualTo("token-1");
        supplier.invalidate();
        assertThat(supplier.getAccessTokenValue()).isEqualTo("token-2");
    }

    @Test
    void getAccessTokenValue_missingClientId_throws() {
        config.setClientId("");

        assertThatThrownBy(() -> supplier.getAccessTokenValue())
                .isInstanceOf(DtimeApiException.class)
                .hasMessageContaining("client-id");
    }

    @Test
    void getAccessTokenValue_missingAccessTokenInResponse_throws() {
        when(restTemplate.exchange(
                eq("http://localhost:9000/application/o/token/"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenReturn(ResponseEntity.ok(Map.of("token_type", "Bearer")));

        assertThatThrownBy(() -> supplier.getAccessTokenValue())
                .isInstanceOf(DtimeApiException.class)
                .hasMessageContaining("access_token");
    }
}
