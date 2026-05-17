package se.dtime.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PublicFrontendUrlResolverTest {

    @Test
    void resolve_prefersExplicitSwitchUserBaseUrl() {
        assertThat(PublicFrontendUrlResolver.resolve(
                "https://app.example/", true, "https://ignored/", null))
                .isEqualTo("https://app.example");
    }

    @Test
    void resolve_usesDevServerWhenEnabled() {
        assertThat(PublicFrontendUrlResolver.resolve("", true, "https://localhost:3000/", null))
                .isEqualTo("https://localhost:3000");
    }

    @Test
    void resolve_usesForwardedHeadersWhenPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-Host", "localhost:3000");
        request.addHeader("X-Forwarded-Proto", "https");

        assertThat(PublicFrontendUrlResolver.resolve("", false, "", request))
                .isEqualTo("https://localhost:3000");
    }
}
