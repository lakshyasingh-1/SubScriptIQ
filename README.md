# SubscriptIQ — Subscription Management & Revenue Control Platform

**One Platform. Every Subscription. Complete Revenue Visibility.**

SubscriptIQ is a subscription management and revenue control platform designed to help businesses manage their subscription lifecycle, automate recurring billing, track payments, and monitor revenue through a unified system.

Developed as a hackathon project, SubscriptIQ addresses the operational challenges faced by subscription-based businesses by bringing subscription management, invoicing, payment tracking, and financial reporting into one centralized application.

---

## 📌 Project Overview

Subscription-based businesses often manage customers, subscription plans, renewals, invoices, and payments using disconnected spreadsheets and manual processes.

These workflows can lead to missed renewals, duplicate invoices, incorrect billing, delayed payment reconciliation, and limited visibility into recurring revenue.

**SubscriptIQ aims to solve these problems by centralizing subscription operations and financial tracking in a single platform.**

The application is designed around the needs of three primary user groups:

- **Administrators:** Manage products, plans, taxes, discounts, users, and financial operations.
- **Staff:** Handle day-to-day customer, subscription, invoice, and payment operations according to their permissions.
- **Customers:** Access their own subscriptions, invoices, payment information, and permitted subscription-management features.

The goal is to make subscription operations more organized, reliable, and transparent while reducing dependence on manual administrative work.

---

## 🎯 Problem Statement

The project is based on the subscription management challenge described in the original hackathon brief.

A fictional subscription-based software company, Brightloop Software, faces several operational problems:

- Customer subscriptions are tracked in spreadsheets.
- Renewals depend on manual reminders.
- Invoices are prepared manually.
- Payments are matched to invoices by checking bank statements.
- Revenue reports are assembled manually at the end of each quarter.
- Customers must contact the company to obtain subscription information or manage certain requests.
- Administrators lack a centralized view of active subscriptions, outstanding payments, and overdue invoices.

These disconnected processes increase operational effort and make it difficult to maintain accurate financial records.

### Our Approach

SubscriptIQ brings these activities together in one application, with the objective of improving operational efficiency, billing consistency, payment tracking, and financial visibility.

---

## 💡 Key Features

### 1. User Authentication and Role-Based Access

The platform is designed to separate administrative, staff, and customer access.

- User registration and authentication.
- Secure password handling.
- Role-based authorization.
- Administrator-controlled staff creation.
- Restricted access to customer-specific information.
- Password recovery workflow.

Access to sensitive operations should be enforced by the backend rather than relying solely on frontend visibility.

### 2. Product and Variant Management

Manage the products offered through subscription plans.

- Create and manage products.
- Maintain product descriptions and pricing.
- Record product cost and sales prices.
- Support product variants.
- Configure additional pricing for selected variants.
- Maintain product availability.

For example, a product with a base price of ₹1,000 and a variant surcharge of ₹560 would have a resulting unit price of ₹1,560.

### 3. Subscription Plan Management

Create subscription plans with configurable commercial terms.

- Daily billing.
- Weekly billing.
- Monthly billing.
- Yearly billing.
- Minimum purchase quantities.
- Plan availability dates.
- Renewal policies.
- Pause and cancellation policies.
- Subscription expiry handling.

The objective is to provide a consistent way to manage recurring customer relationships and their billing schedules.

### 4. Quotations and Subscription Lifecycle

Support the process of turning a commercial offer into an active subscription.

The intended lifecycle is:

`Draft → Quotation → Confirmed → Active → Closed`

Quotation templates can be used to prepare offers with predefined product lines and a configured validity period.

Each subscription can contain:

- A unique subscription reference.
- Customer information.
- Subscription plan.
- Start and expiry dates.
- Payment terms.
- Products and quantities.
- Unit prices.
- Applicable taxes and discounts.
- Current subscription status.

Lifecycle transitions should follow defined business rules to prevent invalid activations, renewals, and closures.

### 5. Recurring Billing and Invoice Generation

Recurring billing is a central part of the SubscriptIQ concept.

