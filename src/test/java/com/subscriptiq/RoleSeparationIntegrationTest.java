package com.subscriptiq;

import com.subscriptiq.account.AccountRole;
import com.subscriptiq.account.UserAccount;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleSeparationIntegrationTest extends AccountIntegrationTestBase {
    @Test
    void loginUsesAccountRoleForDestinationAndNavigation() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("name=\"role\""))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/login\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"username\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"password\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/register\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Demo accounts"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Demo123!pass"))));

        MockHttpSession admin = login("admin@brightloop.example", "/admin/dashboard");
        mockMvc.perform(get("/admin/dashboard").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Finance analytics")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("href=\"/staff/dashboard\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("href=\"/portal\""))));

        MockHttpSession staff = login("staff@brightloop.example", "/staff/dashboard");
        mockMvc.perform(get("/staff/dashboard").session(staff))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/customers\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("href=\"/admin/dashboard\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("href=\"/portal\""))));

        MockHttpSession customer = login("finance@northstar.example", "/portal");
        mockMvc.perform(get("/portal").session(customer))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/portal/invoices\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("href=\"/admin/dashboard\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("href=\"/staff/dashboard\""))));

        assertNotEquals(admin.getId(), staff.getId());
        assertNotEquals(staff.getId(), customer.getId());
        assertNotEquals(admin.getId(), customer.getId());
    }

    @Test
    void roleQueryCannotChangeAuthenticatedRoleAndUnauthorizedRoutesAreForbidden() throws Exception {
        MockHttpSession staff = login("staff@brightloop.example", "/staff/dashboard", "ADMIN");
        mockMvc.perform(get("/staff/dashboard").session(staff))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(">STAFF</span>")));
        mockMvc.perform(get("/admin/dashboard").session(staff)).andExpect(status().isForbidden());
        mockMvc.perform(get("/portal").session(staff)).andExpect(status().isForbidden());

        MockHttpSession admin = login("admin@brightloop.example", "/admin/dashboard", "CUSTOMER");
        mockMvc.perform(get("/staff/dashboard").session(admin)).andExpect(status().isForbidden());
        mockMvc.perform(get("/portal").session(admin)).andExpect(status().isForbidden());

        MockHttpSession customer = login("maya.kapoor@example.com", "/portal", "ADMIN");
        mockMvc.perform(get("/admin/dashboard").session(customer)).andExpect(status().isForbidden());
        mockMvc.perform(get("/staff/dashboard").session(customer)).andExpect(status().isForbidden());
        mockMvc.perform(get("/invoices").session(customer)).andExpect(status().isForbidden());
    }

    @Test
    void customerCanOnlySeeTheirOwnPortalRecordsAndPdf() throws Exception {
        MockHttpSession northstar = login("finance@northstar.example", "/portal");
        mockMvc.perform(get("/portal/invoices/1").session(northstar)).andExpect(status().isOk());
        var pdf = mockMvc.perform(get("/portal/invoices/1/pdf").session(northstar))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        org.junit.jupiter.api.Assertions.assertTrue(
                containsEmbeddedImage(pdf),
                "The downloaded customer invoice PDF should embed the SubscriptIQ logo.");
        mockMvc.perform(get("/portal/invoices/2").session(northstar)).andExpect(status().isNotFound());
        mockMvc.perform(get("/portal/invoices/2/pdf").session(northstar)).andExpect(status().isNotFound());
        mockMvc.perform(get("/portal/subscriptions/2").session(northstar)).andExpect(status().isNotFound());

        MockHttpSession maya = login("maya.kapoor@example.com", "/portal");
        mockMvc.perform(get("/portal/invoices/2").session(maya)).andExpect(status().isOk());
        mockMvc.perform(get("/portal/invoices/1").session(maya)).andExpect(status().isNotFound());
    }

    @Test
    void permittedRolePagesContinueToRender() throws Exception {
        assertPages(login("admin@brightloop.example", "/admin/dashboard"),
                "/admin/dashboard", "/admin/finance", "/admin/products", "/admin/plans", "/admin/taxes",
                "/admin/discounts", "/admin/staff", "/admin/clock", "/customers", "/subscriptions",
                "/invoices", "/payments", "/billing", "/collections", "/invoices/1/pdf");
        MockHttpSession invoiceSession = login("admin@brightloop.example", "/admin/dashboard");
        mockMvc.perform(get("/invoices/1").session(invoiceSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "src=\"/images/subscriptiq-logo.png\"")));
        mockMvc.perform(get("/invoices/1/print").session(invoiceSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "src=\"/images/subscriptiq-logo.png\"")));
        var pdf = mockMvc.perform(get("/invoices/1/pdf").session(invoiceSession))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        org.junit.jupiter.api.Assertions.assertTrue(
                containsEmbeddedImage(pdf),
                "The downloaded invoice PDF should embed the SubscriptIQ logo.");
        assertPages(login("staff@brightloop.example", "/staff/dashboard"),
                "/staff/dashboard", "/customers", "/subscriptions", "/invoices", "/payments",
                "/billing", "/collections");
        assertPages(login("finance@northstar.example", "/portal"),
                "/portal", "/portal/subscriptions", "/portal/subscriptions/1", "/portal/invoices",
                "/portal/invoices/1", "/portal/invoices/1/pdf");
    }

    @Test
    void sharedHeaderUsesAuthenticatedProfileAndExistingDestinations() throws Exception {
        MockHttpSession admin = login("admin@brightloop.example", "/admin/dashboard");
        mockMvc.perform(get("/admin/dashboard").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Alex Morgan")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(">ADMIN</small>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/search\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/collections\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("mailto:billing@brightloop.example")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/customers/new\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/logout\"")));

        MockHttpSession customer = login("maya.kapoor@example.com", "/portal");
        mockMvc.perform(get("/portal").session(customer))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Maya Kapoor")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/search\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/portal/invoices\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Create</span>"))));
    }

    @Test
    void logoutInvalidatesTheAuthenticatedSession() throws Exception {
        MockHttpSession admin = login("admin@brightloop.example", "/admin/dashboard");
        mockMvc.perform(post("/logout").session(admin).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?logout=true"));
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("You have been signed out.")));
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login")));
    }

    @Test
    void invalidCredentialsDoNotCreateAnAuthenticatedSession() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "staff@brightloop.example")
                        .param("password", "incorrect"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?error=true"));
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Email or password was not recognised.")));
        mockMvc.perform(get("/staff/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login")));
    }

    @Test
    void publicRegistrationAlwaysCreatesHashedCustomerAccount() throws Exception {
        String email = "new-customer@example.test";
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "New Customer")
                        .param("email", email)
                        .param("password", TEST_PASSWORD)
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login"));

        UserAccount account = accounts.findByEmailIgnoreCase(email).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(AccountRole.CUSTOMER, account.getRole());
        org.junit.jupiter.api.Assertions.assertNotEquals(TEST_PASSWORD, account.getPasswordHash());

        MvcResult result = mockMvc.perform(post("/login").with(csrf())
                        .param("username", email).param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/portal"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        mockMvc.perform(get("/portal").session(session)).andExpect(status().isOk());
    }

    @Test
    void registrationRejectsWeakPasswordsAndDuplicateEmails() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Weak Customer")
                        .param("email", "weak-customer@example.test")
                        .param("password", "short"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Create your account")));
        org.junit.jupiter.api.Assertions.assertTrue(
                accounts.findByEmailIgnoreCase("weak-customer@example.test").isEmpty());

        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Duplicate Customer")
                        .param("email", "FINANCE@NORTHSTAR.EXAMPLE")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "An account with this email already exists.")));
        org.junit.jupiter.api.Assertions.assertEquals(1,
                accounts.findAll().stream()
                        .filter(account -> account.getEmail().equals("finance@northstar.example"))
                        .count());
    }

    private MockHttpSession login(String email, String dashboard) throws Exception {
        return login(email, dashboard, null);
    }

    private MockHttpSession login(String email, String dashboard, String untrustedRole) throws Exception {
        var request = post("/login")
                .with(csrf())
                .param("username", email)
                .param("password", TEST_PASSWORD);
        if (untrustedRole != null) {
            request.param("role", untrustedRole);
        }
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", dashboard))
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        return session;
    }

    private void assertPages(MockHttpSession session, String... paths) throws Exception {
        for (String path : paths) {
            mockMvc.perform(get(path).session(session)).andExpect(status().isOk());
        }
    }

    private boolean containsEmbeddedImage(byte[] pdfBytes) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        try {
            PdfDictionary resources = reader.getPageN(1).getAsDict(PdfName.RESOURCES);
            PdfDictionary xObjects = resources == null ? null : resources.getAsDict(PdfName.XOBJECT);
            if (xObjects == null) {
                return false;
            }
            return xObjects.getKeysAndValues().stream()
                    .map(entry -> PdfReader.getPdfObject(entry.getValue()))
                    .filter(PdfDictionary.class::isInstance)
                    .map(PdfDictionary.class::cast)
                    .anyMatch(image -> PdfName.IMAGE.equals(image.getAsName(PdfName.SUBTYPE)));
        } finally {
            reader.close();
        }
    }
}
