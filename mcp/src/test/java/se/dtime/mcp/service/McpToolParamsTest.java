package se.dtime.mcp.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class McpToolParamsTest {

    @Test
    void parseInt_acceptsNumericStrings() {
        assertThat(McpToolParams.parseInt("0", "page")).isZero();
        assertThat(McpToolParams.parseInt(" 100 ", "size")).isEqualTo(100);
    }

    @Test
    void parseInt_rejectsBlank() {
        assertThatThrownBy(() -> McpToolParams.parseInt(" ", "page"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
    }
}