Instead of manually preparing every invoice, the system is designed to generate invoices from eligible subscriptions when billing becomes due.

Invoices can contain:

- Invoice number.
- Customer details.
- Subscription reference.
- Product lines and quantities.
- Unit prices.
- Tax details.
- Discounts.
- Invoice totals.
- Issue and due dates.
- Payment status.
- Outstanding balance.

An important design objective is to prevent duplicate invoices for the same subscription and billing period.

### 6. Tax Management

Support configurable tax rules for subscription products and invoices.

The intended functionality includes:

- Tax names and rates.
- Applicable products.
- Tax validity periods.
- Automatic tax calculations.
- Preservation of the tax amounts applied to historical invoices.

Centralized tax calculation helps maintain consistency across billing operations.

### 7. Discount Management

Provide configurable discount rules for eligible products and subscriptions.

Supported discount concepts include:

- Fixed-amount discounts.
- Percentage discounts.
- Minimum purchase requirements.
- Minimum quantity requirements.
- Start and end dates.
- Usage limits.
- Product-specific applicability.
- Subscription-level discounts.

The objective is to apply eligible discounts during invoice calculation while respecting their validity periods and usage limits.

### 8. Payment Tracking and Reconciliation

Record payments against the appropriate invoices.

Payment information can include:

- Payment reference.
- Associated invoice.
- Payment method.
- Amount.
- Payment date.
- Transaction reference.
- Notes.

The platform aims to provide clear visibility into paid, partially paid where supported, and outstanding invoices.

Payment reconciliation helps staff identify outstanding balances and follow up on overdue payments.

Recording a payment in the application should not be confused with processing an actual bank transfer or card transaction. Real payment processing requires a separately configured payment gateway or banking integration.

### 9. Financial Dashboard and Reporting

Provide a centralized view of subscription and billing operations.

Useful business metrics include:

- Active subscriptions.
- Total customers.
- Total invoices.
- Collected revenue.
- Outstanding receivables.
- Overdue invoices.
- Payment activity.
- Upcoming renewals.
- Revenue trends.

The dashboard is intended to help administrators and authorized staff understand the business's financial position without manually combining data from multiple sources.

Financial metrics should be calculated from the underlying subscription, invoice, and payment records.

### 10. Customer Self-Service Portal

Provide customers with access to their own subscription information.

The intended customer experience includes:

- Viewing subscriptions.
- Checking subscription status.
- Reviewing renewal and expiry dates.
- Viewing invoices.
- Checking outstanding balances.
- Reviewing payment history.
- Accessing permitted subscription-management actions.

Customer data isolation is an important security requirement: customers must not be able to access another customer's subscriptions or invoices.

### 11. Invoice PDF Generation

The technology stack includes OpenPDF for invoice PDF generation.

The intended workflow is to generate a printable invoice containing the relevant customer, product, tax, discount, and payment information.

PDF access must respect the application's authorization rules.

---

## 🏗️ Technology Stack

The project uses the following technology stack.

| Layer | Technology | Purpose |
|---|---|---|
| Programming Language | Java 21 | Application logic |
| Backend Framework | Spring Boot 4.1.2 | Application architecture and backend services |
| Build Tool | Maven | Dependency management and builds |
| Frontend Rendering | Thymeleaf | Server-rendered web pages |
| UI Framework | Bootstrap 5 | Responsive user interface |
| Frontend | HTML5, CSS3, JavaScript | Page structure, styling, and interactions |
| Dynamic UI | htmx | Dynamic page interactions where used |
| Charts | Chart.js | Financial charts and visualizations |
| ORM | Hibernate | Object-relational mapping |
| Persistence | Spring Data JPA | Database access |
| Database | MySQL | Persistent application data |
| Authentication | Spring Security | Authentication and access control |
| Validation | Jakarta Validation | Input validation |
| PDF Generation | OpenPDF | Printable invoice documents |
| Development IDE | IntelliJ IDEA | Development and debugging |
| Version Control | Git and GitHub | Source-code management |

---

## ⚙️ How the System Works

