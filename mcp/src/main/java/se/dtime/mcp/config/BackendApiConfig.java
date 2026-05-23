package se.dtime.mcp.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MCP → dtime REST base URL and Authentik client-credentials settings.
 */
@Getter
@Component
@ConfigurationProperties(prefix = "mcp.backend")
public class BackendApiConfig {

    private String url;
    private String tokenUri;
    private String clientId;
    private String clientSecret;
    /**
     * Optional space-separated scopes (e.g. "openid profiles"), or blank.
     */
    private String scope = "";

    private long connectTimeoutMs = 5000;
    private long readTimeoutMs = 30000;

    /**
     * When true and backend URL is HTTPS, RestTemplate trusts any server certificate (dev/docker self-signed).
     * Never enable against untrusted networks.
     */
    private boolean trustInsecureSsl;

    /**
     * Run an initial connectivity probe at startup (non-fatal unless {@link #failOnUnavailable}).
     */
    private boolean validateOnStartup = true;

    /** Interval between Authentik/backend connectivity retries. */
    private long connectivityCheckIntervalMs = 30_000;

    /** Fail fast when OAuth client id/secret/token URI are missing. */
    private boolean failOnMissingOauthConfig = true;

    /**
     * When true, startup throws if Authentik or backend is unreachable (legacy fail-fast behavior).
     */
    private boolean failOnUnavailable;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setConnectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public void setReadTimeoutMs(long readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public void setTrustInsecureSsl(boolean trustInsecureSsl) {
        this.trustInsecureSsl = trustInsecureSsl;
    }

    public void setValidateOnStartup(boolean validateOnStartup) {
        this.validateOnStartup = validateOnStartup;
    }

    public void setConnectivityCheckIntervalMs(long connectivityCheckIntervalMs) {
        this.connectivityCheckIntervalMs = connectivityCheckIntervalMs;
    }

    public void setFailOnMissingOauthConfig(boolean failOnMissingOauthConfig) {
        this.failOnMissingOauthConfig = failOnMissingOauthConfig;
    }

    public void setFailOnUnavailable(boolean failOnUnavailable) {
        this.failOnUnavailable = failOnUnavailable;
    }

    public boolean isOAuthConfigPresent() {
        return isNonBlank(tokenUri) && isNonBlank(clientId) && isNonBlank(clientSecret);
    }

    private static boolean isNonBlank(String value) {
        return value != null && !value.isBlank();
    }
}
