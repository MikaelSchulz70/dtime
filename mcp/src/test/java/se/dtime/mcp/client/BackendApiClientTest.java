package se.dtime.mcp.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import se.dtime.mcp.config.BackendApiConfig;
import se.dtime.mcp.oauth.BackendAccessTokenSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackendApiClientTest {

    @Mock
    private BackendAccessTokenSupplier accessTokenSupplier;

    @Mock
    private RestTemplate restTemplate;

    private BackendApiClient client;

    @BeforeEach
    void setUp() {
        BackendApiConfig config = new BackendApiConfig();
        config.setUrl("https://localhost:8443");
        client = new BackendApiClient(config, restTemplate, accessTokenSupplier);
    }

    @Test
    void canObtainAccessToken_reusesCachedTokenWithoutInvalidate() {
        when(accessTokenSupplier.getAccessTokenValue()).thenReturn("cached-token");

        assertThat(client.canObtainAccessToken()).isTrue();

        verify(accessTokenSupplier).getAccessTokenValue();
        verify(accessTokenSupplier, never()).invalidate();
    }
}
