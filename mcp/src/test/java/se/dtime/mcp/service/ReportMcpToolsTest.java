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
    void getReport_usesPathBasedMonthlyEndpoint() throws Exception {
        when(backendApiClient.get("/api/report/year/2026/month/5?type=USER", Object.class))
                .thenReturn(Map.of());

        tools.getReport(2026, "5", "USER");

        verify(backendApiClient).get("/api/report/year/2026/month/5?type=USER", Object.class);
    }

    @Test
    void getReport_acceptsMonthName() throws Exception {
        when(backendApiClient.get("/api/report/year/2026/month/4?type=USER", Object.class))
                .thenReturn(Map.of());

        tools.getReport(2026, "APRIL", "USER");

        verify(backendApiClient).get("/api/report/year/2026/month/4?type=USER", Object.class);
    }

    @Test
    void getYearlyReport_usesPathBasedYearEndpoint() throws Exception {
        when(backendApiClient.get("/api/report/year/2026?type=ACCOUNT", Object.class))
                .thenReturn(Map.of());

        tools.getYearlyReport(2026, "ACCOUNT");

        verify(backendApiClient).get("/api/report/year/2026?type=ACCOUNT", Object.class);
    }

    @Test
    void getReport_billableTaskType_usesBillableEndpointForMonth() throws Exception {
        when(backendApiClient.get(
                "/api/report/billable-task-type?fromDate=2026-04-01&toDate=2026-04-30", Object.class))
                .thenReturn(Map.of());

        tools.getReport(2026, "4", "BILLABLE_TASK_TYPE");

        verify(backendApiClient).get(
                "/api/report/billable-task-type?fromDate=2026-04-01&toDate=2026-04-30", Object.class);
    }

    @Test
    void getYearlyReport_billableTaskType_usesBillableEndpointForYear() throws Exception {
        when(backendApiClient.get(
                "/api/report/billable-task-type?fromDate=2026-01-01&toDate=2026-12-31", Object.class))
                .thenReturn(Map.of());

        tools.getYearlyReport(2026, "BILLABLE_TASK_TYPE");

        verify(backendApiClient).get(
                "/api/report/billable-task-type?fromDate=2026-01-01&toDate=2026-12-31", Object.class);
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
