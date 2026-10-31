package com.subscriptiq.controller.dev;

import com.subscriptiq.account.UserAccount;
import com.subscriptiq.account.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.core.Authentication;
import org.springframework.context.annotation.Profile;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.util.Map;

@ControllerAdvice
@Profile("mock")
public class MockAdvice {
    private final UserAccountRepository accounts;

    public MockAdvice(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @ModelAttribute
    public void addStandardAttributes(HttpServletRequest request, HttpSession session,
                                      Authentication authentication, Model model) {
        Integer offset = (Integer) session.getAttribute("mockClockOffset");
        if (offset == null) {
            offset = 0;
        }
        model.addAttribute("appName", "SubscriptIQ");
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            String currentRole = authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring("ROLE_".length()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Authenticated demo user has no application role."));
            UserAccount account = accounts.findByEmailIgnoreCase(authentication.getName()).orElse(null);
            String email = account == null ? authentication.getName() : account.getEmail();
            String fullName = account == null ? email : account.getFullName();
            model.addAttribute("currentUser", Map.of("fullName", fullName, "email", email));
            model.addAttribute("currentRole", currentRole);
        }
        model.addAttribute("clockOffsetDays", offset);
        model.addAttribute("today", LocalDate.now().plusDays(offset));
        model.addAttribute("activeNav", request.getRequestURI());
        model.addAttribute("demoError", "1".equals(request.getParameter("error")));
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("hxCsrfHeaders", csrfToken == null ? "{}"
                : "{\"" + csrfToken.getHeaderName() + "\":\"" + csrfToken.getToken() + "\"}");
    }
}
