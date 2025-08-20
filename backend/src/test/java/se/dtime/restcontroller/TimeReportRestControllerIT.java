package se.dtime.restcontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.timereport.Day;
import se.dtime.model.timereport.TimeEntry;
import se.dtime.model.timereport.TimeReportView;
import se.dtime.repository.TaskContributorRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TimeReportRestControllerIT extends BaseRestControllerIT {

    @Autowired
    private TaskContributorRepository taskContributorRepository;

    private TaskContributorPO testTaskContributor;

    @BeforeEach
    void setUpTimeReportData() {
        // Create task contributor for time entry tests
        testTaskContributor = createAndSaveTaskContributor(testUser, testTask);
    }

    private TaskContributorPO createAndSaveTaskContributor(se.dtime.dbmodel.UserPO user, se.dtime.dbmodel.TaskPO task) {
        TaskContributorPO contributor = new TaskContributorPO();
        contributor.setUser(user);
        contributor.setTask(task);
        contributor.setActivationStatus(ActivationStatus.ACTIVE);
        contributor.setCreatedBy(1L);
        contributor.setUpdatedBy(1L);
        contributor.setCreateDateTime(LocalDateTime.now());
        contributor.setUpdatedDateTime(LocalDateTime.now());
        return taskContributorRepository.save(contributor);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldAddTimeEntrySuccessfully() throws Exception {
        LocalDate now = LocalDate.now();
        TimeEntry timeEntry = TimeEntry.builder()
                .id(0L)
                .taskContributorId(testTaskContributor.getId())
                .day(Day.builder().year(now.getYear()).month(now.getMonthValue()).date(now).build())
                .time(8.0f)
                .build();

        mockMvc.perform(post("/api/timereport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(timeEntry)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldUpdateTimeEntrySuccessfully() throws Exception {
        LocalDate now = LocalDate.now();
        TimeEntry timeEntry = TimeEntry.builder()
                .id(1L) // Existing ID
                .taskContributorId(testTaskContributor.getId())
                .day(Day.builder().year(now.getYear()).month(now.getMonthValue()).date(now).build())
                .time(6.5f)
                .build();

        mockMvc.perform(post("/api/timereport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(timeEntry)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldReturnBadRequestForInvalidTimeEntry() throws Exception {
        TimeEntry timeEntry = TimeEntry.builder()
                .id(0L)
                .taskContributorId(0L) // Invalid: zero task contributor ID
                .day(Day.builder().date(LocalDate.now()).build())
                .time(-1.0f) // Invalid: negative time
                .build();

        mockMvc.perform(post("/api/timereport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(timeEntry)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldReturnBadRequestForNullDate() throws Exception {
        TimeEntry timeEntry = TimeEntry.builder()
                .id(0L)
                .taskContributorId(testTaskContributor.getId())
                .day(null) // Invalid: null day
                .time(8.0f)
                .build();

        mockMvc.perform(post("/api/timereport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(timeEntry)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedForAddTimeEntryWithNoAuthentication() throws Exception {
        TimeEntry timeEntry = TimeEntry.builder()
                .id(0L)
                .taskContributorId(testTaskContributor.getId())
                .day(Day.builder().date(LocalDate.now()).build())
                .time(8.0f)
                .build();

        mockMvc.perform(post("/api/timereport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(timeEntry)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldGetCurrentTimeReportSuccessfully() throws Exception {
        mockMvc.perform(get("/api/timereport"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldGetCurrentTimeReportWithViewSuccessfully() throws Exception {
        mockMvc.perform(get("/api/timereport")
                        .param("view", TimeReportView.WEEK.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturnUnauthorizedForGetCurrentTimeReportWithNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/timereport"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldGetPreviousTimeReportSuccessfully() throws Exception {
        mockMvc.perform(get("/api/timereport/previous")
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldGetPreviousTimeReportWithViewSuccessfully() throws Exception {
        mockMvc.perform(get("/api/timereport/previous")
                        .param("view", TimeReportView.WEEK.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldReturnBadRequestForPreviousTimeReportWithoutDate() throws Exception {
        mockMvc.perform(get("/api/timereport/previous"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldReturnBadRequestForPreviousTimeReportWithInvalidDate() throws Exception {
        mockMvc.perform(get("/api/timereport/previous")
                        .param("date", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldGetNextTimeReportSuccessfully() throws Exception {
        mockMvc.perform(get("/api/timereport/next")
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldGetNextTimeReportWithViewSuccessfully() throws Exception {
        mockMvc.perform(get("/api/timereport/next")
                        .param("view", TimeReportView.WEEK.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldReturnBadRequestForNextTimeReportWithoutDate() throws Exception {
        mockMvc.perform(get("/api/timereport/next"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetUserTimeReportSuccessfully() throws Exception {
        mockMvc.perform(get("/api/timereport/user")
                        .param("userId", testUser.getId().toString())
                        .param("view", TimeReportView.WEEK.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForUserTimeReportWithoutRequiredParams() throws Exception {
        mockMvc.perform(get("/api/timereport/user"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForUserTimeReportWithInvalidUserId() throws Exception {
        mockMvc.perform(get("/api/timereport/user")
                        .param("userId", "invalid")
                        .param("view", TimeReportView.WEEK.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUserTimeReportWithUserRole() throws Exception {
        mockMvc.perform(get("/api/timereport/user")
                        .param("userId", testUser.getId().toString())
                        .param("view", TimeReportView.WEEK.name())
                        .param("date", "2024-01-15"))
                .andExpect(status().isForbidden());
    }
}