package com.subscriptiq.controller.dev;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@Profile("mock")
public class MockBillingFinancePages {
    @GetMapping("/billing")
    String billing(Model model) {
        model.addAttribute("businessDate", MockData.TODAY);
        model.addAttribute("lastRun", Map.of("runAt", MockData.TODAY.minusDays(1).atTime(2, 0),
                "businessDate", MockData.TODAY.minusDays(1), "createdCount", 12, "skippedCount", 2,
                "failedCount", 0, "notes", "All eligible subscriptions processed."));
        model.addAttribute("recentRuns", List.of(
                Map.of("id", 1, "runAt", MockData.TODAY.minusDays(1).atTime(2, 0),
                        "businessDate", MockData.TODAY.minusDays(1), "createdCount", 12, "skippedCount", 2,
                        "failedCount", 0, "notes", "Completed"),
                Map.of("id", 2, "runAt", MockData.TODAY.minusDays(2).atTime(2, 0),
                        "businessDate", MockData.TODAY.minusDays(2), "createdCount", 0, "skippedCount", 14,
                        "failedCount", 0, "notes", "Nothing billed twice")));
        return "billing/center";
    }

    @PostMapping("/billing/preview")
    String preview(Model model, HttpServletRequest request) {
        model.addAttribute("preview", Map.of("wouldCreate", 12, "totalValue", MockData.amount("141600.00"),
                "problems", List.of()));
        return "billing/center :: previewPanel";
    }

    @PostMapping("/billing/run")
    String run(Model model, HttpSession session) {
        Integer priorRuns = (Integer) session.getAttribute("mockBillingRuns");
        boolean repeatRun = priorRuns != null && priorRuns > 0;
        session.setAttribute("mockBillingRuns", priorRuns == null ? 1 : priorRuns + 1);
        model.addAttribute("result", Map.of("created", repeatRun ? 0 : 12, "skipped", repeatRun ? 14 : 2,
                "failed", 0, "problems", List.of()));
        return "billing/center :: resultPanel";
    }

    @GetMapping("/staff/dashboard")
    String staffDashboard(Model model) {
        model.addAttribute("draftInvoiceCount", 8);
        model.addAttribute("overdueCount", 14);
        model.addAttribute("renewalsDue", MockData.subscriptions().subList(0, 3));
        model.addAttribute("overdueInvoices", MockData.invoices().subList(0, 1));
        return "staff/dashboard";
    }

    @GetMapping("/collections")
    String collections(Model model, HttpServletRequest request, HttpSession session) {
        Integer offset = (Integer) session.getAttribute("mockClockOffset");
        LocalDate today = MockData.TODAY.plusDays(offset == null ? 0 : offset);
        String query = request.getParameter("q");
        List<Map<String, Object>> overdueInvoices = MockData.invoices().stream()
                .filter(invoice -> "CONFIRMED".equals(invoice.get("status")))
                .filter(invoice -> ((java.math.BigDecimal) invoice.get("outstanding")).signum() > 0)
                .filter(invoice -> ((LocalDate) invoice.get("dueDate")).isBefore(today))
                .filter(invoice -> MockSearch.matches(query, invoice.get("number"),
                        ((Map<?, ?>) invoice.get("customer")).get("name"),
                        ((Map<?, ?>) invoice.get("customer")).get("company")))
                .toList();
        model.addAttribute("page", MockData.page(overdueInvoices, "1".equals(request.getParameter("empty")),
                MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("q", query);
        model.addAttribute("aging", List.of(
                Map.of("label", "1–30 days", "count", 8, "amount", MockData.amount("42800.00")),
                Map.of("label", "31–60 days", "count", 3, "amount", MockData.amount("23100.00")),
                Map.of("label", "61–90 days", "count", 1, "amount", MockData.amount("8500.00")),
                Map.of("label", "90+ days", "count", 2, "amount", MockData.amount("13000.00"))));
        return "collections/list";
    }

    @GetMapping("/admin/finance")
    String finance(Model model, HttpServletRequest request) {
        model.addAttribute("summary", Map.ofEntries(
                Map.entry("activeSubscriptions", 128), Map.entry("mrr", MockData.amount("482500.00")),
                Map.entry("arr", MockData.amount("5790000.00")), Map.entry("invoicedRevenue", MockData.amount("624800.00")),
                Map.entry("collected", MockData.amount("537400.00")), Map.entry("outstanding", MockData.amount("87400.00")),
                Map.entry("overdueTotal", MockData.amount("52900.00")), Map.entry("overdueCount", 14)));
        model.addAttribute("aging", List.of(
                Map.of("label", "1–30 days", "count", 8, "amount", MockData.amount("42800.00")),
                Map.of("label", "31–60 days", "count", 3, "amount", MockData.amount("23100.00")),
                Map.of("label", "61–90 days", "count", 1, "amount", MockData.amount("8500.00")),
                Map.of("label", "90+ days", "count", 2, "amount", MockData.amount("13000.00"))));
        model.addAttribute("overdueInvoices", MockData.page(MockData.invoices().subList(0, 1),
                "1".equals(request.getParameter("empty")), MockSearch.pageNumber(request.getParameter("page"))));
        model.addAttribute("chartLabels", List.of("Jun", "Jul", "Aug", "Sep", "Oct", "Nov"));
        model.addAttribute("chartInvoiced", List.of(280000, 310000, 350000, 325000, 410000, 462000));
        model.addAttribute("chartCollected", List.of(245000, 276000, 301000, 289000, 355000, 397000));
        model.addAttribute("from", LocalDate.now().minusMonths(5));
        model.addAttribute("to", LocalDate.now());
        return "finance/dashboard";
    }

    @PostMapping("/collections/**")
    String collectionAction(RedirectAttributes redirect) {
        redirect.addFlashAttribute("success", "Demo action completed.");
        return "redirect:/collections";
    }
}
