package se.dtime.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth.authentik.machine-jwt")
public class AuthentikMachineJwtProperties implements EnvironmentAware {

    private static final String AUTHORIZED_CLIENT_IDS_ENV = "OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS";
    /** Dev convenience: same Authentik client as dtime-mcp when allowlist env/YAML are empty. */
    private static final String MCP_OAUTH_CLIENT_ID_ENV = "MCP_OAUTH_CLIENT_ID";

    private Environment environment;

    /**
     * When true and a JWKS URI is configured, Bearer JWT authentication is accepted alongside interactive login.
     */
    private boolean enabled;

    /**
     * JWKS for validating machine access tokens. When blank, falls back to {@code oauth.authentik.jwk-set-uri}.
     * Use a dedicated URI when the MCP Authentik provider slug differs from the browser app (e.g. {@code dtmcp} vs {@code dtime}).
     */
    private String jwkSetUri = "";

    /**
     * Access tokens for these OAuth client identifiers ({@code client_id}, {@code azp}, or {@code sub} claim)
     * receive {@code ROLE_ADMIN} and {@code ROLE_USER} for read-only machine access (MCP).
     */
    private List<String> authorizedClientIds = new ArrayList<>();

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Merges {@link #AUTHORIZED_CLIENT_IDS_ENV} (comma-separated) after YAML binding, and splits any list
     * entry that contains commas so a single-line env var works reliably.
     */
    @PostConstruct
    void normalizeAuthorizedClientIds() {
        if (environment == null) {
            return;
        }
        String fromEnv = environment.getProperty(AUTHORIZED_CLIENT_IDS_ENV);
        if (fromEnv != null && !fromEnv.isBlank()) {
            authorizedClientIds = splitCsv(fromEnv);
            return;
        }
        if (authorizedClientIds == null) {
            authorizedClientIds = new ArrayList<>();
            return;
        }
        List<String> flattened = new ArrayList<>();
        for (String entry : authorizedClientIds) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            if (entry.contains(",")) {
                flattened.addAll(splitCsv(entry));
            } else {
                flattened.add(entry.trim());
            }
        }
        authorizedClientIds = flattened;
        if (!authorizedClientIds.isEmpty()) {
            return;
        }
        String mcpClientId = environment.getProperty(MCP_OAUTH_CLIENT_ID_ENV);
        if (mcpClientId != null && !mcpClientId.isBlank()) {
            authorizedClientIds = List.of(mcpClientId.trim());
        }
    }

    private static List<String> splitCsv(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
