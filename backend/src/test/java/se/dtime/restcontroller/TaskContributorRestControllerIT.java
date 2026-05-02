package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.Task;
import se.dtime.model.TaskContributor;
import se.dtime.model.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskContributorRestControllerIT extends BaseRestControllerIT {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAddTaskContributorSuccessfully() throws Exception {
        TaskContributor taskContributor = TaskContributor.builder()
                .id(0L)
                .user(User.builder().id(testUser.getId()).build())
                .task(Task.builder().id(testTask.getId()).build())
                .activationStatus(ActivationStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/taskcontributor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(taskContributor)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateTaskContributorSuccessfully() throws Exception {
        // First create a TaskContributor
        TaskContributor firstTaskContributor = TaskContributor.builder()
                .id(0L)
                .user(User.builder().id(testUser.getId()).build())
                .task(Task.builder().id(testTask.getId()).build())
                .activationStatus(ActivationStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/taskcontributor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(firstTaskContributor)))
                .andExpect(status().isCreated());

        // Now create another TaskContributor for the same user and task (should update)
        TaskContributor updateTaskContributor = TaskContributor.builder()
                .id(0L) // Will be ignored - lookup by user and task
                .user(User.builder().id(testUser.getId()).build())
                .task(Task.builder().id(testTask.getId()).build())
                .activationStatus(ActivationStatus.INACTIVE)
                .build();

        mockMvc.perform(post("/api/taskcontributor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateTaskContributor)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidTaskContributor() throws Exception {
        TaskContributor taskContributor = TaskContributor.builder()
                .id(0L)
                .user(User.builder().id(0L).build()) // Invalid: zero user ID
                .task(Task.builder().id(0L).build()) // Invalid: zero task ID
                .activationStatus(ActivationStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/taskcontributor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(taskContributor)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForNullStatus() throws Exception {
        TaskContributor taskContributor = TaskContributor.builder()
                .id(0L)
                .user(User.builder().id(testUser.getId()).build())
                .task(Task.builder().id(testTask.getId()).build())
                .activationStatus(null) // Invalid: null status
                .build();

        mockMvc.perform(post("/api/taskcontributor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(taskContributor)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUserRole() throws Exception {
        TaskContributor taskContributor = TaskContributor.builder()
                .id(0L)
                .user(User.builder().id(testUser.getId()).build())
                .task(Task.builder().id(testTask.getId()).build())
                .activationStatus(ActivationStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/taskcontributor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(taskContributor)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForNoAuthentication() throws Exception {
        TaskContributor taskContributor = TaskContributor.builder()
                .id(0L)
                .user(User.builder().id(testUser.getId()).build())
                .task(Task.builder().id(testTask.getId()).build())
                .activationStatus(ActivationStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/taskcontributor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(taskContributor)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetTaskContributorsForUserSuccessfully() throws Exception {
        mockMvc.perform(get("/api/taskcontributor/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForGetTaskContributorsWithUserRole() throws Exception {
        mockMvc.perform(get("/api/taskcontributor/" + testUser.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "ADMIN")
    void shouldGetCurrentTaskContributorsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/taskcontributor/currentTaskContributors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForGetCurrentTaskContributorsWithUserRole() throws Exception {
        mockMvc.perform(get("/api/taskcontributor/currentTaskContributors"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldValidateAttributeSuccessfully() throws Exception {
        Attribute attribute = new Attribute();
        attribute.setName("userId");
        attribute.setValue(testUser.getId().toString());

        mockMvc.perform(post("/api/taskcontributor/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(attribute)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForValidateWithUserRole() throws Exception {
        Attribute attribute = new Attribute();
        attribute.setName("userId");
        attribute.setValue(testUser.getId().toString());

        mockMvc.perform(post("/api/taskcontributor/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(attribute)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteTaskContributorSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/taskcontributor/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForDeleteWithUserRole() throws Exception {
        mockMvc.perform(delete("/api/taskcontributor/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldGetSelfTaskContributorsForUserRole() throws Exception {
        mockMvc.perform(get("/api/taskcontributor/self"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldSelfAssignTaskForUserRole() throws Exception {
        mockMvc.perform(post("/api/taskcontributor/self/" + testTask.getId()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldSelfUnassignTaskForUserRole() throws Exception {
        mockMvc.perform(post("/api/taskcontributor/self/" + testTask.getId()))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/taskcontributor/self/" + testTask.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnForbiddenForAdminOnSelfAssignEndpoint() throws Exception {
        mockMvc.perform(post("/api/taskcontributor/self/" + testTask.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForSelfAssignWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/taskcontributor/self/" + testTask.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedForDeleteWithNoAuthentication() throws Exception {
        mockMvc.perform(delete("/api/taskcontributor/1"))
                .andExpect(status().isUnauthorized());
    }
}