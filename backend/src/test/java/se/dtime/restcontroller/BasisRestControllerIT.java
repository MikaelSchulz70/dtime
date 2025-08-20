package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import se.dtime.model.basis.MonthlyCheck;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BasisRestControllerIT extends BaseRestControllerIT {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAddMonthlyCheckSuccessfully() throws Exception {
        MonthlyCheck monthlyCheck = MonthlyCheck.builder()
                .id(0L)
                .accountId(testAccount.getId())
                .date(LocalDate.now().withDayOfMonth(1))
                .build();

        mockMvc.perform(post("/api/basis/invoice/monthlycheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(monthlyCheck)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateMonthlyCheckSuccessfully() throws Exception {
        // First create a MonthlyCheck
        MonthlyCheck firstMonthlyCheck = MonthlyCheck.builder()
                .id(0L)
                .accountId(testAccount.getId())
                .date(LocalDate.now().withDayOfMonth(1))
                .build();

        mockMvc.perform(post("/api/basis/invoice/monthlycheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(firstMonthlyCheck)))
                .andExpect(status().isOk());

        // Now create another MonthlyCheck for the same account and date (should update)
        MonthlyCheck updateMonthlyCheck = MonthlyCheck.builder()
                .id(0L) // Will be ignored - lookup by account and date
                .accountId(testAccount.getId())
                .date(LocalDate.now().withDayOfMonth(1)) // Same date as before
                .build();

        mockMvc.perform(post("/api/basis/invoice/monthlycheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateMonthlyCheck)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidMonthlyCheck() throws Exception {
        MonthlyCheck monthlyCheck = MonthlyCheck.builder()
                .id(0L)
                .accountId(0L) // Invalid: zero account ID
                .date(null) // Invalid: null date
                .build();

        mockMvc.perform(post("/api/basis/invoice/monthlycheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(monthlyCheck)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForNullAccountId() throws Exception {
        MonthlyCheck monthlyCheck = MonthlyCheck.builder()
                .id(0L)
                .accountId(0) // Invalid: zero account ID
                .date(LocalDate.now().withDayOfMonth(1))
                .build();

        mockMvc.perform(post("/api/basis/invoice/monthlycheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(monthlyCheck)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUserRole() throws Exception {
        MonthlyCheck monthlyCheck = MonthlyCheck.builder()
                .id(0L)
                .accountId(testAccount.getId())
                .date(LocalDate.now().withDayOfMonth(1))
                .build();

        mockMvc.perform(post("/api/basis/invoice/monthlycheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(monthlyCheck)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForNoAuthentication() throws Exception {
        MonthlyCheck monthlyCheck = MonthlyCheck.builder()
                .id(0L)
                .accountId(testAccount.getId())
                .date(LocalDate.now().withDayOfMonth(1))
                .build();

        mockMvc.perform(post("/api/basis/invoice/monthlycheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(monthlyCheck)))
                .andExpect(status().isUnauthorized());
    }
}