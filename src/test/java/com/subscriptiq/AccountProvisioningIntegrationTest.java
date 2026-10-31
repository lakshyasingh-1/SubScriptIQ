package com.subscriptiq;

import com.subscriptiq.account.AccountRole;
import com.subscriptiq.account.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AccountProvisioningIntegrationTest extends AccountIntegrationTestBase {
    @Test
    void adminCanProvisionSeparateStaffAndAdminCredentials() throws Exception {
        MockHttpSession adminSession = loginAsAdmin();
        createAccount(adminSession, "new-staff@example.test", "Avery Staff", "STAFF", "/staff/dashboard");
        createAccount(adminSession, "new-admin@example.test", "Taylor Admin", "ADMIN", "/admin/dashboard");

        assertEquals(AccountRole.STAFF, account("new-staff@example.test").getRole());
        assertEquals(AccountRole.ADMIN, account("new-admin@example.test").getRole());
        assertNotEquals(TEST_PASSWORD, account("new-staff@example.test").getPasswordHash());
        assertNotEquals(TEST_PASSWORD, account("new-admin@example.test").getPasswordHash());
    }

    @Test
    void adminProvisioningRejectsCustomerRoleAndDuplicateEmail() throws Exception {
        MockHttpSession adminSession = loginAsAdmin();

        mockMvc.perform(post("/admin/staff").session(adminSession).with(csrf())
                        .param("fullName", "Invalid Customer")
                        .param("email", "not-provisioned@example.test")
                        .param("password", TEST_PASSWORD)
                        .param("role", "CUSTOMER"))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertTrue(
                accounts.findByEmailIgnoreCase("not-provisioned@example.test").isEmpty());

        mockMvc.perform(post("/admin/staff").session(adminSession).with(csrf())
                        .param("fullName", "Duplicate Account")
                        .param("email", "staff@brightloop.example")
                        .param("password", TEST_PASSWORD)
                        .param("role", "STAFF"))
                .andExpect(status().isOk());
        assertEquals(1, accounts.findAllByRoleInOrderByFullNameAsc(
                java.util.List.of(AccountRole.STAFF)).stream()
                .filter(account -> account.getEmail().equals("staff@brightloop.example"))
                .count());
    }

    @Test
    void staffCannotProvisionAccountsAndNewCustomerHasAnEmptyPersonalDashboard() throws Exception {
        MvcResult staffLogin = mockMvc.perform(post("/login").with(csrf())
                        .param("username", "staff@brightloop.example")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/staff/dashboard"))
                .andReturn();
        MockHttpSession staffSession = (MockHttpSession) staffLogin.getRequest().getSession(false);
        mockMvc.perform(post("/admin/staff").session(staffSession).with(csrf())
                        .param("fullName", "Not Allowed")
                        .param("email", "forbidden@example.test")
                        .param("password", TEST_PASSWORD)
                        .param("role", "STAFF"))
                .andExpect(status().isForbidden());
        org.junit.jupiter.api.Assertions.assertTrue(accounts.findByEmailIgnoreCase("forbidden@example.test").isEmpty());

        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Fresh Customer")
                        .param("email", "fresh-customer@example.test")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection());
        MvcResult customerLogin = mockMvc.perform(post("/login").with(csrf())
                        .param("username", "fresh-customer@example.test")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/portal"))
                .andReturn();
        MockHttpSession customerSession = (MockHttpSession) customerLogin.getRequest().getSession(false);
        mockMvc.perform(get("/portal").session(customerSession)).andExpect(status().isOk());
        mockMvc.perform(get("/portal/invoices").session(customerSession)).andExpect(status().isOk());
    }

    private MockHttpSession loginAsAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/login").with(csrf())
                        .param("username", "admin@brightloop.example")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/admin/dashboard"))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private void createAccount(MockHttpSession session, String email, String name, String role,
                               String expectedDestination) throws Exception {
        mockMvc.perform(post("/admin/staff").session(session).with(csrf())
                        .param("fullName", name)
                        .param("email", email)
                        .param("password", TEST_PASSWORD)
                        .param("role", role))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/admin/staff"));

        MvcResult login = mockMvc.perform(post("/login").with(csrf())
                        .param("username", email)
                        .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", expectedDestination))
                .andReturn();
        MockHttpSession userSession = (MockHttpSession) login.getRequest().getSession(false);
        String deniedPath = role.equals("STAFF") ? "/admin/dashboard" : "/staff/dashboard";
        mockMvc.perform(get(deniedPath).session(userSession)).andExpect(status().isForbidden());
    }

    private UserAccount account(String email) {
        return accounts.findByEmailIgnoreCase(email).orElseThrow();
    }
}
