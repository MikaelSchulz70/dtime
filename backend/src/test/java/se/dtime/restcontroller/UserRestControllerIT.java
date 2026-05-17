package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserRestControllerIT extends BaseRestControllerIT {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllUsersSuccessfully() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllActiveUsersSuccessfully() throws Exception {
        mockMvc.perform(get("/api/users").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetPagedUsersSuccessfully() throws Exception {
        mockMvc.perform(get("/api/users/paged")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.currentPage").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetPagedUsersWithFiltersSuccessfully() throws Exception {
        mockMvc.perform(get("/api/users/paged")
                .param("page", "0")
                .param("size", "10")
                .param("active", "true")
                .param("firstName", "Test")
                .param("sort", "firstName")
                .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetPagedUsersWithUserIdSortAlias() throws Exception {
        mockMvc.perform(get("/api/users/paged")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "userId")
                .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForGetPagedWithUserRole() throws Exception {
        mockMvc.perform(get("/api/users/paged"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForGetAllWithUserRole() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetUserByIdSuccessfully() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForGetByIdWithUserRole() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeactivateUserSuccessfully() throws Exception {
        mockMvc.perform(post("/api/users/" + testUser.getId() + "/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForDeactivateWithUserRole() throws Exception {
        mockMvc.perform(post("/api/users/" + testUser.getId() + "/deactivate"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForDeactivateWithNoAuthentication() throws Exception {
        mockMvc.perform(post("/api/users/" + testUser.getId() + "/deactivate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldActivateUserSuccessfully() throws Exception {
        mockMvc.perform(post("/api/users/" + testUser.getId() + "/deactivate"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users/" + testUser.getId() + "/activate"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForActivateWithUserRole() throws Exception {
        mockMvc.perform(post("/api/users/" + testUser.getId() + "/activate"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCreateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }
}
