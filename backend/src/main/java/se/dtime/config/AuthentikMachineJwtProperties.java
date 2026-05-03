package se.dtime.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth.authentik.machine-jwt")
public class AuthentikMachineJwtProperties {

    /**
     * When true and {@code oauth.authentik.jwk-set-uri} is set, Bearer JWT authentication is accepted
     * alongside the existing interactive login (session OAuth2 login).
     */
    private boolean enabled;

    /**
     * Access tokens for these OAuth client identifiers ({@code client_id}, {@code azp}, or {@code sub} claim)
     * receive {@code ROLE_ADMIN} and {@code ROLE_USER} for read-only machine access (MCP).
     */
    private List<String> authorizedClientIds = new ArrayList<>();
}
