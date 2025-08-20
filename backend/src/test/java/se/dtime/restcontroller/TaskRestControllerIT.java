package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import se.dtime.model.Account;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.Task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskRestControllerIT extends BaseRestControllerIT {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateTaskSuccessfully() throws Exception {
        Task task = Task.builder()
                .id(0L)
                .name("New Task")
                .activationStatus(ActivationStatus.ACTIVE)
                .account(Account.builder().id(testAccount.getId()).build())
                .build();

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(task)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateTaskSuccessfully() throws Exception {
        Task task = Task.builder()
                .id(testTask.getId())
                .name("Updated Task")
                .activationStatus(ActivationStatus.INACTIVE)
                .account(Account.builder().id(testAccount.getId()).build())
                .build();

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(task)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidTask() throws Exception {
        Task task = Task.builder()
                .id(0L)
                .name("") // Invalid: empty name
                .activationStatus(ActivationStatus.ACTIVE)
                .account(Account.builder().id(testAccount.getId()).build())
                .build();

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(task)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForNullStatus() throws Exception {
        Task task = Task.builder()
                .id(0L)
                .name("Test Task")
                .activationStatus(null) // Invalid: null status
                .account(Account.builder().id(testAccount.getId()).build())
                .build();

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(task)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidActivationStatus() throws Exception {
        Task task = Task.builder()
                .id(0L)
                .name("Test Task")
                .activationStatus(null)
                .account(Account.builder().id(0L).build()) // Invalid: zero account ID
                .build();

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(task)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUserRole() throws Exception {
        Task task = Task.builder()
                .id(0L)
                .name("New Task")
                .activationStatus(ActivationStatus.ACTIVE)
                .account(Account.builder().id(testAccount.getId()).build())
                .build();

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(task)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForNoAuthentication() throws Exception {
        Task task = Task.builder()
                .id(0L)
                .name("New Task")
                .activationStatus(ActivationStatus.ACTIVE)
                .account(Account.builder().id(testAccount.getId()).build())
                .build();

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(task)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllTasksSuccessfully() throws Exception {
        mockMvc.perform(get("/api/task"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllActiveTasksSuccessfully() throws Exception {
        mockMvc.perform(get("/api/task").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForGetAllWithUserRole() throws Exception {
        mockMvc.perform(get("/api/task"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetTaskByIdSuccessfully() throws Exception {
        mockMvc.perform(get("/api/task/" + testTask.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(testTask.getName()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForGetByIdWithUserRole() throws Exception {
        mockMvc.perform(get("/api/task/" + testTask.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldValidateAttributeSuccessfully() throws Exception {
        Attribute attribute = new Attribute();
        attribute.setName("name");
        attribute.setValue("Valid Task Name");

        mockMvc.perform(post("/api/task/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(attribute)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForValidateWithUserRole() throws Exception {
        Attribute attribute = new Attribute();
        attribute.setName("name");
        attribute.setValue("Valid Task Name");

        mockMvc.perform(post("/api/task/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(attribute)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteTaskSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/task/" + testTask.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForDeleteWithUserRole() throws Exception {
        mockMvc.perform(delete("/api/task/" + testTask.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForDeleteWithNoAuthentication() throws Exception {
        mockMvc.perform(delete("/api/task/" + testTask.getId()))
                .andExpect(status().isUnauthorized());
    }
}