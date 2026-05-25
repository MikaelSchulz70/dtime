package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import se.dtime.model.report.ReportType;
import se.dtime.model.timereport.CloseDate;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportRestControllerIT extends BaseRestControllerIT {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetMonthlyReportSuccessfully() throws Exception {
        mockMvc.perform(get("/api/report/year/2025/month/1")
                        .param("type", ReportType.ACCOUNT.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetYearlyReportSuccessfully() throws Exception {
        mockMvc.perform(get("/api/report/year/2025")
                        .param("type", ReportType.USER.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidMonth() throws Exception {
        mockMvc.perform(get("/api/report/year/2025/month/13")
                        .param("type", ReportType.TASK.name()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForAdminReportWithUserRole() throws Exception {
        mockMvc.perform(get("/api/report/year/2025/month/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForReportWithNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/report/year/2025/month/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldGetUserReportForMonthSuccessfully() throws Exception {
        mockMvc.perform(get("/api/report/user/year/2025/month/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturnUnauthorizedForUserReportWithNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/report/user/year/2025/month/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldCloseTimeReportSuccessfully() throws Exception {
        CloseDate closeDate = CloseDate.builder()
                .userId(testUser.getId())
                .closeDate(LocalDate.now().minusDays(7))
                .build();

        mockMvc.perform(post("/api/report/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(closeDate)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldReturnBadRequestForInvalidCloseDate() throws Exception {
        CloseDate closeDate = CloseDate.builder()
                .userId(0L)
                .closeDate(null)
                .build();

        mockMvc.perform(post("/api/report/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(closeDate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedForCloseTimeReportWithNoAuthentication() throws Exception {
        CloseDate closeDate = CloseDate.builder()
                .userId(testUser.getId())
                .closeDate(LocalDate.now().minusDays(7))
                .build();

        mockMvc.perform(post("/api/report/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(closeDate)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldOpenTimeReportSuccessfully() throws Exception {
        CloseDate closeDate = CloseDate.builder()
                .userId(testUser.getId())
                .closeDate(LocalDate.now().minusDays(7))
                .build();

        mockMvc.perform(post("/api/report/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(closeDate)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidOpenDate() throws Exception {
        CloseDate closeDate = CloseDate.builder()
                .userId(0L)
                .closeDate(null)
                .build();

        mockMvc.perform(post("/api/report/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(closeDate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForOpenTimeReportWithUserRole() throws Exception {
        CloseDate closeDate = CloseDate.builder()
                .userId(testUser.getId())
                .closeDate(LocalDate.now().minusDays(7))
                .build();

        mockMvc.perform(post("/api/report/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(closeDate)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForOpenTimeReportWithNoAuthentication() throws Exception {
        CloseDate closeDate = CloseDate.builder()
                .userId(testUser.getId())
                .closeDate(LocalDate.now().minusDays(7))
                .build();

        mockMvc.perform(post("/api/report/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(closeDate)))
                .andExpect(status().isUnauthorized());
    }
}
