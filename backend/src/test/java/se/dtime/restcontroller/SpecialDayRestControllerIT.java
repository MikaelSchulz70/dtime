package se.dtime.restcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import se.dtime.model.SpecialDay;
import se.dtime.model.timereport.DayType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=true;INIT=CREATE SCHEMA IF NOT EXISTS \"public\"",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.default_schema=PUBLIC",
        "security.enable-csrf=false"
})
@Transactional
public class SpecialDayRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAvailableYears() throws Exception {
        mockMvc.perform(get("/api/specialday/years"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateSpecialDaySuccessfully() throws Exception {
        SpecialDay specialDay = SpecialDay.builder()
                .name("Test Holiday")
                .dayType(DayType.PUBLIC_HOLIDAY)
                .date(LocalDate.of(2025, 12, 25))
                .build();

        mockMvc.perform(post("/api/specialday")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialDay)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUserRole() throws Exception {
        mockMvc.perform(get("/api/specialday/years"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForNoAuth() throws Exception {
        mockMvc.perform(get("/api/specialday/years"))
                .andExpect(status().isUnauthorized());
    }
}