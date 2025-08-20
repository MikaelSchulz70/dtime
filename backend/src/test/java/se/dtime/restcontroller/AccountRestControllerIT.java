package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import se.dtime.model.Account;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountRestControllerIT extends BaseRestControllerIT {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAccountSuccessfully() throws Exception {
        Account account = Account.builder()
                .id(0L)
                .name("New Account")
                .activationStatus(ActivationStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(account)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAccountSuccessfully() throws Exception {
        Account account = Account.builder()
                .id(testAccount.getId())
                .name("Updated Account")
                .activationStatus(ActivationStatus.ACTIVE) // Keep ACTIVE to avoid validation error
                .build();

        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(account)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidAccount() throws Exception {
        Account account = Account.builder()
                .id(0L)
                .name("") // Invalid: empty name
                .activationStatus(ActivationStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(account)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForNullStatus() throws Exception {
        Account account = Account.builder()
                .id(0L)
                .name("Test Account")
                .activationStatus(null) // Invalid: null status
                .build();

        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(account)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUserRole() throws Exception {
        Account account = Account.builder()
                .id(0L)
                .name("New Account")
                .activationStatus(ActivationStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(account)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForNoAuthentication() throws Exception {
        Account account = Account.builder()
                .id(0L)
                .name("New Account")
                .activationStatus(ActivationStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(account)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllAccountsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/account"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllActiveAccountsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/account").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForGetAllWithUserRole() throws Exception {
        mockMvc.perform(get("/api/account"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAccountByIdSuccessfully() throws Exception {
        mockMvc.perform(get("/api/account/" + testAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(testAccount.getName()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForGetByIdWithUserRole() throws Exception {
        mockMvc.perform(get("/api/account/" + testAccount.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldValidateAttributeSuccessfully() throws Exception {
        Attribute attribute = new Attribute();
        attribute.setName("name");
        attribute.setValue("Valid Account Name");

        mockMvc.perform(post("/api/account/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(attribute)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForValidateWithUserRole() throws Exception {
        Attribute attribute = new Attribute();
        attribute.setName("name");
        attribute.setValue("Valid Account Name");

        mockMvc.perform(post("/api/account/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(attribute)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteAccountSuccessfully() throws Exception {
        // Create an account without tasks for deletion
        se.dtime.dbmodel.AccountPO accountPO = createAndSaveAccount("Account To Delete");
        
        mockMvc.perform(delete("/api/account/" + accountPO.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForDeleteWithUserRole() throws Exception {
        mockMvc.perform(delete("/api/account/" + testAccount.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForDeleteWithNoAuthentication() throws Exception {
        mockMvc.perform(delete("/api/account/" + testAccount.getId()))
                .andExpect(status().isUnauthorized());
    }
}