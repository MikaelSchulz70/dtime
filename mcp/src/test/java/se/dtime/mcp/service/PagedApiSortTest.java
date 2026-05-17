package se.dtime.mcp.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PagedApiSortTest {

    @Test
    void users_mapsUserIdToId() {
        assertThat(PagedApiSort.users("userId")).isEqualTo("id");
    }

    @Test
    void users_keepsFirstName() {
        assertThat(PagedApiSort.users("firstName")).isEqualTo("firstName");
    }

    @Test
    void users_unknownFallsBackToDisplayName() {
        assertThat(PagedApiSort.users("notAField")).isEqualTo("displayName");
    }

    @Test
    void accounts_mapsAccountIdToId() {
        assertThat(PagedApiSort.accounts("accountId")).isEqualTo("id");
    }

    @Test
    void tasks_mapsTaskIdToId() {
        assertThat(PagedApiSort.tasks("taskId")).isEqualTo("id");
    }
}
