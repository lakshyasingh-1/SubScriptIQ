package com.subscriptiq.controller;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.subscriptiq.config.Fmt;
import com.subscriptiq.account.UserAccountRepository;
import com.subscriptiq.controller.dev.MockData;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.Map;
import javax.imageio.ImageIO;

@Controller
@Profile("mock")
public class InvoicePdfController {
    private static final Color NAVY = new Color(20, 35, 62);
    private static final Color MUTED = new Color(92, 105, 130);
    private static final Color BLUE = new Color(57, 107, 235);
    private static final Color PALE_BLUE = new Color(239, 245, 255);
    private static final Color LINE = new Color(226, 231, 239);
    private static final Color PALE_GREEN = new Color(231, 250, 241);
    private static final Color GREEN = new Color(24, 132, 78);
    private static final Color PALE_AMBER = new Color(255, 246, 224);
    private static final Color AMBER = new Color(151, 99, 18);

    private final Fmt fmt;
    private final UserAccountRepository accounts;

    public InvoicePdfController(Fmt fmt, UserAccountRepository accounts) {
        this.fmt = fmt;
        this.accounts = accounts;
    }

    @GetMapping("/invoices/{id}/pdf")
    @ResponseBody
    ResponseEntity<byte[]> mockPdf(@PathVariable long id) throws DocumentException, IOException {
        return createInvoicePdf(id);
    }

    @GetMapping("/portal/invoices/{id}/pdf")
    @ResponseBody
    ResponseEntity<byte[]> portalInvoicePdf(@PathVariable long id, Authentication authentication)
            throws DocumentException, IOException {
        long customerId = MockData.customers().stream()
                .filter(customer -> customer.get("email").toString().equalsIgnoreCase(authentication.getName()))
                .mapToLong(customer -> ((Number) customer.get("id")).longValue())
                .findFirst()
                .orElseGet(() -> accounts.findByEmailIgnoreCase(authentication.getName())
                        .map(ignored -> 0L)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        Map<String, Object> invoice = MockData.invoices().stream()
                .filter(item -> ((Number) ((Map<?, ?>) item.get("customer")).get("id")).longValue() == customerId)
                .filter(item -> ((Number) item.get("id")).longValue() == id)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return createInvoicePdf(invoice);
    }

    private ResponseEntity<byte[]> createInvoicePdf(long id) throws DocumentException, IOException {
        Map<String, Object> invoice = MockData.invoices().get((int) ((id - 1) % MockData.invoices().size()));
        return createInvoicePdf(invoice);
    }

    private ResponseEntity<byte[]> createInvoicePdf(Map<String, Object> invoice)
            throws DocumentException, IOException {
        Map<?, ?> customer = (Map<?, ?>) invoice.get("customer");
        Map<?, ?> subscription = (Map<?, ?>) invoice.get("subscription");
        Map<?, ?> plan = (Map<?, ?>) subscription.get("plan");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 42, 42, 34, 34);
        PdfWriter.getInstance(document, output);
        document.open();

        addCompanyHeader(document);
        addInvoiceHeading(document, invoice);
        addInvoiceAndCustomerDetails(document, invoice, customer);
        addSubscriptionCard(document, plan);
        addLineItems(document, invoice);
        addTotals(document, invoice);
        addNotesAndSignoff(document);

        document.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + invoice.get("number") + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(output.toByteArray());
    }

    private void addCompanyHeader(Document document) throws DocumentException, IOException {
        PdfPTable header = new PdfPTable(new float[]{1.12f, 1f});
        header.setWidthPercentage(100);
        header.setSpacingAfter(16);

        PdfPTable brand = new PdfPTable(new float[]{0.38f, 1.62f});
        brand.setWidthPercentage(100);
        PdfPCell logoCell = emptyCell();
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Image logo = logoImage();
        logo.scaleToFit(42, 42);
        logoCell.addElement(logo);
        brand.addCell(logoCell);

        PdfPCell wordmark = emptyCell();
        wordmark.setVerticalAlignment(Element.ALIGN_MIDDLE);
        wordmark.addElement(new Paragraph("SubscriptIQ", font(21, NAVY, true)));
        wordmark.addElement(new Paragraph("Manage subscriptions, simply.", font(8.5f, MUTED, false)));
        brand.addCell(wordmark);
        header.addCell(containerCell(brand, Element.ALIGN_LEFT));

        PdfPCell company = emptyCell();
        company.setHorizontalAlignment(Element.ALIGN_RIGHT);
        company.addElement(rightParagraph("Brightloop Software", font(10, NAVY, true)));
        company.addElement(rightParagraph("42 Residency Road, Bengaluru, Karnataka 560025", font(8.5f, MUTED, false)));
        company.addElement(rightParagraph("billing@brightloop.example  |  +91 80 5555 0101", font(8.5f, MUTED, false)));
        header.addCell(company);

        PdfPCell rule = emptyCell();
        rule.setBorder(Rectangle.BOTTOM);
        rule.setBorderColor(LINE);
        rule.setBorderWidth(1);
        rule.setColspan(2);
        rule.setFixedHeight(8);
        header.addCell(rule);
        document.add(header);
    }

