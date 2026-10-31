package com.subscriptiq.controller.dev;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@Profile("mock")
public class MockSearchPages {
    @GetMapping("/search")
    String search(@RequestParam(required = false) String q, Authentication authentication, Model model) {
        List<Map<String, Object>> results = new ArrayList<>();
        boolean customerRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CUSTOMER"));
        List<Map<String, Object>> visibleCustomers = customerRole
                ? MockData.customers().stream()
                .filter(customer -> customer.get("email").equals(authentication.getName()))
                .toList()
                : MockData.customers();

        if (q != null && !q.isBlank()) {
            if (!customerRole) {
                visibleCustomers.stream()
                        .filter(customer -> MockSearch.matches(q, customer.get("name"), customer.get("company"),
                                customer.get("email"), customer.get("phone")))
                        .forEach(customer -> results.add(result("Customer", customer.get("name"),
                                customer.get("company") + " · " + customer.get("email"),
                                "/customers/" + customer.get("id"))));
            }

            List<Map<String, Object>> visibleSubscriptions = customerRole
                    ? MockData.subscriptions().stream()
                    .filter(subscription -> belongsToVisibleCustomer(subscription.get("customer"), visibleCustomers))
                    .toList()
                    : MockData.subscriptions();
            visibleSubscriptions.stream()
                    .filter(subscription -> MockSearch.matches(q, subscription.get("number"),
                            ((Map<?, ?>) subscription.get("customer")).get("name"),
                            ((Map<?, ?>) subscription.get("customer")).get("company"),
                            ((Map<?, ?>) subscription.get("plan")).get("name"), subscription.get("status")))
                    .forEach(subscription -> results.add(result("Subscription", subscription.get("number"),
                            ((Map<?, ?>) subscription.get("customer")).get("name") + " · "
                                    + ((Map<?, ?>) subscription.get("plan")).get("name"),
                                    (customerRole ? "/portal/subscriptions/" : "/subscriptions/") + subscription.get("id"))));

            List<Map<String, Object>> visibleInvoices = customerRole
                    ? MockData.invoices().stream()
                    .filter(invoice -> belongsToVisibleCustomer(invoice.get("customer"), visibleCustomers))
                    .toList()
                    : MockData.invoices();
            visibleInvoices.stream()
                    .filter(invoice -> MockSearch.matches(q, invoice.get("number"), invoice.get("status"),
                            ((Map<?, ?>) invoice.get("customer")).get("name"),
                            ((Map<?, ?>) invoice.get("customer")).get("company"),
                            ((Map<?, ?>) invoice.get("customer")).get("email")))
                    .forEach(invoice -> results.add(result("Invoice", invoice.get("number"),
                            ((Map<?, ?>) invoice.get("customer")).get("name") + " · "
                                    + invoice.get("status"),
                            (customerRole ? "/portal/invoices/" : "/invoices/") + invoice.get("id"))));
        }

        model.addAttribute("q", q);
        model.addAttribute("results", results);
        return "search/results";
    }

    private boolean belongsToVisibleCustomer(Object customer, List<Map<String, Object>> visibleCustomers) {
        Object customerId = ((Map<?, ?>) customer).get("id");
        return visibleCustomers.stream().anyMatch(visible -> visible.get("id").equals(customerId));
    }

    private Map<String, Object> result(String type, Object title, Object detail, String href) {
        return Map.of("type", type, "title", title, "detail", detail, "href", href);
    }
}
