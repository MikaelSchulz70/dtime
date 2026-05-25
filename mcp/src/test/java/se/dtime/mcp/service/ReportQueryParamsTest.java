package se.dtime.mcp.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportQueryParamsTest {

    @Test
    void normalize_passesThroughValidArgs() {
        ReportQueryParams.Normalized n = ReportQueryParams.normalize("MONTH", "USER", "2026-05-15");
        assertThat(n.view()).isEqualTo("MONTH");
        assertThat(n.type()).isEqualTo("USER");
        assertThat(n.date()).isEqualTo("2026-05-15");
    }

    @Test
    void normalize_swapsWhenViewAndTypeReversed() {
        ReportQueryParams.Normalized n = ReportQueryParams.normalize("USER_TASK", "MONTH", null);
        assertThat(n.view()).isEqualTo("MONTH");
        assertThat(n.type()).isEqualTo("USER_TASK");
    }

    @Test
    void normalize_movesTypeFromViewWhenTypeMissing() {
        ReportQueryParams.Normalized n = ReportQueryParams.normalize("USER_TASK", null, null);
        assertThat(n.view()).isEqualTo("MONTH");
        assertThat(n.type()).isEqualTo("USER_TASK");
    }

    @Test
    void normalize_isCaseInsensitive() {
        ReportQueryParams.Normalized n = ReportQueryParams.normalize("month", "user", null);
        assertThat(n.view()).isEqualTo("MONTH");
        assertThat(n.type()).isEqualTo("USER");
    }

    @Test
    void normalize_treatsMonthNameInViewAsMonthPeriod() {
        int year = LocalDate.now().getYear();
        ReportQueryParams.Normalized n = ReportQueryParams.normalize("APRIL", "USER", null);
        assertThat(n.view()).isEqualTo("MONTH");
        assertThat(n.type()).isEqualTo("USER");
        assertThat(n.date()).isEqualTo(year + "-04-15");
    }

    @Test
    void normalize_parsesYearMonthDate() {
        ReportQueryParams.Normalized n = ReportQueryParams.normalize("MONTH", "USER", "2026-04");
        assertThat(n.date()).isEqualTo("2026-04-15");
    }

    @Test
    void normalize_rejectsInvalidView() {
        assertThatThrownBy(() -> ReportQueryParams.normalize("WEEK", "USER", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("view")
                .hasMessageContaining("MONTH");
    }

    @Test
    void normalize_rejectsInvalidType() {
        assertThatThrownBy(() -> ReportQueryParams.normalize("MONTH", "BILLABLE", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type")
                .hasMessageContaining("USER");
    }
}
