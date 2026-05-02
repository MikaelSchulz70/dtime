package se.dtime.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OidcSwitchUserRedirectSupportTest {

    @Test
    void buildOAuthRestartUrlFromPublicBase_stripsTrailingSlash() {
        assertThat(OidcSwitchUserRedirectSupport.buildOAuthRestartUrlFromPublicBase("https://localhost:3000/"))
                .isEqualTo("https://localhost:3000/oauth2/authorization/authentik?switch_user=1");
    }

    @Test
    void buildOAuthRestartUrlFromPublicBase_rejectsBlank() {
        assertThatThrownBy(() -> OidcSwitchUserRedirectSupport.buildOAuthRestartUrlFromPublicBase("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
