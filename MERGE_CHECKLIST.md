# SubscriptIQ merge checklist

## Switch from standalone mock mode

1. Keep the existing Maven project and the approved `com.github.librepdf:openpdf:2.0.4` dependency. Do not copy a second project or change the Spring Boot / Java versions.
2. Start the current standalone application with `--spring.profiles.active=mock`. Authentication accounts are stored in the persistent local H2 database at `./data/subscriptiq`; the subscription, invoice, and billing business dataset remains mock-backed. To use MySQL, activate both `mock,mysql` and set `SUBSCRIPTIQ_DB_URL`, `SUBSCRIPTIQ_DB_USERNAME`, and `SUBSCRIPTIQ_DB_PASSWORD`.
3. There are no built-in demo credentials or default Admin account. Set both `SUBSCRIPTIQ_BOOTSTRAP_ADMIN_EMAIL` and `SUBSCRIPTIQ_BOOTSTRAP_ADMIN_PASSWORD` on the first startup to provision the initial Admin (optionally set `SUBSCRIPTIQ_BOOTSTRAP_ADMIN_NAME`). The password must be at least 9 characters and contain uppercase, lowercase, and a special character. Subsequent Staff/Admin accounts are created by an authenticated Admin in **Staff & roles**. Public registration always creates a CUSTOMER account.
4. Remove or disable the mock-only classes once the real endpoints are merged:
   - `config/dev/MockSecurityConfig.java`
   - every class under `controller/dev/` (`MockAdvice`, `MockAuthPages`, `MockAdminPages`, `MockCustomerSubscriptionPages`, `MockInvoicePaymentPages`, `MockBillingFinancePages`, `MockData`)
   - `controller/InvoicePdfController.java` (its implementation is deliberately mock-profile-only)
   - `application-mock.properties` if the mock startup mode is no longer needed.
5. Replace the `service/InvoicePdfService.java` `// STUB` after the backend `Invoice` and `InvoiceLine` types are available. Implement `generate(Invoice) -> byte[]` against the agreed fields in `TEMPLATE_CONTRACT.md`, then wire the real PDF controller to it. The current mock PDF endpoint is a sample OpenPDF document and does not read a real entity.
6. Add a shared production `@ControllerAdvice` (or equivalent model setup) for every page rendered through `layout/base.html`: `appName`, `currentUser.fullName`, `currentUser.email`, `currentRole`, `clockOffsetDays`, `today`, `activeNav`, and any flash attributes. Ensure the CSRF token is available to the base layout; CSRF must remain enabled.
7. Map production controllers to the URL and view names in `TEMPLATE_CONTRACT.md`. Preserve the two htmx fragment responses:
   - `GET /invoices/rows` → `invoices/list :: rows`
   - `GET /payments/outstanding?customerId=` → `payments/form :: outstandingList`
   - `POST /billing/preview` → `billing/center :: previewPanel`
   - `POST /billing/run` → `billing/center :: resultPanel`
   - `POST /subscriptions/{id}/lines` → `subscriptions/detail :: linesPanel`
8. Ensure form controllers expose a `form` object with a `BindingResult` on validation failure. Field names consumed by templates are enumerated per view in `TEMPLATE_CONTRACT.md`. Add real `types`, `periods`, `taxes`, `products`, `customers`, `methods`, `outstandingInvoices`, and other page-specific attributes where noted.
9. Backend data requirements before enabling the real templates:
   - `Invoice.outstanding` must be exposed by the backend (for example as a getter); templates never derive it.
   - Invoice line property names must be `description`, `quantity`, `unitPrice`, `lineAmount`, `discountAmount`, `discountLabel`, `taxRatePercent`, and `taxAmount`.
   - Keep enum values string-compatible with the documented status, period, product, discount, and payment-method names.
   - Build the `actions` lists on the server and include only permitted transition keys.
10. Keep role authorization on the server. Current mock-profile authentication uses persisted account roles and protected routes; the mock business data itself is still demo-only and is not production billing persistence.

## Re-test after merging

- `GET /login`, `GET/POST /register`: test validation and duplicate-email errors.
- Admin: `/admin/dashboard`, `/admin/finance`, `/admin/products`, `/admin/plans`, `/admin/taxes`, `/admin/discounts`, `/admin/staff`, `/admin/clock`.
- Staff: `/staff/dashboard`, `/customers`, `/subscriptions`, `/invoices`, `/payments`, `/billing`, `/collections`.
- Customer: `/portal`, `/portal/subscriptions`, `/portal/subscriptions/{id}`, `/portal/invoices`, `/portal/invoices/{id}`.
- Verify invoice filters, print layout, inline PDF output, payment outstanding-invoice fragment, billing preview/run fragments, subscription line add/remove, and CSRF on every state-changing request.
- Exercise empty and validation-error states; check keyboard/mobile layouts at 375px; verify A4 print output; verify `₹12,34,567.50` formatting.
- Search rendered templates and scripts for CDN references and frontend arithmetic on monetary values.
- Run `mvn test` to validate persisted login roles, registration/provisioning constraints, session/logout behavior, route authorization, and search regressions.
