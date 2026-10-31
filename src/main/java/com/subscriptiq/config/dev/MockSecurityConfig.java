package com.subscriptiq.config.dev;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("mock")
public class MockSecurityConfig {
    @Bean
    PasswordEncoder mockPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecurityFilterChain mockSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/vendor/**",
                                "/error", "/error/**").permitAll()
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/staff", "/staff/**").hasRole("STAFF")
                        .requestMatchers("/portal", "/portal/**").hasRole("CUSTOMER")
                        .requestMatchers("/billing", "/billing/**", "/collections", "/collections/**")
                        .hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/customers", "/customers/**", "/subscriptions", "/subscriptions/**",
                                "/invoices", "/invoices/**", "/payments", "/payments/**")
                        .hasAnyRole("ADMIN", "STAFF")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(new RoleBasedAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                .sessionManagement(session -> session.sessionFixation().changeSessionId())
                .build();
    }
}