The overall business workflow can be understood through the following stages.

### Step 1: Product and Plan Configuration

An administrator creates products, configures variants, establishes prices, and defines subscription plans.

### Step 2: Customer and Quotation Management

A customer is associated with a subscription offer. Staff can prepare quotations using reusable templates and predefined product lines.

### Step 3: Subscription Activation

After confirmation, the subscription progresses through its permitted lifecycle and becomes eligible for billing according to its configured terms.

### Step 4: Recurring Invoice Generation

When a subscription reaches its billing date, the billing workflow identifies the eligible subscription and generates the corresponding invoice.

### Step 5: Tax and Discount Calculation

The invoice calculation process determines applicable product prices, discounts, taxes, and the final payable amount.

### Step 6: Payment Recording

Payments are associated with invoices so that the system can calculate the amount received and the remaining balance.

### Step 7: Financial Monitoring

The dashboard presents subscription and financial information to authorized users, helping them monitor collections, outstanding receivables, overdue invoices, and renewal activity.

### Step 8: Customer Self-Service

Customers can access their own subscriptions and invoices through the customer-facing portal, reducing the need for routine administrative assistance.

---

## 🧩 High-Level Architecture

SubscriptIQ follows a layered application architecture.

```text
                  USERS
                    |
          +---------+---------+
          |                   |
      ADMIN / STAFF        CUSTOMER
          |                   |
          +---------+---------+
                    |
              Web Interface
        Thymeleaf + Bootstrap 5
             HTML / CSS / JS
                    |
              Spring Security
                    |
                Controllers
                    |
                 Services
          Business Rules and Logic
                    |
        +-----------+-----------+
        |           |           |
   Repositories  Scheduler   PDF Service
        |       (Billing)       |
        +-----------+-----------+
                    |
             Hibernate / JPA
                    |
                  MySQL
```

### Architectural Responsibilities

**Presentation Layer**

Renders dashboards, forms, tables, subscription details, invoices, and customer portal pages.

**Controller Layer**

Handles incoming web requests, validates request-level requirements, and coordinates application services.

**Service Layer**

Contains subscription lifecycle rules, billing calculations, discount and tax processing, payment reconciliation, and other business operations.

**Repository Layer**

Provides database access through Spring Data JPA and appropriate queries.

**Database Layer**

Stores users, customers, products, plans, subscriptions, invoices, payments, and related records.

**Supporting Components**

Scheduled billing processes and PDF generation support recurring operations and document output.

This architecture separates presentation, business logic, and persistence responsibilities to improve maintainability.

---

## 💼 Business Value

SubscriptIQ is designed to address practical business problems rather than simply provide basic record-management screens.

| Business Challenge | Intended Solution |
|---|---|
| Subscription records scattered across spreadsheets | Centralized subscription management |
| Missed renewals | Billing schedules and renewal monitoring |
| Manual invoice preparation | Recurring invoice generation |
| Incorrect or inconsistent billing | Centralized tax, discount, and invoice calculations |
| Difficult payment reconciliation | Invoice-linked payment records |
| Limited financial visibility | Revenue and receivables dashboard |
| Excessive administrative work | Customer self-service portal |
| Unauthorized data access | Role-based access and customer data isolation |
| Manual report preparation | Centralized operational and financial reporting |

### Expected Business Benefits

- Reduced manual administrative work.
- More consistent subscription workflows.
- Better visibility into recurring billing.
- Improved invoice and payment tracking.
- Faster identification of overdue payments.
- More accessible financial information.
- Clearer separation of administrative and customer responsibilities.

These are intended benefits; their actual impact depends on implementation quality and operational use.

---

## 🧪 Testing and Reliability Goals

Important areas for testing include:

- Authentication and role-based authorization.
- Product and subscription-plan management.
- Subscription lifecycle transitions.
- Billing-period calculations.
- Duplicate invoice prevention.
- Tax and discount calculations.
- Payment recording and reconciliation.
- Dashboard calculation accuracy.
- Customer data isolation.
- Invoice PDF generation.

