package com.subscriptiq;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchFunctionalityIntegrationTest extends AccountIntegrationTestBase {
    @Test
    void customerAndSubscriptionSearchesFilterCaseInsensitivelyAndRespectStatus() throws Exception {
        mockMvc.perform(get("/customers").with(user("admin@brightloop.example").roles("ADMIN")).param("q", "NORTH"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Northstar Labs")))
                .andExpect(content().string(not(containsString("Orbit Works"))));
        mockMvc.perform(get("/customers").with(user("admin@brightloop.example").roles("ADMIN")).param("q", "missing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No customers found")));
        mockMvc.perform(get("/customers").with(user("admin@brightloop.example").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Northstar Labs")))
                .andExpect(content().string(containsString("Orbit Works")));

        mockMvc.perform(get("/subscriptions").with(user("staff@brightloop.example").roles("STAFF"))
                        .param("q", "maya").param("status", "PAUSED"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SUB-2026-002")))
                .andExpect(content().string(not(containsString("SUB-2026-001"))));
        mockMvc.perform(get("/subscriptions").with(user("staff@brightloop.example").roles("STAFF"))
                        .param("q", "not-a-subscription"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No subscriptions found")));
    }

    @Test
    void invoiceAndPaymentSearchesUseTheirRecordsAndKeepInvoiceFilters() throws Exception {
        mockMvc.perform(get("/invoices").with(user("admin@brightloop.example").roles("ADMIN"))
                        .param("q", "inv-2026-031").param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("INV-2026-031")))
                .andExpect(content().string(not(containsString("INV-2026-030"))));
        mockMvc.perform(get("/invoices/rows").with(user("staff@brightloop.example").roles("STAFF"))
                        .param("q", "NORTHSTAR").param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("INV-2026-031")));
        mockMvc.perform(get("/invoices").with(user("admin@brightloop.example").roles("ADMIN"))
                        .param("q", "missing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No invoices found")));
        mockMvc.perform(get("/invoices").with(user("admin@brightloop.example").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("change from:select")));

        mockMvc.perform(get("/payments").with(user("staff@brightloop.example").roles("STAFF")).param("q", "part"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Part payment")))
                .andExpect(content().string(not(containsString("Settlement"))));
    }

    @Test
    void adminCatalogAndTeamListsAreSearchable() throws Exception {
        mockMvc.perform(get("/admin/products").with(user("admin@brightloop.example").roles("ADMIN"))
                        .param("q", "ANALYTICS"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Analytics Pack")))
                .andExpect(content().string(not(containsString(">Professional</a>"))));
        mockMvc.perform(get("/admin/plans").with(user("admin@brightloop.example").roles("ADMIN"))
                        .param("q", "priority"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Priority support")));
        mockMvc.perform(get("/admin/staff").with(user("admin@brightloop.example").roles("ADMIN"))
                        .param("q", "jordan"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Jordan Lee")))
                .andExpect(content().string(not(containsString("Samira Patel"))))
                .andExpect(content().string(containsString("name=\"q\" value=\"jordan\"")));
        mockMvc.perform(get("/admin/staff").with(user("admin@brightloop.example").roles("ADMIN"))
                        .param("q", "rohit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No team members found")))
                .andExpect(content().string(containsString("name=\"q\" value=\"rohit\"")));
        mockMvc.perform(get("/admin/taxes").with(user("admin@brightloop.example").roles("ADMIN"))
                        .param("q", "5.00"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("GST Reduced")));
        mockMvc.perform(get("/admin/discounts").with(user("admin@brightloop.example").roles("ADMIN"))
                        .param("q", "launch"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Launch offer")))
                .andExpect(content().string(not(containsString("Analytics bundle"))));
    }

    @Test
    void globalSearchReturnsRoleAppropriateResultsAndDoesNotLeakCustomerRecords() throws Exception {
        mockMvc.perform(get("/search").with(user("admin@brightloop.example").roles("ADMIN")).param("q", "northstar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/customers/1\"")))
                .andExpect(content().string(containsString("href=\"/subscriptions/1\"")))
                .andExpect(content().string(containsString("href=\"/invoices/1\"")));
        mockMvc.perform(get("/search").with(user("staff@brightloop.example").roles("STAFF"))
                        .param("q", "INV-2026-031"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/invoices/1\"")));
        mockMvc.perform(get("/search").with(user("finance@northstar.example").roles("CUSTOMER"))
                        .param("q", "INV-2026-031"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/portal/invoices/1\"")))
                .andExpect(content().string(not(containsString("href=\"/invoices/1\""))));
        mockMvc.perform(get("/search").with(user("maya.kapoor@example.com").roles("CUSTOMER"))
                        .param("q", "northstar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No results found")))
                .andExpect(content().string(not(containsString("Northstar Labs"))));
        mockMvc.perform(get("/search").with(user("staff@brightloop.example").roles("STAFF")).param("q", "nothing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No results found")));
    }

    @Test
    void customerPortalSearchesRemainScopedToTheAuthenticatedCustomer() throws Exception {
        mockMvc.perform(get("/portal/subscriptions").with(user("maya.kapoor@example.com").roles("CUSTOMER"))
                        .param("q", "SUB-2026-002"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SUB-2026-002")))
                .andExpect(content().string(not(containsString("SUB-2026-001"))));
        mockMvc.perform(get("/portal/invoices").with(user("maya.kapoor@example.com").roles("CUSTOMER"))
                        .param("q", "inv-2026-030"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("INV-2026-030")))
                .andExpect(content().string(not(containsString("INV-2026-031"))));
        mockMvc.perform(get("/portal/invoices").with(user("maya.kapoor@example.com").roles("CUSTOMER"))
                        .param("q", "unmatched"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No invoices found")));
        mockMvc.perform(get("/invoices").with(user("maya.kapoor@example.com").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/search").param("q", "INV-2026-031"))
                .andExpect(status().is3xxRedirection());
    }
}
