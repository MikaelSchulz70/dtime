package se.dtime.restcontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import se.dtime.model.SpecialDay;
import se.dtime.model.timereport.DayType;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SpecialDayRestControllerIT extends BaseRestControllerIT {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

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