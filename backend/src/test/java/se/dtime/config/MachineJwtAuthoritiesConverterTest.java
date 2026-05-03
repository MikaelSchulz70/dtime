package se.dtime.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MachineJwtAuthoritiesConverterTest {

    @Test
    void grantsRolesWhenClientIdAllowed() {
        AuthentikMachineJwtProperties props = new AuthentikMachineJwtProperties();
        props.setAuthorizedClientIds(List.of("mcp-client"));
        MachineJwtAuthoritiesConverter converter = new MachineJwtAuthoritiesConverter(props);

        Jwt jwt = jwtWithClaims(Map.of("client_id", "mcp-client"));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        assertThat(authorities).contains(MachineJwtAuthoritiesConverter.MCP_ADMIN_AUTHORITY,
                MachineJwtAuthoritiesConverter.MCP_USER_AUTHORITY);
    }

    @Test
    void noRolesWhenClientIdNotAllowed() {
        AuthentikMachineJwtProperties props = new AuthentikMachineJwtProperties();
        props.setAuthorizedClientIds(List.of("other"));
        MachineJwtAuthoritiesConverter converter = new MachineJwtAuthoritiesConverter(props);

        Jwt jwt = jwtWithClaims(Map.of("client_id", "mcp-client"));

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void noRolesWhenAuthorizedListEmpty() {
        AuthentikMachineJwtProperties props = new AuthentikMachineJwtProperties();
        props.setAuthorizedClientIds(List.of());
        MachineJwtAuthoritiesConverter converter = new MachineJwtAuthoritiesConverter(props);

        Jwt jwt = jwtWithClaims(Map.of("client_id", "mcp-client"));

        assertThat(converter.convert(jwt)).isEmpty();
    }

    private static Jwt jwtWithClaims(Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claims(c -> c.putAll(claims))
                .build();
    }
}
