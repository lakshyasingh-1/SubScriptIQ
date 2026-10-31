package com.subscriptiq.controller.dev;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public final class MockData {
    static final LocalDate TODAY = LocalDate.now();

    private MockData() {
    }

    static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    static Map<String, Object> customer(long id, String name, String email, String company) {
        return Map.of("id", id, "name", name, "email", email, "phone", "+91 98765 43210",
                "company", company, "address", "42 Residency Road, Bengaluru, Karnataka 560025");
    }

    static Map<String, Object> product(long id, String name, String type, String price) {
        return Map.of("id", id, "name", name, "type", type, "salesPrice", amount(price),
                "costPrice", amount("1200.00"), "notes", "Brightloop subscription service",
                "tax", Map.of("id", 1, "name", "GST", "ratePercent", amount("18.00"), "active", true),
                "active", true, "variants", List.of());
    }

    static Map<String, Object> plan(long id, String name, Map<String, Object> product, String price) {
        return Map.ofEntries(
                Map.entry("id", id), Map.entry("name", name), Map.entry("product", product),
                Map.entry("price", amount(price)), Map.entry("billingPeriod", "MONTHLY"),
                Map.entry("minQuantity", 1), Map.entry("availableFrom", TODAY.minusMonths(6)),
                Map.entry("availableTo", TODAY.plusYears(1)), Map.entry("autoClose", false),
                Map.entry("allowEarlyClose", true), Map.entry("allowPause", true),
                Map.entry("allowRenew", true), Map.entry("active", true));
    }

    static Map<String, Object> subscription(long id, String number, Map<String, Object> customer,
                                            Map<String, Object> plan, String status) {
        return Map.ofEntries(
                Map.entry("id", id), Map.entry("number", number), Map.entry("customer", customer),
                Map.entry("plan", plan), Map.entry("status", status),
                Map.entry("startDate", TODAY.minusMonths(2)), Map.entry("expiryDate", TODAY.plusMonths(10)),
                Map.entry("paymentTermsDays", 15), Map.entry("quotationValidUntil", TODAY.plusDays(14)),
                Map.entry("nextBillingDate", TODAY.plusDays(3)), Map.entry("lines", List.of()));
    }

    static Map<String, Object> invoice(long id, String number, Map<String, Object> customer,
                                       Map<String, Object> subscription, String status, String total,
                                       String paid, LocalDate dueDate) {
        BigDecimal invoiceTotal = amount(total);
        BigDecimal amountPaid = amount(paid);
        return Map.ofEntries(
                Map.entry("id", id), Map.entry("number", number), Map.entry("subscription", subscription),
                Map.entry("customer", customer), Map.entry("periodStart", TODAY.minusMonths(1)),
                Map.entry("periodEnd", TODAY), Map.entry("invoiceDate", TODAY.minusDays(8)),
                Map.entry("dueDate", dueDate), Map.entry("status", status),
                Map.entry("subtotal", amount("10000.00")), Map.entry("discountTotal", amount("0.00")),
                Map.entry("taxTotal", amount("1800.00")), Map.entry("total", invoiceTotal),
                Map.entry("amountPaid", amountPaid), Map.entry("outstanding", invoiceTotal.subtract(amountPaid)),
                Map.entry("lines", List.of(Map.of("id", 1, "description", "SubscriptIQ Professional",
                        "quantity", 5, "unitPrice", amount("2000.00"), "lineAmount", amount("10000.00"),
                        "discountAmount", amount("0.00"), "discountLabel", "No discount",
                        "taxRatePercent", amount("18.00"), "taxAmount", amount("1800.00")))));
    }

    public static List<Map<String, Object>> customers() {
        return List.of(
                customer(1, "Northstar Labs", "finance@northstar.example", "Northstar Labs"),
                customer(2, "Maya Kapoor", "maya.kapoor@example.com", "Kapoor Design Studio"),
                customer(3, "Orbit Works", "accounts@orbit.example", "Orbit Works"));
    }

    static List<Map<String, Object>> products() {
        return List.of(product(1, "Professional", "SOFTWARE", "2000.00"),
                product(2, "Priority Support", "SERVICE", "850.00"),
                product(3, "Analytics Pack", "ADD_ON", "1250.00"));
    }

    static List<Map<String, Object>> subscriptions() {
        List<Map<String, Object>> customers = customers();
        List<Map<String, Object>> products = products();
        Map<String, Object> monthly = plan(1, "Professional monthly", products.getFirst(), "10000.00");
        return List.of(
                subscription(1, "SUB-2026-001", customers.get(0), monthly, "ACTIVE"),
                subscription(2, "SUB-2026-002", customers.get(1), monthly, "PAUSED"),
                subscription(3, "SUB-2026-003", customers.get(2), monthly, "QUOTATION"),
                subscription(4, "SUB-2026-004", customers.get(0), monthly, "DRAFT"),
                subscription(5, "SUB-2026-005", customers.get(1), monthly, "CLOSED"));
    }

    public static List<Map<String, Object>> invoices() {
        List<Map<String, Object>> customers = customers();
        List<Map<String, Object>> subscriptions = subscriptions();
        return List.of(
                invoice(1, "INV-2026-031", customers.get(0), subscriptions.get(0), "CONFIRMED",
                        "11800.00", "2000.00", TODAY.minusDays(4)),
                invoice(2, "INV-2026-030", customers.get(1), subscriptions.get(1), "PAID",
                        "5900.00", "5900.00", TODAY.minusDays(3)),
                invoice(3, "INV-2026-029", customers.get(2), subscriptions.get(2), "DRAFT",
                        "11800.00", "0.00", TODAY.plusDays(7)),
                invoice(4, "INV-2026-028", customers.get(0), subscriptions.get(0), "CANCELLED",
                        "2400.00", "0.00", TODAY.minusDays(1)),
                invoice(5, "INV-2026-027", customers.get(1), subscriptions.get(1), "CONFIRMED",
                        "11800.00", "0.00", TODAY.plusDays(6)));
    }

    public static Form form() {
        return new Form();
    }

    public static class Form {
        private String fullName = "";
        private String email = "";
        private String password = "";
        private String name = "";
        private String type = "SOFTWARE";
        private String notes = "";
        private String billingPeriod = "MONTHLY";
        private String scope = "SUBSCRIPTION";
        private String role = "STAFF";
        private String phone = "";
        private String company = "";
        private String address = "";
        private String method = "UPI";
        private String note = "";
        private String invoiceId = "";
        private BigDecimal salesPrice = BigDecimal.ZERO;
        private BigDecimal costPrice = BigDecimal.ZERO;
        private BigDecimal price = BigDecimal.ZERO;
        private BigDecimal ratePercent = BigDecimal.ZERO;
        private BigDecimal value = BigDecimal.ZERO;
        private BigDecimal minPurchase = BigDecimal.ZERO;
        private BigDecimal amount = BigDecimal.ZERO;
        private Integer minQuantity = 1;
        private Integer usageLimit = 100;
        private Integer paymentTermsDays = 15;
        private boolean active = true;
        private boolean enabled = true;
        private boolean autoClose;
        private boolean allowEarlyClose = true;
        private boolean allowPause = true;
        private boolean allowRenew = true;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getBillingPeriod() { return billingPeriod; }
        public void setBillingPeriod(String billingPeriod) { this.billingPeriod = billingPeriod; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public String getInvoiceId() { return invoiceId; }
        public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
        public BigDecimal getSalesPrice() { return salesPrice; }
        public void setSalesPrice(BigDecimal salesPrice) { this.salesPrice = salesPrice; }
        public BigDecimal getCostPrice() { return costPrice; }
        public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getRatePercent() { return ratePercent; }
        public void setRatePercent(BigDecimal ratePercent) { this.ratePercent = ratePercent; }
        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }
        public BigDecimal getMinPurchase() { return minPurchase; }
        public void setMinPurchase(BigDecimal minPurchase) { this.minPurchase = minPurchase; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Integer getMinQuantity() { return minQuantity; }
        public void setMinQuantity(Integer minQuantity) { this.minQuantity = minQuantity; }
        public Integer getUsageLimit() { return usageLimit; }
        public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
        public Integer getPaymentTermsDays() { return paymentTermsDays; }
        public void setPaymentTermsDays(Integer paymentTermsDays) { this.paymentTermsDays = paymentTermsDays; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isAutoClose() { return autoClose; }
        public void setAutoClose(boolean autoClose) { this.autoClose = autoClose; }
        public boolean isAllowEarlyClose() { return allowEarlyClose; }
        public void setAllowEarlyClose(boolean allowEarlyClose) { this.allowEarlyClose = allowEarlyClose; }
        public boolean isAllowPause() { return allowPause; }
        public void setAllowPause(boolean allowPause) { this.allowPause = allowPause; }
        public boolean isAllowRenew() { return allowRenew; }
        public void setAllowRenew(boolean allowRenew) { this.allowRenew = allowRenew; }
    }

    static Page<Map<String, Object>> page(List<Map<String, Object>> items, boolean empty) {
        return page(items, empty, 0);
    }

    static Page<Map<String, Object>> page(List<Map<String, Object>> items, boolean empty, int pageNumber) {
        List<Map<String, Object>> visible = empty ? List.of() : items;
        Pageable pageable = PageRequest.of(Math.max(0, pageNumber), 10);
        int start = (int) Math.min(pageable.getOffset(), visible.size());
        int end = Math.min(start + pageable.getPageSize(), visible.size());
        return new PageImpl<>(visible.subList(start, end), pageable, visible.size());
    }
}
