package se.dtime.mcp.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.time.Duration;

/**
 * Configures {@link RestTemplate} for backend calls. When {@code mcp.backend.trust-insecure-ssl} is true
 * and the backend URL is HTTPS, uses Apache HttpClient 5 with a trust-all TLS strategy so the default
 * JVM truststore does not need the dev self-signed certificate.
 */
@Configuration
public class RestTemplateConfig {

    private final BackendApiConfig backendApiConfig;

    public RestTemplateConfig(BackendApiConfig backendApiConfig) {
        this.backendApiConfig = backendApiConfig;
    }

    @Bean
    public RestTemplate restTemplate() throws Exception {
        if (backendApiConfig.isTrustInsecureSsl()
                && backendApiConfig.getUrl() != null
                && backendApiConfig.getUrl().toLowerCase().startsWith("https://")) {
            return createTrustAllHttpsRestTemplate();
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) backendApiConfig.getConnectTimeoutMs());
        factory.setReadTimeout((int) backendApiConfig.getReadTimeoutMs());
        return new RestTemplate(factory);
    }

    private RestTemplate createTrustAllHttpsRestTemplate() throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                .build();

        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                        .setSslContext(sslContext)
                        .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build())
                .build();

        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        Duration connect = Duration.ofMillis(backendApiConfig.getConnectTimeoutMs());
        Duration read = Duration.ofMillis(backendApiConfig.getReadTimeoutMs());
        factory.setConnectionRequestTimeout(connect);
        factory.setReadTimeout(read);
        return new RestTemplate(factory);
    }
}