Financial calculations and billing workflows require particular attention because mistakes can result in incorrect invoices, inaccurate reports, or duplicate charges.

The final project should be evaluated against these workflows before being used in a live demonstration.

---

## 🚀 Getting Started

### Prerequisites

Install and configure:

- Java Development Kit 25.
- Maven, or use the Maven Wrapper included in the project.
- MySQL Server.
- IntelliJ IDEA or another Java IDE.

### 1. Clone the Repository

Replace `<your-github-username>` with the GitHub account or organization that hosts the project.

```bash
git clone https://github.com/<your-github-username>/SubscriptIQ.git
cd SubscriptIQ
```

### 2. Create the MySQL Database

Open MySQL and create the application database.

```sql
CREATE DATABASE subscriptiq;
```

If your project uses a different database name, use the name configured in its application properties.

### 3. Configure Database Credentials

Configure the database connection using the project's supported Spring Boot configuration.

For example, the following environment variables can be used if the application is configured to read them:

```bash
export DB_URL="jdbc:mysql://localhost:3306/subscriptiq"
export DB_USERNAME="your_mysql_username"
export DB_PASSWORD="your_mysql_password"
```

Do not commit actual passwords or other secrets to GitHub.

### 4. Configure the Application

Review the project's configuration files, such as:

```text
src/main/resources/application.properties
```

or:

```text
src/main/resources/application.yml
```

Ensure that the MySQL connection, schema initialization or migration settings, and any required environment variables match the project configuration.

Use the database initialization or migration procedure documented by the project.

### 5. Build and Test

Using the Maven Wrapper on macOS or Linux, if it is included:

```bash
chmod +x mvnw
./mvnw clean verify
```

Alternatively, if Maven is installed:

```bash
mvn clean verify
```

### 6. Run the Application

With the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

Or with Maven installed:

```bash
mvn spring-boot:run
```

The application will normally be available at:

```text
http://localhost:8080
```

The actual port may differ depending on the application configuration.

**Important:** These are standard Spring Boot startup instructions. Successful execution depends on the final repository containing the required configuration, compatible dependencies, database setup, and application code.

---

## 🔐 Security Considerations

The intended security model includes:

- Secure password storage.
- Authenticated access to protected pages.
- Role-based authorization.
- Server-side input validation.
- Customer-specific data access restrictions.
- Protected invoice and payment records.
- Secure handling of configuration and credentials.

Production deployment should additionally use HTTPS, appropriate session settings, secure secret management, and a properly configured email service for password recovery.

Never use development credentials as production credentials.

---

## 🔮 Future Scope

Potential future improvements include:

- Payment gateway integration.
- Automated email notifications for invoices and renewals.
- Subscription renewal reminders.
- Advanced revenue analytics.
- CSV and Excel report exports.
- Multi-currency support.
- Automated payment reconciliation.
- Webhook-based payment updates.
- Customer notification preferences.
- Advanced audit logs.
- Improved billing-run monitoring.
- Additional performance testing for large subscription volumes.

These are potential extensions and should not be interpreted as existing functionality unless implemented.

---

## 🏆 Hackathon Objective

The objective of SubscriptIQ is to demonstrate how a unified subscription management platform can help a business manage recurring customer relationships and billing operations more effectively.

By connecting products, plans, subscriptions, invoices, payments, and financial reporting, the platform aims to provide a clearer and more structured view of the subscription business lifecycle.

**SubscriptIQ — From Subscription Creation to Revenue Visibility.**

---

## 👥 Project Information

**Project:** SubscriptIQ  
**Category:** Subscription Management and Revenue Control  
**Development Context:** College Hackathon  
**Institution:** PW Institute of Innovation, Lucknow  
**Batch:** 2025–2029  

**Repository:** Add the final GitHub repository URL here.

---

## 📄 Project Requirements

The project is based on the Subscription Management System challenge, which describes the operational requirements of a subscription-based software company, including user roles, product and plan management, quotations, recurring billing, taxes, discounts, payments, and financial reporting.

The final implementation should be evaluated against the original challenge requirements and the features verified in the application.
