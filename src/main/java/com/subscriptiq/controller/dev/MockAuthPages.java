package com.subscriptiq.controller.dev;

import com.subscriptiq.account.AccountRole;
import com.subscriptiq.account.RegistrationForm;
import com.subscriptiq.account.UserAccount;
import com.subscriptiq.account.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.BindingResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Controller
@Profile("mock")
public class MockAuthPages {
    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;

    public MockAuthPages(UserAccountRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    String login() {
        return "auth/login";
    }

    @GetMapping("/")
    String home(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.equals("ROLE_ADMIN")
                        || authority.equals("ROLE_STAFF")
                        || authority.equals("ROLE_CUSTOMER"))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        return switch (role) {
            case "ROLE_CUSTOMER" -> "redirect:/portal";
            case "ROLE_STAFF" -> "redirect:/staff/dashboard";
            case "ROLE_ADMIN" -> "redirect:/admin/dashboard";
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        };
    }

    @GetMapping("/register")
    String register(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult bindingResult,
                    RedirectAttributes redirect) {
        String normalizedEmail = form.getEmail() == null ? "" : form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!normalizedEmail.isBlank() && accounts.existsByEmailIgnoreCase(normalizedEmail)) {
            bindingResult.rejectValue("email", "duplicate", "An account with this email already exists.");
        }
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        accounts.save(new UserAccount(normalizedEmail, form.getFullName().trim(),
                passwordEncoder.encode(form.getPassword()), AccountRole.CUSTOMER));
        redirect.addFlashAttribute("registrationSuccess", true);
        return "redirect:/login";
    }
}