    private void addInvoiceHeading(Document document, Map<String, Object> invoice) throws DocumentException {
        PdfPTable heading = new PdfPTable(new float[]{1.55f, 0.75f});
        heading.setWidthPercentage(100);
        heading.setSpacingAfter(17);

        PdfPCell title = emptyCell();
        title.addElement(new Paragraph("Subscription Invoice", font(23, NAVY, true)));
        title.addElement(new Paragraph("Thank you for choosing Brightloop Software. Here are the details of your subscription.",
                font(9.5f, MUTED, false)));
        heading.addCell(title);

        String status = invoice.get("status").toString();
        boolean paid = "PAID".equals(status);
        PdfPCell statusCell = emptyCell();
        statusCell.setPadding(11);
        statusCell.setBackgroundColor(paid ? PALE_GREEN : PALE_AMBER);
        statusCell.setBorder(Rectangle.NO_BORDER);
        statusCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        statusCell.addElement(new Paragraph(paid ? "✓  Paid" : statusLabel(status),
                font(13, paid ? GREEN : AMBER, true)));
        statusCell.addElement(new Paragraph(paid ? "Payment successful" : "Payment status",
                font(8.5f, paid ? GREEN : AMBER, false)));
        heading.addCell(statusCell);
        document.add(heading);
    }

    private void addInvoiceAndCustomerDetails(Document document, Map<String, Object> invoice,
                                               Map<?, ?> customer) throws DocumentException {
        PdfPTable details = new PdfPTable(new float[]{1.15f, 0.85f});
        details.setWidthPercentage(100);
        details.setSpacingAfter(17);

        PdfPTable values = new PdfPTable(new float[]{0.95f, 1.25f});
        values.setWidthPercentage(100);
        addKeyValue(values, "Invoice No.", text(invoice.get("number")));
        addKeyValue(values, "Invoice Date", fmt.date((java.time.LocalDate) invoice.get("invoiceDate")));
        addKeyValue(values, "Billing Period", fmt.date((java.time.LocalDate) invoice.get("periodStart"))
                + " - " + fmt.date((java.time.LocalDate) invoice.get("periodEnd")));
        addKeyValue(values, "Due Date", fmt.date((java.time.LocalDate) invoice.get("dueDate")));
        details.addCell(containerCell(values, Element.ALIGN_LEFT));

        PdfPCell billTo = emptyCell();
        billTo.setBorder(Rectangle.LEFT);
        billTo.setBorderColor(LINE);
        billTo.setPaddingLeft(16);
        billTo.addElement(new Paragraph("BILL TO", font(8, MUTED, true)));
        String companyName = text(customer.get("company"));
        String customerName = text(customer.get("name"));
        if (!companyName.equals(customerName) && !companyName.isBlank()) {
            billTo.addElement(new Paragraph(companyName, font(10, NAVY, true)));
        }
        billTo.addElement(new Paragraph(customerName, font(9.5f, NAVY, true)));
        billTo.addElement(new Paragraph(text(customer.get("address")), font(8.5f, MUTED, false)));
        billTo.addElement(new Paragraph(text(customer.get("email")), font(8.5f, MUTED, false)));
        details.addCell(billTo);
        document.add(details);
    }

