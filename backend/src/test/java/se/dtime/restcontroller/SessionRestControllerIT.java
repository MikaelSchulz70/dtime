package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SessionRestControllerIT extends BaseRestControllerIT {

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldGetSessionInfoSuccessfully() throws Exception {
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loggedInUser").exists())
                .andExpect(jsonPath("$.currentDate").exists());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void shouldGetSessionInfoWithAdminRoleSuccessfully() throws Exception {
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loggedInUser").exists())
                .andExpect(jsonPath("$.currentDate").exists());
    }

    @Test
    void shouldReturnUnauthorizedForNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldReturnSessionInfoWithCorrectContentType() throws Exception {
        mockMvc.perform(get("/api/session")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldHandleMultipleSimultaneousRequests() throws Exception {
        // Test that multiple requests work correctly
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/session"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldReturnBadRequestForInvalidHttpMethod() throws Exception {
        mockMvc.perform(post("/api/session"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldReturnBadRequestForPutMethod() throws Exception {
        mockMvc.perform(put("/api/session"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldReturnBadRequestForDeleteMethod() throws Exception {
        mockMvc.perform(delete("/api/session"))
                .andExpect(status().isMethodNotAllowed());
    }
}