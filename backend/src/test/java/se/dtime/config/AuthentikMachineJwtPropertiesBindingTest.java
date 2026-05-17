package se.dtime.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class AuthentikMachineJwtPropertiesBindingTest {

    @Configuration
    @EnableConfigurationProperties(AuthentikMachineJwtProperties.class)
    static class PropsConfig {}

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withUserConfiguration(PropsConfig.class);

    @Test
    void commaSeparatedEnvVarPopulatesAuthorizedClientIds() {
        runner.withPropertyValues("OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS=uuid-one,uuid-two")
                .run(ctx -> {
                    AuthentikMachineJwtProperties p = ctx.getBean(AuthentikMachineJwtProperties.class);
                    assertThat(p.getAuthorizedClientIds()).containsExactly("uuid-one", "uuid-two");
                });
    }

    @Test
    void yamlListWithCommaInSingleEntryIsFlattenedWhenEnvNotSet() {
        runner.withPropertyValues("oauth.authentik.machine-jwt.authorized-client-ids=a,b,c").run(ctx -> {
            AuthentikMachineJwtProperties p = ctx.getBean(AuthentikMachineJwtProperties.class);
            assertThat(p.getAuthorizedClientIds()).containsExactly("a", "b", "c");
        });
    }

    @Test
    void mcpOAuthClientIdUsedWhenAllowlistEnvAndYamlEmpty() {
        runner.withPropertyValues("MCP_OAUTH_CLIENT_ID=mcp-client-uuid").run(ctx -> {
            AuthentikMachineJwtProperties p = ctx.getBean(AuthentikMachineJwtProperties.class);
            assertThat(p.getAuthorizedClientIds()).containsExactly("mcp-client-uuid");
        });
    }

    @Test
    void explicitAllowlistEnvTakesPrecedenceOverMcpOAuthClientId() {
        runner.withPropertyValues(
                        "OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS=explicit-id",
                        "MCP_OAUTH_CLIENT_ID=mcp-client-uuid")
                .run(ctx -> {
                    AuthentikMachineJwtProperties p = ctx.getBean(AuthentikMachineJwtProperties.class);
                    assertThat(p.getAuthorizedClientIds()).containsExactly("explicit-id");
                });
    }

    @Test
    void machineJwtJwkSetUriBindsFromProperty() {
        runner.withPropertyValues(
                        "oauth.authentik.machine-jwt.jwk-set-uri=http://localhost:9000/application/o/dtmcp/jwks/")
                .run(ctx -> {
                    AuthentikMachineJwtProperties p = ctx.getBean(AuthentikMachineJwtProperties.class);
                    assertThat(p.getJwkSetUri()).isEqualTo("http://localhost:9000/application/o/dtmcp/jwks/");
                });
    }
}
