package com.subscriptiq;

import com.subscriptiq.account.AccountRole;
import com.subscriptiq.account.UserAccount;
import com.subscriptiq.account.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:subscriptiq-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
abstract class AccountIntegrationTestBase {
    protected static final String TEST_PASSWORD = "SubscriptIQ!2026";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserAccountRepository accounts;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void resetAccounts() {
        accounts.deleteAll();
        create("admin@brightloop.example", "Alex Morgan", AccountRole.ADMIN);
        create("staff@brightloop.example", "Samira Patel", AccountRole.STAFF);
        create("jordan@brightloop.example", "Jordan Lee", AccountRole.STAFF);
        create("finance@northstar.example", "Northstar Labs", AccountRole.CUSTOMER);
        create("maya.kapoor@example.com", "Maya Kapoor", AccountRole.CUSTOMER);
        create("accounts@orbit.example", "Orbit Works", AccountRole.CUSTOMER);
    }

    protected UserAccount create(String email, String name, AccountRole role) {
        return accounts.save(new UserAccount(email, name, passwordEncoder.encode(TEST_PASSWORD), role));
    }
}
