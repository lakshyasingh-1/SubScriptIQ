package com.subscriptiq.controller.dev;

import com.subscriptiq.account.AccountRole;
import com.subscriptiq.account.PrivilegedAccountForm;
import com.subscriptiq.account.UserAccount;
import com.subscriptiq.account.UserAccountRepository;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@Profile("mock")
@RequestMapping("/admin")
public class MockAdminPages {
    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;

    public MockAdminPages(UserAccountRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    String root() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    String dashboard(Model model) {
        model.addAttribute("setup", Map.of("productCount", 3, "planCount", 4, "taxCount", 2,
                "discountCount", 3, "staffCount", 5));
        model.addAttribute("summary", Map.of("activeSubscriptions", 128, "mrr", MockData.amount("482500.00"),
                "outstanding", MockData.amount("87400.00")));
        return "admin/dashboard";
    }

    @GetMapping("/products")
    String products(Model model, jakarta.servlet.http.HttpServletRequest request) {
        String query = request.getParameter("q");
        List<Map<String, Object>> products = MockData.products().stream()
                .filter(product -> MockSearch.matches(query, product.get("name"), product.get("type"),
                        product.get("notes"), ((Map<?, ?>) product.get("tax")).get("name")))
                .toList();
        model.addAttribute("page", MockData.page(products, "1".equals(request.getParameter("empty")),
                MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("q", query);
        model.addAttribute("status", request.getParameter("status"));
        return "admin/products";
    }

    @GetMapping("/products/new")
    String productForm(Model model) {
        model.addAttribute("form", MockData.form());
        model.addAttribute("types", List.of("SOFTWARE", "ADD_ON", "SERVICE"));
        model.addAttribute("taxes", List.of(Map.of("id", 1, "name", "GST 18%", "ratePercent", 18)));
        model.addAttribute("variants", List.of());
        return "admin/product-form";
    }

    @GetMapping("/products/{id}/edit")
    String editProduct(@PathVariable long id, Model model) {
        Map<String, Object> product = MockData.products().get((int) ((id - 1) % MockData.products().size()));
        MockData.Form form = MockData.form();
        form.setName((String) product.get("name"));
        form.setType((String) product.get("type"));
        form.setSalesPrice((java.math.BigDecimal) product.get("salesPrice"));
        form.setCostPrice((java.math.BigDecimal) product.get("costPrice"));
        form.setNotes((String) product.get("notes"));
        form.setActive((Boolean) product.get("active"));
        model.addAttribute("form", form);
        model.addAttribute("types", List.of("SOFTWARE", "ADD_ON", "SERVICE"));
        model.addAttribute("taxes", List.of(Map.of("id", 1, "name", "GST 18%", "ratePercent", 18)));
        model.addAttribute("variants", List.of());
        return "admin/product-form";
    }

    @GetMapping("/plans")
    String plans(Model model, jakarta.servlet.http.HttpServletRequest request) {
        List<Map<String, Object>> plans = List.of(
                MockData.plan(1, "Professional monthly", MockData.products().get(0), "10000.00"),
                MockData.plan(2, "Starter monthly", MockData.products().get(0), "4500.00"),
                MockData.plan(3, "Priority support", MockData.products().get(1), "850.00"));
        String query = request.getParameter("q");
        plans = plans.stream()
                .filter(plan -> MockSearch.matches(query, plan.get("name"), plan.get("billingPeriod"),
                        ((Map<?, ?>) plan.get("product")).get("name")))
                .toList();
        model.addAttribute("page", MockData.page(plans, "1".equals(request.getParameter("empty")),
                MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("q", query);
        return "admin/plans";
    }

    @GetMapping("/plans/new")
    String planForm(Model model) {
        MockData.Form form = MockData.form();
        form.setBillingPeriod("MONTHLY");
        model.addAttribute("form", form);
        model.addAttribute("products", MockData.products());
        model.addAttribute("periods", List.of("DAILY", "WEEKLY", "MONTHLY", "YEARLY"));
        return "admin/plan-form";
    }

    @GetMapping("/taxes")
    String taxes(Model model, jakarta.servlet.http.HttpServletRequest request) {
        List<Map<String, Object>> taxes = List.of(
                Map.of("id", 1, "name", "GST", "ratePercent", MockData.amount("18.00"), "active", true),
                Map.of("id", 2, "name", "GST Reduced", "ratePercent", MockData.amount("5.00"), "active", true));
        String query = request.getParameter("q");
        taxes = taxes.stream()
                .filter(tax -> MockSearch.matches(query, tax.get("name"), tax.get("ratePercent")))
                .toList();
        model.addAttribute("page", MockData.page(taxes, "1".equals(request.getParameter("empty")),
                MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("q", query);
        return "admin/taxes";
    }

    @GetMapping("/taxes/new")
    String taxForm(Model model) {
        model.addAttribute("form", MockData.form());
        return "admin/tax-form";
    }

    @GetMapping("/discounts")
    String discounts(Model model, jakarta.servlet.http.HttpServletRequest request) {
        List<Map<String, Object>> discounts = List.of(
                Map.ofEntries(Map.entry("id", 1), Map.entry("name", "Launch offer"), Map.entry("type", "PERCENTAGE"),
                        Map.entry("value", 10), Map.entry("scope", "SUBSCRIPTION"),
                        Map.entry("minPurchase", MockData.amount("0.00")), Map.entry("minQuantity", 1),
                        Map.entry("startDate", LocalDate.now().minusMonths(1)), Map.entry("endDate", LocalDate.now().plusMonths(1)),
                        Map.entry("usageLimit", 100), Map.entry("usedCount", 42), Map.entry("products", List.of()),
                        Map.entry("active", true)),
                Map.ofEntries(Map.entry("id", 2), Map.entry("name", "Analytics bundle"), Map.entry("type", "FIXED"),
                        Map.entry("value", MockData.amount("500.00")), Map.entry("scope", "PRODUCT"),
                        Map.entry("minPurchase", MockData.amount("1000.00")), Map.entry("minQuantity", 2),
                        Map.entry("startDate", LocalDate.now().minusDays(5)), Map.entry("endDate", LocalDate.now().plusDays(20)),
                        Map.entry("usageLimit", 50), Map.entry("usedCount", 18),
                        Map.entry("products", MockData.products().subList(0, 1)), Map.entry("active", true)));
        String query = request.getParameter("q");
        discounts = discounts.stream()
                .filter(discount -> MockSearch.matches(query, discount.get("name"), discount.get("type"),
                        discount.get("scope")))
                .toList();
        model.addAttribute("page", MockData.page(discounts, "1".equals(request.getParameter("empty")),
                MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("q", query);
        return "admin/discounts";
    }

    @GetMapping("/discounts/new")
    String discountForm(Model model) {
        MockData.Form form = MockData.form();
        form.setType("PERCENTAGE");
        form.setScope("SUBSCRIPTION");
        model.addAttribute("form", form);
        model.addAttribute("products", MockData.products());
        model.addAttribute("types", List.of("PERCENTAGE", "FIXED"));
        model.addAttribute("scopes", List.of("PRODUCT", "SUBSCRIPTION"));
        return "admin/discount-form";
    }

    @GetMapping("/staff")
    String staff(Model model, jakarta.servlet.http.HttpServletRequest request) {
        String query = request.getParameter("q");
        List<UserAccount> team = accounts.findAllByRoleInOrderByFullNameAsc(
                        List.of(AccountRole.STAFF, AccountRole.ADMIN)).stream()
                .filter(member -> MockSearch.matches(query, member.getFullName(), member.getEmail(),
                        member.getRole().name()))
                .toList();
        model.addAttribute("staff", "1".equals(request.getParameter("empty")) ? List.of() : team);
        model.addAttribute("q", query);
        return "admin/staff";
    }

    @GetMapping("/staff/new")
    String staffForm(Model model) {
        model.addAttribute("form", new PrivilegedAccountForm());
        model.addAttribute("roles", List.of(AccountRole.STAFF, AccountRole.ADMIN));
        return "admin/staff-form";
    }

    @PostMapping("/staff")
    String createStaff(@Valid @ModelAttribute("form") PrivilegedAccountForm form,
                       BindingResult bindingResult, Model model, RedirectAttributes redirect) {
        String normalizedEmail = form.getEmail() == null ? "" : form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (form.getRole() == AccountRole.CUSTOMER) {
            bindingResult.rejectValue("role", "invalid", "Only Staff or Admin accounts can be provisioned here.");
        }
        if (!normalizedEmail.isBlank() && accounts.existsByEmailIgnoreCase(normalizedEmail)) {
            bindingResult.rejectValue("email", "duplicate", "An account with this email already exists.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", List.of(AccountRole.STAFF, AccountRole.ADMIN));
            return "admin/staff-form";
        }
        accounts.save(new UserAccount(normalizedEmail, form.getFullName().trim(),
                passwordEncoder.encode(form.getPassword()), form.getRole()));
        redirect.addFlashAttribute("success", "Team account created.");
        return "redirect:/admin/staff";
    }

    @GetMapping("/clock")
    String clock(HttpSession session, Model model) {
        Integer offset = (Integer) session.getAttribute("mockClockOffset");
        int days = offset == null ? 0 : offset;
        model.addAttribute("offsetDays", days);
        model.addAttribute("simulatedDate", LocalDate.now().plusDays(days));
        model.addAttribute("clockSteps", List.of(1, 7, 30));
        return "admin/clock";
    }

    @PostMapping("/clock/advance/{days}")
    String advanceClock(@PathVariable int days, HttpSession session, RedirectAttributes redirect) {
        Integer offset = (Integer) session.getAttribute("mockClockOffset");
        session.setAttribute("mockClockOffset", (offset == null ? 0 : offset) + days);
        redirect.addFlashAttribute("success", "Demo clock advanced by " + days + " days.");
        return "redirect:/admin/clock";
    }

    @PostMapping("/clock/reset")
    String resetClock(HttpSession session, RedirectAttributes redirect) {
        session.setAttribute("mockClockOffset", 0);
        redirect.addFlashAttribute("success", "Demo clock reset.");
        return "redirect:/admin/clock";
    }

    @PostMapping({"/products/**", "/plans/**", "/taxes/**", "/discounts/**"})
    String save(RedirectAttributes redirect) {
        redirect.addFlashAttribute("success", "Demo changes are not persisted.");
        return "redirect:/admin/dashboard";
    }
}