    private void addSubscriptionCard(Document document, Map<?, ?> plan) throws DocumentException {
        PdfPTable card = new PdfPTable(new float[]{0.28f, 1.72f});
        card.setWidthPercentage(100);
        card.setSpacingAfter(0);

        PdfPCell icon = cell("PLAN", font(8, BLUE, true), Element.ALIGN_CENTER, Element.ALIGN_MIDDLE);
        icon.setBackgroundColor(PALE_BLUE);
        icon.setBorder(Rectangle.NO_BORDER);
        icon.setFixedHeight(48);
        card.addCell(icon);

        PdfPCell details = emptyCell();
        details.setBorder(Rectangle.NO_BORDER);
        details.setPaddingLeft(12);
        details.addElement(new Paragraph(text(plan.get("name")), font(11, NAVY, true)));
        details.addElement(new Paragraph("Recurring subscription plan", font(8.5f, MUTED, false)));
        card.addCell(details);

        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        PdfPCell panel = containerCell(card, Element.ALIGN_LEFT);
        panel.setBorderColor(LINE);
        panel.setBorderWidth(0.8f);
        panel.setPadding(10);
        wrapper.addCell(panel);
        document.add(wrapper);
    }

    private void addLineItems(Document document, Map<String, Object> invoice) throws DocumentException {
        PdfPTable lines = new PdfPTable(new float[]{3.1f, 0.65f, 1.25f, 1.25f});
        lines.setWidthPercentage(100);
        lines.setSpacingAfter(0);
        lines.setHeaderRows(1);
        for (String heading : new String[]{"Description", "Quantity", "Unit Price", "Amount"}) {
            PdfPCell cell = cell(heading, font(8.5f, NAVY, true),
                    "Description".equals(heading) ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT,
                    Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(new Color(245, 247, 250));
            cell.setBorderColor(LINE);
            cell.setPadding(9);
            lines.addCell(cell);
        }
        for (Object lineValue : (Iterable<?>) invoice.get("lines")) {
            Map<?, ?> line = (Map<?, ?>) lineValue;
            PdfPCell description = emptyCell();
            description.addElement(new Paragraph(text(line.get("description")), font(9, NAVY, true)));
            description.addElement(new Paragraph("Subscription service", font(8, MUTED, false)));
            addBodyCell(lines, description, Element.ALIGN_LEFT);
            addBodyCell(lines, cell(text(line.get("quantity")), font(9, NAVY, false),
                    Element.ALIGN_RIGHT, Element.ALIGN_MIDDLE), Element.ALIGN_RIGHT);
            addBodyCell(lines, cell(money((BigDecimal) line.get("unitPrice")), font(9, NAVY, false),
                    Element.ALIGN_RIGHT, Element.ALIGN_MIDDLE), Element.ALIGN_RIGHT);
            addBodyCell(lines, cell(money((BigDecimal) line.get("lineAmount")), font(9, NAVY, false),
                    Element.ALIGN_RIGHT, Element.ALIGN_MIDDLE), Element.ALIGN_RIGHT);
        }
        document.add(lines);
    }

    private void addTotals(Document document, Map<String, Object> invoice) throws DocumentException {
        PdfPTable outer = new PdfPTable(new float[]{1.1f, 0.9f});
        outer.setWidthPercentage(100);
        outer.setSpacingAfter(18);

        PdfPCell spacer = emptyCell();
        spacer.setBorder(Rectangle.TOP);
        spacer.setBorderColor(LINE);
        outer.addCell(spacer);

        PdfPTable totals = new PdfPTable(new float[]{1.25f, 1f});
        totals.setWidthPercentage(100);
        addTotal(totals, "Subtotal", money((BigDecimal) invoice.get("subtotal")), false);
        BigDecimal discount = (BigDecimal) invoice.get("discountTotal");
        if (discount.signum() != 0) {
            addTotal(totals, "Discount", "-" + money(discount), false);
        }
        addTotal(totals, "Tax", money((BigDecimal) invoice.get("taxTotal")), false);
        addTotal(totals, "Total", money((BigDecimal) invoice.get("total")), true);
        addTotal(totals, "Amount paid", money((BigDecimal) invoice.get("amountPaid")), false);
        addTotal(totals, "Balance due", money((BigDecimal) invoice.get("outstanding")), true);

        PdfPCell totalsCell = containerCell(totals, Element.ALIGN_RIGHT);
        totalsCell.setBorder(Rectangle.TOP);
        totalsCell.setBorderColor(LINE);
        totalsCell.setPaddingTop(8);
        outer.addCell(totalsCell);
        document.add(outer);
    }

    private void addNotesAndSignoff(Document document) throws DocumentException, IOException {
        PdfPTable footer = new PdfPTable(new float[]{1.25f, 0.75f});
        footer.setWidthPercentage(100);
        footer.setSpacingBefore(3);

        PdfPCell notes = emptyCell();
        notes.addElement(new Paragraph("Notes", font(9, NAVY, true)));
        notes.addElement(new Paragraph("This is a computer-generated invoice.", font(8.5f, MUTED, false)));
        notes.addElement(new Paragraph("For billing questions, contact billing@brightloop.example.",
                font(8.5f, MUTED, false)));
        footer.addCell(notes);

        PdfPCell signoff = emptyCell();
        signoff.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Image signatureLogo = logoImage();
        signatureLogo.scaleToFit(34, 38);
        signatureLogo.setAlignment(Element.ALIGN_RIGHT);
        signoff.addElement(signatureLogo);
        Paragraph rule = rightParagraph("________________________________", font(8, LINE, false));
        rule.setSpacingAfter(2);
        signoff.addElement(rule);
        signoff.addElement(rightParagraph("Authorized billing team", font(8, MUTED, false)));
        signoff.addElement(rightParagraph("Brightloop Software", font(8, MUTED, false)));
        footer.addCell(signoff);
        document.add(footer);
    }

    private Image logoImage() throws IOException, DocumentException {
        ClassPathResource logoResource = new ClassPathResource("static/images/subscriptiq-logo.png");
        BufferedImage source;
        try (var input = logoResource.getInputStream()) {
            source = ImageIO.read(input);
        }
        if (source == null) {
            throw new IOException("Unable to read the SubscriptIQ invoice logo.");
        }
        BufferedImage opaque = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = opaque.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, opaque.getWidth(), opaque.getHeight());
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        try (ByteArrayOutputStream flattenedLogo = new ByteArrayOutputStream()) {
            if (!ImageIO.write(opaque, "png", flattenedLogo)) {
                throw new IOException("Unable to encode the SubscriptIQ invoice logo.");
            }
            return Image.getInstance(flattenedLogo.toByteArray());
        }
    }

