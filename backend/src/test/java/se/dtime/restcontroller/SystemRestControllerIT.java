package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import se.dtime.model.SystemPropertyDB;
import se.dtime.model.SystemPropertyType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SystemRestControllerIT extends BaseRestControllerIT {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetSystemConfigSuccessfully() throws Exception {
        mockMvc.perform(get("/api/system/config"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.systemProperties").exists())
                .andExpect(jsonPath("$.publicHolidays").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForGetConfigWithUserRole() throws Exception {
        mockMvc.perform(get("/api/system/config"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForGetConfigWithNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/system/config"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateSystemPropertySuccessfully() throws Exception {
        SystemPropertyDB systemProperty = SystemPropertyDB.builder()
                .id(testSystemProperty.getId())
                .name("test.property")
                .value("test value")
                .description("Test property description")
                .systemPropertyType(SystemPropertyType.TEXT)
                .build();

        mockMvc.perform(put("/api/system/systemproperty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(systemProperty)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateSystemPropertySuccessfully() throws Exception {
        SystemPropertyDB systemProperty = SystemPropertyDB.builder()
                .id(testSystemProperty.getId())
                .name("new.property")
                .value("new value")
                .description("New property description")
                .systemPropertyType(SystemPropertyType.INT)
                .build();

        mockMvc.perform(put("/api/system/systemproperty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(systemProperty)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidSystemProperty() throws Exception {
        SystemPropertyDB systemProperty = SystemPropertyDB.builder()
                .id(0L)
                .name("") // Invalid: empty name (violates @Size(min=1, max=80))
                .value("test value")
                .description("Test description")
                .systemPropertyType(SystemPropertyType.TEXT)
                .build();

        mockMvc.perform(put("/api/system/systemproperty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(systemProperty)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForNullName() throws Exception {
        SystemPropertyDB systemProperty = SystemPropertyDB.builder()
                .id(0L)
                .name(null) // Invalid: null name (violates @NotNull)
                .value("test value")
                .description("Test description")
                .systemPropertyType(SystemPropertyType.TEXT)
                .build();

        mockMvc.perform(put("/api/system/systemproperty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(systemProperty)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForNameTooLong() throws Exception {
        SystemPropertyDB systemProperty = SystemPropertyDB.builder()
                .id(0L)
                .name("a".repeat(81)) // Invalid: name too long (violates @Size(min=1, max=80))
                .value("test value")
                .description("Test description")
                .systemPropertyType(SystemPropertyType.TEXT)
                .build();

        mockMvc.perform(put("/api/system/systemproperty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(systemProperty)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForValueTooLong() throws Exception {
        SystemPropertyDB systemProperty = SystemPropertyDB.builder()
                .id(0L)
                .name("test.property")
                .value("a".repeat(101)) // Invalid: value too long (violates @Size(min=0, max=100))
                .description("Test description")
                .systemPropertyType(SystemPropertyType.TEXT)
                .build();

        mockMvc.perform(put("/api/system/systemproperty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(systemProperty)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAcceptSystemPropertyWithNullValue() throws Exception {
        SystemPropertyDB systemProperty = SystemPropertyDB.builder()
                .id(testSystemProperty.getId())
                .name("null.value.property")
                .value(null) // Valid: null value is allowed
                .description("Property with null value")
                .systemPropertyType(SystemPropertyType.TEXT)
                .build();

        mockMvc.perform(put("/api/system/systemproperty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(systemProperty)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAcceptSystemPropertyWithEmptyValue() throws Exception {
        SystemPropertyDB systemProperty = SystemPropertyDB.builder()
                .id(testSystemProperty.getId())
                .name("empty.value.property")
                .value("") // Valid: empty value is allowed (min=0)
                .description("Property with empty value")
                .systemPropertyType(SystemPropertyType.TEXT)
                .build();

        mockMvc.perform(put("/api/system/systemproperty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(systemProperty)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUpdateSystemPropertyWithUserRole() throws Exception {
        SystemPropertyDB systemProperty = SystemPropertyDB.builder()
                .id(1L)
                .name("test.property")
                .value("test value")
                .description("Test property description")
                .systemPropertyType(SystemPropertyType.TEXT)
                .build();

        mockMvc.perform(put("/api/system/systemproperty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(systemProperty)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForUpdateSystemPropertyWithNoAuthentication() throws Exception {
        SystemPropertyDB systemProperty = SystemPropertyDB.builder()
                .id(1L)
                .name("test.property")
                .value("test value")
                .description("Test property description")
                .systemPropertyType(SystemPropertyType.TEXT)
                .build();

        mockMvc.perform(put("/api/system/systemproperty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(systemProperty)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSendEmailReminderSuccessfully() throws Exception {
        mockMvc.perform(post("/api/system/emailreminder"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForEmailReminderWithUserRole() throws Exception {
        mockMvc.perform(post("/api/system/emailreminder"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForEmailReminderWithNoAuthentication() throws Exception {
        mockMvc.perform(post("/api/system/emailreminder"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidHttpMethodOnConfig() throws Exception {
        mockMvc.perform(post("/api/system/config"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidHttpMethodOnSystemProperty() throws Exception {
        mockMvc.perform(get("/api/system/systemproperty"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidHttpMethodOnEmailReminder() throws Exception {
        mockMvc.perform(get("/api/system/emailreminder"))
                .andExpect(status().isMethodNotAllowed());
    }
}