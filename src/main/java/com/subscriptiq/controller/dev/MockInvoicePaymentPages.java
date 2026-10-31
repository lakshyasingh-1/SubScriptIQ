package com.subscriptiq.controller.dev;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@Profile("mock")
public class MockInvoicePaymentPages {
    @GetMapping("/invoices")
    String invoices(Model model, HttpServletRequest request, HttpSession session) {
        populateInvoiceResults(model, request, session);
        return "invoices/list";
    }

    @GetMapping("/invoices/rows")
    String invoiceRows(Model model, HttpServletRequest request, HttpSession session) {
        populateInvoiceResults(model, request, session);
        return "invoices/list :: rows";
    }

    @GetMapping("/invoices/{id}")
    String invoiceDetail(@PathVariable long id, Model model) {
        model.addAttribute("invoice", MockData.invoices().get((int) ((id - 1) % MockData.invoices().size())));
        model.addAttribute("payments", paymentExamples());
        model.addAttribute("actions", List.of("confirm", "cancel", "send", "print", "pdf", "add-payment"));
        return "invoices/detail";
    }

    @GetMapping("/invoices/{id}/print")
    String invoicePrint(@PathVariable long id, Model model) {
        model.addAttribute("invoice", MockData.invoices().get((int) ((id - 1) % MockData.invoices().size())));
        model.addAttribute("company", Map.of("name", "Brightloop Software",
                "address", "42 Residency Road, Bengaluru, Karnataka 560025",
                "email", "billing@brightloop.example", "phone", "+91 80 5555 0101"));
        return "invoices/print";
    }

    @GetMapping("/payments")
    String payments(Model model, HttpServletRequest request) {
        String query = request.getParameter("q");
        List<Map<String, Object>> payments = paymentExamples().stream()
                .filter(payment -> {
                    Map<?, ?> invoice = (Map<?, ?>) payment.get("invoice");
                    Map<?, ?> customer = (Map<?, ?>) invoice.get("customer");
                    return MockSearch.matches(query, payment.get("id"), payment.get("method"),
                            payment.get("note"), invoice.get("number"), customer.get("name"),
                            customer.get("company"), customer.get("email"));
                }).toList();
        model.addAttribute("page", MockData.page(payments, "1".equals(request.getParameter("empty")),
                MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("q", query);
        return "payments/list";
    }

    @GetMapping("/payments/new")
    String paymentForm(Model model, HttpServletRequest request) {
        MockData.Form form = MockData.form();
        form.setInvoiceId(request.getParameter("invoiceId") == null ? "" : request.getParameter("invoiceId"));
        model.addAttribute("form", form);
        model.addAttribute("customers", MockData.customers());
        model.addAttribute("methods", List.of("CASH", "BANK_TRANSFER", "UPI", "CARD", "CHEQUE"));
        model.addAttribute("outstandingInvoices", MockData.invoices().stream()
                .filter(invoice -> !"PAID".equals(invoice.get("status")) && !"CANCELLED".equals(invoice.get("status")))
                .toList());
        return "payments/form";
    }

    @GetMapping("/payments/outstanding")
    String outstandingInvoices(Model model, HttpServletRequest request) {
        model.addAttribute("form", MockData.form());
        String customerId = request.getParameter("customerId");
        List<Map<String, Object>> outstanding = "1".equals(request.getParameter("empty")) ? List.of()
                : MockData.invoices().stream()
                .filter(invoice -> !"PAID".equals(invoice.get("status")) && !"CANCELLED".equals(invoice.get("status")))
                .filter(invoice -> customerId == null || customerId.isBlank()
                        || customerId.equals(((Map<?, ?>) invoice.get("customer")).get("id").toString()))
                .toList();
        model.addAttribute("outstandingInvoices", outstanding);
        return "payments/form :: outstandingList";
    }

    private List<Map<String, Object>> filteredInvoices(HttpServletRequest request, HttpSession session) {
        String query = request.getParameter("q");
        String status = request.getParameter("status");
        boolean overdueOnly = "true".equals(request.getParameter("overdueOnly"));
        Integer offset = (Integer) session.getAttribute("mockClockOffset");
        java.time.LocalDate today = MockData.TODAY.plusDays(offset == null ? 0 : offset);
        return MockData.invoices().stream()
                .filter(invoice -> status == null || status.isBlank() || status.equals(invoice.get("status")))
                .filter(invoice -> MockSearch.matches(query, invoice.get("number"),
                        ((Map<?, ?>) invoice.get("customer")).get("name"),
                        ((Map<?, ?>) invoice.get("customer")).get("company"),
                        ((Map<?, ?>) invoice.get("customer")).get("email")))
                .filter(invoice -> !overdueOnly || ("CONFIRMED".equals(invoice.get("status"))
                        && MockData.amount("0.00").compareTo((java.math.BigDecimal) invoice.get("outstanding")) < 0
                        && ((java.time.LocalDate) invoice.get("dueDate")).isBefore(today)))
                .toList();
    }

    private void populateInvoiceResults(Model model, HttpServletRequest request, HttpSession session) {
        model.addAttribute("page", MockData.page(filteredInvoices(request, session),
                "1".equals(request.getParameter("empty")), MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("q", request.getParameter("q"));
        model.addAttribute("status", request.getParameter("status"));
        model.addAttribute("overdueOnly", "true".equals(request.getParameter("overdueOnly")));
    }

    private List<Map<String, Object>> paymentExamples() {
        return List.of(
                Map.of("id", 1, "invoice", MockData.invoices().get(0), "method", "UPI",
                        "amount", MockData.amount("2000.00"), "paidOn", MockData.TODAY.minusDays(2), "note", "Part payment"),
                Map.of("id", 2, "invoice", MockData.invoices().get(1), "method", "BANK_TRANSFER",
                        "amount", MockData.amount("5900.00"), "paidOn", MockData.TODAY.minusDays(4), "note", "Settlement"));
    }

    @PostMapping("/invoices/{id}/confirm")
    String confirm(@PathVariable long id, RedirectAttributes redirect) {
        return invoiceAction(id, "Invoice confirmed in demo mode.", redirect);
    }

    @PostMapping({"/invoices/{id}/cancel", "/invoices/{id}/send"})
    String invoiceAction(@PathVariable long id, RedirectAttributes redirect) {
        return invoiceAction(id, "Demo action completed.", redirect);
    }

    private String invoiceAction(long id, String message, RedirectAttributes redirect) {
        redirect.addFlashAttribute("success", message);
        return "redirect:/invoices/" + id;
    }

    @PostMapping("/payments")
    String savePayment(RedirectAttributes redirect) {
        redirect.addFlashAttribute("success", "Demo payment recorded for preview only.");
        return "redirect:/payments";
    }
}