    private void addKeyValue(PdfPTable table, String label, String value) {
        PdfPCell key = cell(label, font(8.5f, MUTED, false), Element.ALIGN_LEFT, Element.ALIGN_MIDDLE);
        PdfPCell data = cell(value, font(8.5f, NAVY, false), Element.ALIGN_LEFT, Element.ALIGN_MIDDLE);
        key.setPaddingTop(5);
        key.setPaddingBottom(5);
        data.setPaddingTop(5);
        data.setPaddingBottom(5);
        table.addCell(key);
        table.addCell(data);
    }

    private void addTotal(PdfPTable table, String label, String amount, boolean emphasized) {
        PdfPCell labelCell = cell(label, font(emphasized ? 9 : 8.5f, emphasized ? NAVY : MUTED, emphasized),
                Element.ALIGN_LEFT, Element.ALIGN_MIDDLE);
        PdfPCell amountCell = cell(amount, font(emphasized ? 9.5f : 8.5f, NAVY, emphasized),
                Element.ALIGN_RIGHT, Element.ALIGN_MIDDLE);
        labelCell.setPadding(6);
        amountCell.setPadding(6);
        if (emphasized) {
            labelCell.setBackgroundColor(PALE_BLUE);
            amountCell.setBackgroundColor(PALE_BLUE);
        }
        labelCell.setBorder(Rectangle.NO_BORDER);
        amountCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        table.addCell(amountCell);
    }

    private void addBodyCell(PdfPTable table, PdfPCell cell, int alignment) {
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(9);
        cell.setBorderColor(LINE);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private PdfPCell cell(String value, Font font, int horizontal, int vertical) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(horizontal);
        cell.setVerticalAlignment(vertical);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell emptyCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        return cell;
    }

    private PdfPCell containerCell(PdfPTable table, int alignment) {
        PdfPCell cell = new PdfPCell(table);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        return cell;
    }

    private Paragraph rightParagraph(String value, Font font) {
        Paragraph paragraph = new Paragraph(value, font);
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        return paragraph;
    }

    private Font font(float size, Color color, boolean bold) {
        return new Font(Font.HELVETICA, size, bold ? Font.BOLD : Font.NORMAL, color);
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "CONFIRMED" -> "Payment pending";
            case "OVERDUE" -> "Overdue";
            case "DRAFT" -> "Draft invoice";
            case "CANCELLED" -> "Cancelled";
            default -> status;
        };
    }

    private String money(BigDecimal amount) {
        return fmt.money(amount).replace("₹", "INR ");
    }

    private String text(Object value) {
        return value == null ? "—" : value.toString();
    }
}
