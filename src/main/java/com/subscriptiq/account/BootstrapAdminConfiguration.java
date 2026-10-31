package com.subscriptiq.account;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration
@Profile("mock")
public class BootstrapAdminConfiguration {
    @Bean
    ApplicationRunner bootstrapAdmin(UserAccountRepository accounts, PasswordEncoder passwordEncoder,
                                     Environment environment) {
        return arguments -> {
            String email = environment.getProperty("SUBSCRIPTIQ_BOOTSTRAP_ADMIN_EMAIL");
            String password = environment.getProperty("SUBSCRIPTIQ_BOOTSTRAP_ADMIN_PASSWORD");
            if ((email == null || email.isBlank()) && (password == null || password.isBlank())) {
                return;
            }
            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                throw new IllegalStateException("Both SUBSCRIPTIQ_BOOTSTRAP_ADMIN_EMAIL and "
                        + "SUBSCRIPTIQ_BOOTSTRAP_ADMIN_PASSWORD must be set together.");
            }
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            if (accounts.existsByEmailIgnoreCase(normalizedEmail)) {
                return;
            }
            if (password.length() < 9 || password.length() > 72
                    || password.chars().noneMatch(Character::isUpperCase)
                    || password.chars().noneMatch(Character::isLowerCase)
                    || password.chars().noneMatch(character -> !Character.isLetterOrDigit(character))) {
                throw new IllegalStateException("Bootstrap admin password must meet the account password policy.");
            }
            String name = environment.getProperty("SUBSCRIPTIQ_BOOTSTRAP_ADMIN_NAME", "Initial Admin");
            accounts.save(new UserAccount(normalizedEmail, name.trim(),
                    passwordEncoder.encode(password), AccountRole.ADMIN));
        };
    }
}
