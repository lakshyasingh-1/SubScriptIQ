package com.subscriptiq.controller.dev;

import com.subscriptiq.account.UserAccount;
import com.subscriptiq.account.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@Profile("mock")
public class MockCustomerSubscriptionPages {
    private final UserAccountRepository accounts;

    public MockCustomerSubscriptionPages(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @GetMapping("/customers")
    String customers(Model model, HttpServletRequest request) {
        String query = request.getParameter("q");
        List<Map<String, Object>> customers = MockData.customers().stream()
                .filter(customer -> MockSearch.matches(query, customer.get("name"), customer.get("company"),
                        customer.get("email"), customer.get("phone")))
                .toList();
        model.addAttribute("page", MockData.page(customers, "1".equals(request.getParameter("empty")),
                MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("q", query);
        return "customers/list";
    }

    @GetMapping("/customers/new")
    String customerForm(Model model) {
        model.addAttribute("form", MockData.form());
        return "customers/form";
    }

    @GetMapping("/customers/{id}/edit")
    String editCustomer(@PathVariable long id, Model model) {
        Map<String, Object> customer = MockData.customers().get((int) ((id - 1) % MockData.customers().size()));
        MockData.Form form = MockData.form();
        form.setName((String) customer.get("name"));
        form.setEmail((String) customer.get("email"));
        form.setPhone((String) customer.get("phone"));
        form.setCompany((String) customer.get("company"));
        form.setAddress((String) customer.get("address"));
        model.addAttribute("form", form);
        return "customers/form";
    }

    @GetMapping("/customers/{id}")
    String customerDetail(@PathVariable long id, Model model) {
        Map<String, Object> customer = MockData.customers().get((int) ((id - 1) % MockData.customers().size()));
        model.addAttribute("customer", customer);
        model.addAttribute("subscriptions", MockData.subscriptions().subList(0, 2));
        model.addAttribute("invoices", MockData.invoices().subList(0, 2));
        return "customers/detail";
    }

    @GetMapping("/subscriptions")
    String subscriptions(Model model, HttpServletRequest request) {
        List<Map<String, Object>> subscriptions = MockData.subscriptions();
        String query = request.getParameter("q");
        subscriptions = subscriptions.stream()
                .filter(item -> MockSearch.matches(query, item.get("number"),
                        ((Map<?, ?>) item.get("customer")).get("name"),
                        ((Map<?, ?>) item.get("customer")).get("company"),
                        ((Map<?, ?>) item.get("customer")).get("email"),
                        ((Map<?, ?>) item.get("plan")).get("name")))
                .toList();
        String selectedStatus = request.getParameter("status");
        if (selectedStatus != null && !selectedStatus.isBlank()) {
            subscriptions = subscriptions.stream()
                    .filter(item -> selectedStatus.equals(item.get("status"))).toList();
        }
        model.addAttribute("page", MockData.page(subscriptions, "1".equals(request.getParameter("empty")),
                MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("status", selectedStatus);
        model.addAttribute("q", query);
        return "subscriptions/list";
    }

    @GetMapping("/subscriptions/new")
    String subscriptionForm(Model model) {
        model.addAttribute("form", MockData.form());
        model.addAttribute("customers", MockData.customers());
        model.addAttribute("plans", List.of(MockData.plan(1, "Professional monthly",
                MockData.products().get(0), "10000.00")));
        return "subscriptions/form";
    }

    @GetMapping("/subscriptions/{id}")
    String subscriptionDetail(@PathVariable long id, Model model) {
        Map<String, Object> subscription = MockData.subscriptions().get((int) ((id - 1) % MockData.subscriptions().size()));
        populateSubscription(model, subscription);
        return "subscriptions/detail";
    }

    @PostMapping("/subscriptions/{id}/lines")
    String subscriptionLines(@PathVariable long id, HttpServletRequest request, Model model) {
        Map<String, Object> subscription = MockData.subscriptions().get((int) ((id - 1) % MockData.subscriptions().size()));
        populateSubscription(model, subscription);
        if (request.getParameter("removeLineId") != null) {
            model.addAttribute("lines", List.of());
        } else if (request.getParameter("productId") != null) {
            Map<String, Object> product = MockData.products().get(
                    Math.floorMod(Integer.parseInt(request.getParameter("productId")) - 1, MockData.products().size()));
            Map<String, Object> addedLine = new java.util.LinkedHashMap<>();
            addedLine.put("id", 2);
            addedLine.put("product", product);
            addedLine.put("variant", null);
            addedLine.put("quantity", 1);
            addedLine.put("unitPrice", product.get("salesPrice"));
            addedLine.put("taxRatePercent", MockData.amount("18.00"));
            addedLine.put("amount", product.get("salesPrice"));
            model.addAttribute("lines", List.of(addedLine));
        }
        model.addAttribute("products", MockData.products());
        return "subscriptions/detail :: linesPanel";
    }

    private void populateSubscription(Model model, Map<String, Object> subscription) {
        model.addAttribute("subscription", subscription);
        model.addAttribute("lines", List.of(Map.of("id", 1, "product", MockData.products().get(0),
                "variant", Map.of("id", 1, "attribute", "Users", "value", "5 seats", "extraPrice", 0),
                "quantity", 5, "unitPrice", MockData.amount("2000.00"),
                "taxRatePercent", MockData.amount("18.00"), "amount", MockData.amount("10000.00"))));
        model.addAttribute("invoices", MockData.invoices().subList(0, 2));
        model.addAttribute("products", MockData.products());
        model.addAttribute("actions", List.of("send-quotation", "confirm", "activate", "pause", "resume", "close", "renew"));
    }

    @GetMapping("/portal")
    String portalHome(Authentication authentication, Model model) {
        Map<String, Object> customer = customerFor(authentication);
        model.addAttribute("customer", customer);
        model.addAttribute("subscriptions", subscriptionsForCustomer(customer));
        model.addAttribute("openInvoices", invoicesForCustomer(customer).subList(0,
                Math.min(2, invoicesForCustomer(customer).size())));
        return "portal/home";
    }

    @GetMapping("/portal/subscriptions")
    String portalSubscriptions(Authentication authentication, Model model, HttpServletRequest request) {
        String query = request.getParameter("q");
        List<Map<String, Object>> subscriptions = subscriptionsForCustomer(customerFor(authentication)).stream()
                .filter(item -> MockSearch.matches(query, item.get("number"),
                        ((Map<?, ?>) item.get("plan")).get("name"), item.get("status")))
                .toList();
        model.addAttribute("page", MockData.page(subscriptions, "1".equals(request.getParameter("empty")),
                MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("q", query);
        return "portal/subscriptions";
    }

    @GetMapping("/portal/subscriptions/{id}")
    String portalSubscription(@PathVariable long id, Authentication authentication, Model model) {
        Map<String, Object> customer = customerFor(authentication);
        Map<String, Object> subscription = subscriptionForCustomer(id, customer);
        populateSubscription(model, subscription);
        model.addAttribute("invoices", invoicesForCustomer(customer).stream()
                .filter(invoice -> idOf(invoice.get("subscription")) == id)
                .toList());
        return "portal/subscription";
    }

    @GetMapping("/portal/invoices")
    String portalInvoices(Authentication authentication, Model model, HttpServletRequest request) {
        String query = request.getParameter("q");
        List<Map<String, Object>> invoices = invoicesForCustomer(customerFor(authentication)).stream()
                .filter(item -> MockSearch.matches(query, item.get("number"), item.get("status"),
                        ((Map<?, ?>) item.get("subscription")).get("number")))
                .toList();
        model.addAttribute("page", MockData.page(invoices, "1".equals(request.getParameter("empty")),
                MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("q", query);
        return "portal/invoices";
    }

    @GetMapping("/portal/invoices/{id}")
    String portalInvoice(@PathVariable long id, Authentication authentication, Model model) {
        Map<String, Object> customer = customerFor(authentication);
        Map<String, Object> invoice = invoicesForCustomer(customer).stream()
                .filter(item -> idOf(item) == id)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("invoice", invoice);
        model.addAttribute("payments", List.of(Map.of("id", 1, "method", "UPI",
                "amount", MockData.amount("2000.00"), "paidOn", MockData.TODAY.minusDays(2), "note", "Part payment")));
        return "portal/invoice";
    }

    @PostMapping({"/customers/**", "/subscriptions/**"})
    String save(RedirectAttributes redirect) {
        redirect.addFlashAttribute("success", "Demo changes are not persisted.");
        return "redirect:/subscriptions";
    }

    @PostMapping("/subscriptions/{id}/transition/{action}")
    String transition(@PathVariable long id, @PathVariable String action,
                      RedirectAttributes redirect) {
        redirect.addFlashAttribute("success", "Demo action " + action + " accepted.");
        return "redirect:/subscriptions/" + id;
    }

    @PostMapping("/portal/subscriptions/{id}/transition/{action}")
    String portalTransition(@PathVariable long id, @PathVariable String action, Authentication authentication,
                            RedirectAttributes redirect) {
        subscriptionForCustomer(id, customerFor(authentication));
        redirect.addFlashAttribute("success", "Demo action " + action + " accepted.");
        return "redirect:/portal/subscriptions/" + id;
    }

    @PostMapping("/portal/subscriptions/**")
    String savePortalSubscription(RedirectAttributes redirect) {
        redirect.addFlashAttribute("success", "Demo changes are not persisted.");
        return "redirect:/portal/subscriptions";
    }

    private Map<String, Object> customerFor(Authentication authentication) {
        return MockData.customers().stream()
                .filter(customer -> customer.get("email").toString().equalsIgnoreCase(authentication.getName()))
                .findFirst()
                .orElseGet(() -> {
                    UserAccount account = accounts.findByEmailIgnoreCase(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                    return Map.of("id", 0, "name", account.getFullName(), "email", account.getEmail(),
                            "phone", "", "company", account.getFullName(),
                            "address", "No billing address on file.");
                });
    }

    private List<Map<String, Object>> subscriptionsForCustomer(Map<String, Object> customer) {
        return MockData.subscriptions().stream()
                .filter(subscription -> idOf(subscription.get("customer")) == idOf(customer))
                .toList();
    }

    private List<Map<String, Object>> invoicesForCustomer(Map<String, Object> customer) {
        return MockData.invoices().stream()
                .filter(invoice -> idOf(invoice.get("customer")) == idOf(customer))
                .toList();
    }

    private Map<String, Object> subscriptionForCustomer(long id, Map<String, Object> customer) {
        return subscriptionsForCustomer(customer).stream()
                .filter(subscription -> idOf(subscription) == id)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private long idOf(Object value) {
        return Long.parseLong(String.valueOf(((Map<?, ?>) value).get("id")));
    }
}
