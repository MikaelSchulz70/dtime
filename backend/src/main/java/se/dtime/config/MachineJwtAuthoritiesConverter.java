package se.dtime.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class MachineJwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    static final GrantedAuthority MCP_ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_ADMIN");
    static final GrantedAuthority MCP_USER_AUTHORITY = new SimpleGrantedAuthority("ROLE_USER");

    private final AuthentikMachineJwtProperties props;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Optional<String> clientId = firstNonBlankClaim(jwt, "client_id", "azp", "sub");
        if (clientId.isEmpty()) {
            log.debug("Machine JWT: no client identifier claim");
            return List.of();
        }
        List<String> allowed = props.getAuthorizedClientIds();
        if (allowed == null || allowed.isEmpty()) {
            log.warn(
                    "Machine JWT accepted but oauth.authentik.machine-jwt.authorized-client-ids is empty; no roles granted");
            return List.of();
        }
        String id = clientId.get();
        if (allowed.stream().noneMatch(cid -> cid.equals(id))) {
            log.debug("Machine JWT client '{}' is not in authorized-client-ids", id);
            return List.of();
        }
        return List.of(MCP_ADMIN_AUTHORITY, MCP_USER_AUTHORITY);
    }

    private static Optional<String> firstNonBlankClaim(Jwt jwt, String... claimNames) {
        for (String name : claimNames) {
            String v = jwt.getClaimAsString(name);
            if (v != null && !v.isBlank()) {
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }
}
