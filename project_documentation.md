# FlowPay -- Payment Processing System Documentation

## 1. Introduction

**FlowPay -- Payment Processing System** is a simulated payment
management platform that demonstrates the complete lifecycle of
financial transactions.

The system allows a single customer (mocked user environment) to create,
process, track, schedule, and refund payments while maintaining
transaction history and audit records.

The application does not integrate with real payment networks or banks.
Instead, it internally simulates payment processing workflows similar to
real-world banking systems.

------------------------------------------------------------------------

# 2. Business Objective

The objective of this project is to design a payment processing platform
that can:

-   Create different types of payments.
-   Validate and process transactions.
-   Track payment status changes.
-   Support multiple currencies.
-   Handle payments to individuals and organizations.
-   Schedule future payments.
-   Process refunds.
-   Provide transaction analytics through dashboards.

------------------------------------------------------------------------

# 3. Payment Types Supported

## Individual Payment

Allows a customer to transfer money to another individual recipient.

## Entity Payment

Allows payments to organizations, businesses, or other entities.

## International Payment

Allows customers to make payments to international recipients using
different currencies.

Supported currencies:

-   INR
-   USD
-   EUR
-   GBP
-   AED
-   JPY

Exchange rate conversion is not implemented because the system is a
payment processing demonstration.

------------------------------------------------------------------------

# 4. Payment Lifecycle

    CREATED
       |
       ↓
    VALIDATED
       |
       ↓
    SENT
       |
       ↓
    COMPLETED

FAILED can occur at any stage.

## Status Definitions

  Status      Description
  ----------- ------------------------------------
  CREATED     Payment request submitted
  VALIDATED   Payment passed validation checks
  SENT        Payment transmitted to destination
  COMPLETED   Payment successfully processed
  FAILED      Payment processing failed
  REFUNDED    Completed payment was reversed

------------------------------------------------------------------------

# 5. Main Features

## Create Payment

-   Create individual, entity, and international payments.
-   Validate payment details.
-   Generate payment reference.
-   Track payment lifecycle.

## Transaction History

Users can:

-   View all transactions.
-   Search payments.
-   Filter by payment type.
-   Filter by status.
-   Filter by currency.

## Payment Status Audit Trail

Every payment status change is recorded with:

-   Previous status.
-   Current status.
-   Timestamp.
-   Description.

## Scheduled Payments

Users can schedule future payments with date and time information.

## Refund Processing

Completed payments can be refunded while maintaining refund records and
status history.

------------------------------------------------------------------------

# 6. Dashboard Analytics

The dashboard provides:

-   Total payments.
-   Total transaction amount.
-   Successful payments.
-   Failed payments.
-   Scheduled payments.
-   Refund statistics.

Visualization:

-   Status distribution pie charts.
-   Payment type comparison charts.
-   Transaction trends.

------------------------------------------------------------------------

# 7. Database Design

## Important Entities

### Payment

Stores payment details and current payment status.

Main attributes:

-   Payment ID
-   Amount
-   Currency
-   Payment Type
-   Recipient details
-   Current Status
-   Created Date
-   Scheduled Date

------------------------------------------------------------------------

### PaymentStatusHistory

Stores status transition records over time.

Main attributes:

-   History ID
-   Payment ID
-   Previous Status
-   Current Status
-   Changed Timestamp
-   Reason/Description

------------------------------------------------------------------------

### Refund

Stores refund request and processing details.

Main attributes:

-   Refund ID
-   Payment ID
-   Refund Amount
-   Refund Status
-   Created Date

------------------------------------------------------------------------

## Simple Relationship Overview

-   One payment has many status history records.
-   One payment may have zero or more refunds based on business rules.

------------------------------------------------------------------------

## Mermaid ER Diagram

``` mermaid
erDiagram
    PAYMENT ||--o{ PAYMENT_STATUS_HISTORY : has
    PAYMENT ||--o{ REFUND : may_have

    PAYMENT {
        bigint id PK
        decimal amount
        string currency
        string status
        datetime created_at
        datetime updated_at
        datetime scheduled_at
    }

    PAYMENT_STATUS_HISTORY {
        bigint id PK
        bigint payment_id FK
        string from_status
        string to_status
        datetime changed_at
        string reason
    }

    REFUND {
        bigint id PK
        bigint payment_id FK
        decimal amount
        string status
        datetime created_at
    }
```

------------------------------------------------------------------------

# 8. Technology Stack

  Technology            Purpose
  --------------------- ---------------------------------
  Java                  Backend programming
  Spring Boot           REST API framework
  Maven                 Dependency management and build
  MySQL                 Database
  JPA/Hibernate         Database operations
  HTML/CSS/JavaScript   Frontend development
  Bootstrap             Responsive UI design
  Chart.js              Dashboard visualization

------------------------------------------------------------------------

# 9. System Architecture

                    User
                     |
              HTML/CSS/JavaScript
                     |
                  REST API
                     |
            Spring Boot Application
                     |
           -----------------------
           |                     |
     Service Layer        Repository Layer
           |                     |
           -----------------------
                     |
                  MySQL Database

------------------------------------------------------------------------


## 10. Team Work Division

The project was divided among three team members to ensure parallel development, effective collaboration, and smooth integration of different modules.

| Member | Ownership | Core Responsibilities | Key Deliverables |
|---|---|---|---|
| Aastha | Backend Development | Payment processing logic, API development, dashboard backend integration, database connectivity | Backend services, payment APIs, dashboard data integration, and database communication |
| Bhargavi | Backend Development & Testing | API development, database creation, identifying and fixing errors, unit testing | Backend API support, database setup, bug fixes, and test validation |
| Rajini | Frontend Development & Documentation | User interface development, dashboard design, payment screen implementation, project documentation and presentation preparation | Complete frontend interface, dashboard UI, payment workflows, documentation, and presentation materials |

---

## Collaboration Model

- Aastha and Bhargavi collaborated on backend development, API creation, and database integration to ensure smooth data flow.
- Aastha handled payment processing logic, backend services, and dashboard integration.
- Bhargavi focused on API development, database creation, error handling, and unit testing.
- Rajini worked on frontend development, integrating user interfaces with backend APIs, and preparing project documentation and presentation materials.
- Team members collaborated using Git branches and pull requests for version control and code integration.
- Regular discussions were conducted to align features, resolve issues, and maintain consistency across the project.

------------------------------------------------------------------------

# 11. REST API Overview

  Function            Endpoint
  ------------------- --------------------------------
  Create Payment      POST /api/payments
  Get Payments        GET /api/payments
  Payment History     GET /api/payments/history
  Dashboard Summary   GET /api/dashboard/summary
  Schedule Payment    POST /api/payments/schedule
  Refund Payment      POST /api/payments/{id}/refund

------------------------------------------------------------------------

# 12. Project Outcome

The completed system demonstrates:

-   Payment creation.
-   Multiple payment categories.
-   Multi-currency support.
-   Payment lifecycle management.
-   Audit trail tracking.
-   Transaction filtering.
-   Scheduled payments.
-   Refund processing.
-   Dashboard analytics.
-   REST API development.
-   Frontend-backend integration.

------------------------------------------------------------------------

# Conclusion

FlowPay represents a simplified real-world banking payment processing
platform. It demonstrates how modern payment systems manage transactions
from creation to completion while maintaining traceability, workflow
control, and business rules.
