package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportMcpToolsTest {

    @Mock
    private BackendApiClient backendApiClient;

    private ReportMcpTools tools;

    @BeforeEach
    void setUp() {
        tools = new ReportMcpTools(backendApiClient, new ObjectMapper());
    }

    @Test
    void getCurrentReports_includesViewAndType() throws Exception {
        when(backendApiClient.get("/api/report?view=MONTH&type=ACCOUNT", Object.class))
                .thenReturn(Map.of());

        tools.getCurrentReports("MONTH", "ACCOUNT");

        verify(backendApiClient).get("/api/report?view=MONTH&type=ACCOUNT", Object.class);
    }

    @Test
    void getPreviousReport_includesDate() throws Exception {
        when(backendApiClient.get(
                "/api/report/previous?view=YEAR&type=USER&date=2024-06-01", Object.class))
                .thenReturn(Map.of());

        tools.getPreviousReport("YEAR", "USER", "2024-06-01");

        verify(backendApiClient).get(
                "/api/report/previous?view=YEAR&type=USER&date=2024-06-01", Object.class);
    }

    @Test
    void getMonthlyUserReportSummary_usesPreviousReportForTargetMonth() throws Exception {
        when(backendApiClient.get(
                "/api/report/previous?view=MONTH&type=USER&date=2026-06-01", Object.class))
                .thenReturn(Map.of("userReports", java.util.List.of()));

        tools.getMonthlyUserReportSummary("2026-05-15");

        verify(backendApiClient).get(
                "/api/report/previous?view=MONTH&type=USER&date=2026-06-01", Object.class);
    }

    @Test
    void getBillableTaskTypeReport_includesDateRange() throws Exception {
        when(backendApiClient.get(
                "/api/report/billable-task-type?fromDate=2024-01-01&toDate=2024-01-31", Object.class))
                .thenReturn(Map.of());

        tools.getBillableTaskTypeReport("2024-01-01", "2024-01-31");

        verify(backendApiClient).get(
                "/api/report/billable-task-type?fromDate=2024-01-01&toDate=2024-01-31", Object.class);
    }
}
