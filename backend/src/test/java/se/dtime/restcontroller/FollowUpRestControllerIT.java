package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import se.dtime.model.report.FollowUpReportType;
import se.dtime.model.report.ReportView;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FollowUpRestControllerIT extends BaseRestControllerIT {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetCurrentFollowUpReportSuccessfully() throws Exception {
        mockMvc.perform(get("/api/followup")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetCurrentFollowUpReportWithViewSuccessfully() throws Exception {
        mockMvc.perform(get("/api/followup")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetCurrentFollowUpReportWithTypeSuccessfully() throws Exception {
        mockMvc.perform(get("/api/followup")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.USER.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetCurrentFollowUpReportWithBothParamsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/followup")
                        .param("view", ReportView.YEAR.name())
                        .param("type", FollowUpReportType.ACCOUNT.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForCurrentFollowUpReportWithUserRole() throws Exception {
        mockMvc.perform(get("/api/followup")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForCurrentFollowUpReportWithNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/followup"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetPreviousFollowUpReportSuccessfully() throws Exception {
        mockMvc.perform(get("/api/followup/previous")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetPreviousFollowUpReportWithAllParamsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/followup/previous")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.USER.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForPreviousFollowUpReportWithoutDate() throws Exception {
        mockMvc.perform(get("/api/followup/previous")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForPreviousFollowUpReportWithInvalidDate() throws Exception {
        mockMvc.perform(get("/api/followup/previous")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name())
                        .param("date", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidReportView() throws Exception {
        mockMvc.perform(get("/api/followup/previous")
                        .param("view", "INVALID_VIEW")
                        .param("type", FollowUpReportType.ACCOUNT.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidFollowUpReportType() throws Exception {
        mockMvc.perform(get("/api/followup/previous")
                        .param("view", ReportView.MONTH.name())
                        .param("type", "INVALID_TYPE")
                        .param("date", "2024-01-15"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForPreviousFollowUpReportWithUserRole() throws Exception {
        mockMvc.perform(get("/api/followup/previous")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetNextFollowUpReportSuccessfully() throws Exception {
        mockMvc.perform(get("/api/followup/next")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetNextFollowUpReportWithAllParamsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/followup/next")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForNextFollowUpReportWithoutDate() throws Exception {
        mockMvc.perform(get("/api/followup/next")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForNextFollowUpReportWithInvalidDate() throws Exception {
        mockMvc.perform(get("/api/followup/next")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name())
                        .param("date", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForNextFollowUpReportWithUserRole() throws Exception {
        mockMvc.perform(get("/api/followup/next")
                        .param("view", ReportView.MONTH.name())
                        .param("type", FollowUpReportType.ACCOUNT.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForPreviousFollowUpReportWithNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/followup/previous")
                        .param("date", "2024-01-15"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedForNextFollowUpReportWithNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/followup/next")
                        .param("date", "2024-01-15"))
                .andExpect(status().isUnauthorized());
    }
}