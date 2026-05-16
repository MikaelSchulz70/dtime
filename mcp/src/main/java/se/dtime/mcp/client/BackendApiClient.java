package se.dtime.mcp.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import se.dtime.mcp.config.BackendApiConfig;
import se.dtime.mcp.model.DtimeApiException;
import se.dtime.mcp.oauth.BackendAccessTokenSupplier;

/**
 * Read-only REST client for dtime-backend. Sends {@code Authorization: Bearer} tokens from Authentik client_credentials.
 */
@Slf4j
@Service
public class BackendApiClient {

    private final BackendApiConfig backendApiConfig;
    private final RestTemplate restTemplate;
    private final BackendAccessTokenSupplier accessTokenSupplier;

    public BackendApiClient(
            BackendApiConfig backendApiConfig,
            RestTemplate restTemplate,
            BackendAccessTokenSupplier accessTokenSupplier) {
        this.backendApiConfig = backendApiConfig;
        this.restTemplate = restTemplate;
        this.accessTokenSupplier = accessTokenSupplier;
    }

    /** GET only — MCP tools must not mutate backend state through this client. */
    public <T> T get(String endpoint, Class<T> responseType) {
        return get(endpoint, responseType, true);
    }

    private <T> T get(String endpoint, Class<T> responseType, boolean allowTokenRetry) {
        String url = backendApiConfig.getUrl() + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessTokenSupplier.getAccessTokenValue());
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, responseType).getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            if (!allowTokenRetry) {
                throw unauthorizedException(endpoint, e);
            }
            accessTokenSupplier.invalidate();
            return get(endpoint, responseType, false);
        } catch (HttpClientErrorException.Forbidden e) {
            throw new DtimeApiException(
                    "API forbidden (403) for " + endpoint + " — Bearer token accepted but missing ROLE_ADMIN/ROLE_USER. "
                            + "Add the MCP token client id to OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS on dtime-backend "
                            + "(decode token: mcp/scripts/print-access-token-claims.sh).",
                    e);
        } catch (Exception e) {
            log.error("GET request failed for {}", endpoint, e);
            throw new DtimeApiException("API request failed: " + e.getMessage(), e);
        }
    }

    /** Obtains an Authentik access token (does not call dtime-backend). */
    public boolean canObtainAccessToken() {
        try {
            accessTokenSupplier.invalidate();
            accessTokenSupplier.getAccessTokenValue();
            return true;
        } catch (DtimeApiException e) {
            log.warn("Token acquisition check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifies Bearer access to the backend (machine JWT + allowlist). Uses a minimal admin list call.
     */
    public boolean canAccessBackendApi() {
        try {
            get("/api/users/paged?page=0&size=1&sort=id&direction=asc", Object.class);
            return true;
        } catch (DtimeApiException e) {
            log.warn("Backend API access check failed: {}", e.getMessage());
            return false;
        }
    }

    private static DtimeApiException unauthorizedException(String endpoint, HttpClientErrorException.Unauthorized cause) {
        return new DtimeApiException(
                "API unauthorized (401) for " + endpoint + " — Authentik token was sent but dtime-backend rejected it. "
                        + "On dtime-backend set OAUTH_AUTHENTIK_MACHINE_JWT_ENABLED=true and "
                        + "OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS to the MCP client's client_id/azp/sub claim "
                        + "(run mcp/scripts/print-access-token-claims.sh with MCP_OAUTH_* env). Restart backend after changing.",
                cause);
    }
}
