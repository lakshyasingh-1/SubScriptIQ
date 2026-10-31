# SubscriptIQ template contract

This contract reflects the templates currently in `src/main/resources/templates`. Model property names are intentionally aligned with the backend handoff brief. The mock controllers return Maps for domain objects and a mutable `MockData.Form` bean for form binding.

## Shared layout and fragments

| Template | URL / use | Model attributes and properties read |
|---|---|---|
| `layout/base.html` | Shared by all application pages except auth and error | `currentUser.fullName`, `currentUser.email`; `currentRole`; `activeNav`; `clockOffsetDays`; `today`; `success`, `error`, `demoError`, `hxCsrfHeaders`, `_csrf.token`, `_csrf.headerName`. `@fmt.date(today)`. |
| `fragments/ui.html` | Shared Thymeleaf fragments | `statusBadge(status)` reads `status.toString()`; `money(amount)` calls `@fmt.money`; `pagination(page, baseUrl)` reads `page.totalPages`, `page.first`, `page.last`, `page.number`; `pageHeader(title, actionLabel, actionUrl)`; `emptyState(message)`; `fieldError(field)` reads `BindingResult` errors. |
| `error.html` | Spring error view (403/404/500) | `status`. |

## Pages

| Template | URL(s) | Model attributes and properties read |
|---|---|---|
| `auth/login.html` | `GET /login`, `POST /login` | Request parameters `error`, `logout`; form submits `username`, `password`, `role`. |
| `auth/register.html` | `GET/POST /register` | `form.fullName`, `form.email`, `form.password`; `BindingResult` field errors for those fields. |
| `admin/dashboard.html` | `GET /admin/dashboard` | `setup.productCount`, `planCount`, `taxCount`, `discountCount`, `staffCount`; `summary.activeSubscriptions`, `summary.mrr`, `summary.outstanding`. |
| `admin/products.html` | `GET /admin/products`, `GET /admin/products/rows` not used | `page.content`: `product.id`, `name`, `type.toString()`, `salesPrice`, `tax.ratePercent`, `active`; `page.empty`, `q`, `status`; pagination `page.number`, `totalPages`, `first`, `last`. |
| `admin/product-form.html` | `GET /admin/products/new`, `GET /admin/products/{id}/edit`, `POST /admin/products` | `form.name`, `type`, `salesPrice`, `costPrice`, `notes`, `active`; `types`; `taxes`: `id`, `name`, `ratePercent`; `variants`: `attribute`, `value`, `extraPrice`. Variant submit names: `variantAttributes[]`, `variantValues[]`, `variantExtraPrices[]`. |
| `admin/plans.html` | `GET /admin/plans` | `page.content`: `plan.name`, `product.name`, `billingPeriod.toString()`, `price`, `allowRenew`, `active`; Page pagination properties. |
| `admin/plan-form.html` | `GET /admin/plans/new`, `POST /admin/plans` | `form.name`, `price`, `billingPeriod`, `minQuantity`, `autoClose`, `allowEarlyClose`, `allowPause`, `allowRenew`; `products`: `id`, `name`; `periods`. Form also submits `availableFrom`, `availableTo`, `productId`. |
| `admin/taxes.html` | `GET /admin/taxes` | `page.content`: `tax.name`, `ratePercent`, `active`; Page pagination properties. |
| `admin/tax-form.html` | `GET /admin/taxes/new`, `POST /admin/taxes` | `form.name`, `ratePercent`, `active`. |
| `admin/discounts.html` | `GET /admin/discounts` | `page.content`: `discount.name`, `type`, `value`, `scope.toString()`, `minPurchase`, `minQuantity`, `startDate`, `endDate`, `usageLimit`, `usedCount`, `products`, `active`; Page pagination properties. |
| `admin/discount-form.html` | `GET /admin/discounts/new`, `POST /admin/discounts` | `form.name`, `type`, `value`, `scope`, `minPurchase`, `minQuantity`, `usageLimit`, `active`; `types`, `scopes`; `products.id`, `products.name`. Form also submits `startDate`, `endDate`, and `productIds`. |
| `admin/staff.html` | `GET /admin/staff` | `staff`: each `fullName`, `email`, `role.toString()`, `enabled`. |
| `admin/staff-form.html` | `GET /admin/staff/new`, `POST /admin/staff` | `form.fullName`, `email`, `role`, `enabled`. |
| `admin/clock.html` | `GET /admin/clock`, `POST /admin/clock/advance/{days}`, `POST /admin/clock/reset` | `offsetDays`, `simulatedDate`, `clockSteps`. |
| `staff/dashboard.html` | `GET /staff/dashboard` | `draftInvoiceCount`, `overdueCount`, `today`; `renewalsDue`: subscription `id`, `number`, `customer.name`, `plan.name`, `nextBillingDate`; `overdueInvoices`: invoice `id`, `number`, `customer.name`, `dueDate`, `outstanding`. |
| `portal/home.html` | `GET /portal` | `customer.name`; `subscriptions` and `openInvoices` (same object shapes as portal detail/list below). |
| `portal/subscriptions.html` | `GET /portal/subscriptions` | `page.content`: subscription `id`, `plan.name`, `number`, `plan.billingPeriod`, `nextBillingDate`, `status`; Page empty state. |
| `portal/subscription.html` | `GET /portal/subscriptions/{id}` | `subscription.id`, `number`, `status`, `plan.name`, `plan.price`, `plan.billingPeriod`, `startDate`, `expiryDate`, `nextBillingDate`; `actions` (see action keys below); `invoices`: `id`, `number`, `status`. |
| `portal/invoices.html` | `GET /portal/invoices` | `page.content`: invoice `id`, `number`, `invoiceDate`, `dueDate`, `outstanding`, `status`; Page empty state. |
| `portal/invoice.html` | `GET /portal/invoices/{id}` | `invoice.id`, `number`, `status`, `total`, `amountPaid`, `outstanding`, `invoiceDate`, `dueDate`; `invoice.lines`: `description`, `quantity`, `unitPrice`, `lineAmount`, `taxRatePercent`; `payments`: `method`, `paidOn`, `amount`. |
| `customers/list.html` | `GET /customers` | `page.content`: customer `id`, `name`, `company`, `email`, `phone`; `q`; Page pagination/empty state. |
| `customers/form.html` | `GET /customers/new`, `GET /customers/{id}/edit`, `POST /customers` | `form.name`, `email`, `company`, `phone`, `address`. |
| `customers/detail.html` | `GET /customers/{id}` | `customer.id`, `name`, `company`, `email`, `phone`, `address`; `subscriptions`: `id`, `number`, `plan.name`, `status`, `nextBillingDate`; `invoices`: `id`, `number`, `invoiceDate`, `status`, `outstanding`. |
| `subscriptions/list.html` | `GET /subscriptions` | `page.content`: subscription `id`, `number`, `customer.name`, `plan.name`, `status`, `nextBillingDate`; `status`, `q`; Page pagination/empty state. |
| `subscriptions/form.html` | `GET /subscriptions/new`, `POST /subscriptions` | `form.paymentTermsDays`; `customers`: `id`, `name`, `email`; `plans`: `id`, `name`, `price`, `billingPeriod`. Form also submits `customerId`, `planId`, `startDate`, `quotationValidUntil`. |
| `subscriptions/detail.html` | `GET /subscriptions/{id}`, `POST /subscriptions/{id}/lines` fragment `linesPanel`, `POST /subscriptions/{id}/transition/{action}` | `subscription.id`, `number`, `customer.name`, `plan.name`, `status.toString()`, `startDate`, `expiryDate`, `nextBillingDate`; `actions`; `lines`: `id`, `product.name`, `variant.value` (nullable), `quantity`, `unitPrice`, `taxRatePercent`, `amount`; `products`: `id`, `name`; `invoices`: `id`, `number`, `dueDate`, `status`, `total`. |
| `invoices/list.html` | `GET /invoices`, `GET /invoices/rows` fragment `rows` | `page.content`: invoice `id`, `number`, `customer.name`, `invoiceDate`, `dueDate`, `status.toString()`, `outstanding`, `total`; `today`; `q`, `status`, `overdueOnly`; Page pagination/empty state. |
| `invoices/detail.html` | `GET /invoices/{id}`, `POST /invoices/{id}/confirm`, `/cancel`, `/send` | `invoice.id`, `number`, `customer.name`, `status`, `total`, `amountPaid`, `outstanding`, `dueDate`, `subtotal`, `discountTotal`, `taxTotal`; `invoice.lines`: `description`, `quantity`, `unitPrice`, `lineAmount`, `discountAmount`, `discountLabel`, `taxRatePercent`, `taxAmount`; `payments`: `paidOn`, `method`, `note`, `amount`; `actions`. |
| `invoices/print.html` | `GET /invoices/{id}/print` | `company.name`, `address`, `email`, `phone`; invoice `id`, `number`, `status`, `customer.name`, `customer.address`, `invoiceDate`, `dueDate`, `periodStart`, `periodEnd`, `subtotal`, `discountTotal`, `taxTotal`, `total`, `amountPaid`, `outstanding`; lines use the same properties as invoice detail. |
| `payments/list.html` | `GET /payments` | `page.content`: payment `id`, `invoice.id`, `invoice.number`, `invoice.customer.name`, `paidOn`, `method.toString()`, `amount`, `note`; Page pagination/empty state. |
| `payments/form.html` | `GET /payments/new?invoiceId=`, `POST /payments`, `GET /payments/outstanding?customerId=` fragment `outstandingList` | `form.invoiceId`, `amount`, `method`, `note`; `customers`: `id`, `name`; `methods`; `today`; `outstandingInvoices`: `id`, `number`, `customer.name`, `dueDate`, `outstanding`, `status`. |
| `billing/center.html` | `GET /billing`, `POST /billing/preview` fragment `previewPanel`, `POST /billing/run` fragment `resultPanel` | `businessDate`; `lastRun.businessDate`, `createdCount`; `recentRuns`: `businessDate`, `runAt`, `createdCount`, `skippedCount`, `failedCount`, `notes`; `preview.wouldCreate`, `totalValue`, `problems`; `result.created`, `skipped`, `failed`, `problems`. |
| `collections/list.html` | `GET /collections` | `aging`: `label`, `count`, `amount`; `page.totalElements`, `page.content`: invoice `id`, `number`, `customer.name`, `dueDate`, `outstanding`; Page pagination/empty state. |
| `finance/dashboard.html` | `GET /admin/finance` | `summary.activeSubscriptions`, `mrr`, `arr`, `invoicedRevenue`, `collected`, `outstanding`, `overdueTotal`, `overdueCount`; `aging`: `label`, `count`, `amount`; `overdueInvoices.content`: invoice `id`, `number`, `customer.name`, `dueDate`, `outstanding`; `chartLabels`, `chartInvoiced`, `chartCollected`, `from`, `to`. |

## Shared object requirements

- Status values may be enums in the real application; templates compare `status.toString()`. The shared status fragment is the sole source for badge colors.
- `Invoice.outstanding` is required as a backend-provided property. Templates do not subtract `amountPaid` from `total`.
- Invoice/line monetary totals and chart data must be calculated server-side and passed as `BigDecimal`/numeric values.
- Subscription action keys: `send-quotation`, `confirm`, `activate`, `pause`, `resume`, `close`, `renew`, `cancel`.
- Invoice action keys: `confirm`, `cancel`, `send`, `print`, `pdf`, `add-payment`.
- The mock `Form` is only a form-binding bean; it is not a domain entity.
