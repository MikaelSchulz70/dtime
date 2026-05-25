package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TimeReportStatusRestControllerIT extends BaseRestControllerIT {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetUnclosedUsersForMonthSuccessfully() throws Exception {
        mockMvc.perform(get("/api/timereportstatus/year/2025/month/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnActiveUsersWithOpenReportsByDefault() throws Exception {
        mockMvc.perform(get("/api/timereportstatus/year/2025/month/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.unclosedUsers.length()").value(2))
                .andExpect(jsonPath("$.unclosedUsers[0].closed").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidMonth() throws Exception {
        mockMvc.perform(get("/api/timereportstatus/year/2025/month/13"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetClosedUsersForMonthWhenClosedQueryParam() throws Exception {
        mockMvc.perform(get("/api/timereportstatus/year/2025/month/1?closed=true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUserRole() throws Exception {
        mockMvc.perform(get("/api/timereportstatus/year/2025/month/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/timereportstatus/year/2025/month/1"))
                .andExpect(status().isUnauthorized());
    }
}
