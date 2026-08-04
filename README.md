# FlowOps Payment Processing System

## Overview

FlowOps is a mock corporate payment processing system designed to simulate the complete lifecycle of financial payments.

The system allows a single employer/business user to initiate and track payments such as employee salary transfers. The application simulates payment processing internally without integrating with real banking networks or payment gateways.

The main objective of this project is to demonstrate:

* REST API development
* Payment lifecycle management
* Validation handling
* Status tracking
* Audit history
* Error handling
* Payment scheduling
* Refund processing
* Payment analytics

---

# Project Scope

This project represents a corporate payment processing platform where:

* A single employer/business is assumed.
* No authentication or user account management is required.
* No real bank transactions are performed.
* Payments are simulated internally.
* Multiple banks and currencies are supported conceptually.

The system is not intended to be:

* A payroll management system
* A banking application
* A wallet/payment app like Google Pay
* A real payment gateway integration

---

# Payment Lifecycle

Every payment follows a controlled workflow:

```
CREATED
   |
   v
VALIDATED
   |
   v
SENT
   |
   v
COMPLETED
```

A payment can fail at any stage:

```
CREATED
VALIDATED
SENT

    |
    v

FAILED
```

---

# Core Features

## Payment Management

The system supports:

* Creating payments
* Viewing payment details
* Tracking payment status
* Viewing payment history
* Searching payments by status
* Viewing failed payment details

## Employee Salary Payments

The main use case is employer-to-employee payments.

Example:

```
Company Account
        |
        |
        v
Employee Bank Account
```

Employees may have accounts with different banks.

## Payment Validation

The system validates:

* Amount must be greater than zero
* Currency must be supported
* Required fields must be provided
* Source and destination accounts must be valid
* Duplicate payments must be detected

## Idempotency

The system prevents duplicate payments.

Example:

A user submits a payment request twice due to network issues.

Using an idempotency key:

```
First Request
      |
      v
Payment Created


Second Request
      |
      v
Existing Payment Returned
```

No duplicate transaction is created.

## Payment History / Audit Trail

Every status change is recorded.

Example:

```
Payment ID: 1001


CREATED
  |
  | 10:01 AM
  |
VALIDATED
  |
  | 10:02 AM
  |
SENT
  |
  | 10:03 AM
  |
COMPLETED
```

---

# Additional Features

## Scheduled Payments

The system supports scheduling future payments.

Examples:

* Monthly employee salaries
* Recurring payments

Scheduled payments are automatically processed when their execution date arrives.

## Refund Processing

Refund requests can be created for completed or failed payments.

Refund details include:

* Original payment ID
* Refund amount
* Reason
* Refund status
* Request timestamp
* Completion timestamp

## Analytics Dashboard

The system supports payment analytics such as:

* Total payments
* Successful payments
* Failed payments
* Total amount processed
* Payment trends

---

# Technology Stack

Backend:

* Java
* Spring Boot
* Spring Web
* Spring Data JPA

Database:

* MySQL

API Documentation:

* Swagger / OpenAPI

Testing:

* Postman

Version Control:

* Git

---

# Project Structure

```
src
 |
 └── main
      |
      ├── java
      |    |
      |    ├── controller
      |    ├── service
      |    ├── repository
      |    ├── entity
      |    └── dto
      |
      └── resources
           |
           └── application.properties
```

---

# Running the Application

## Prerequisites

Install:

* Java JDK
* Maven
* MySQL

## Clone Repository

```
git clone <repository-url>
```

## Navigate to Project

```
cd FlowOps-Payment-Processing
```

## Build Application

```
mvn clean install
```

## Run Application

```
mvn spring-boot:run
```

The application will start on:

```
http://localhost:8080
```

---

# API Overview

## Payment APIs

### Create Payment

```
POST /payments
```

Creates a new payment request.

### Get Payment Details

```
GET /payments/{id}
```

Retrieves payment information.

### Get All Payments

```
GET /payments
```

Returns all payments.

### Update Payment Status

```
PUT /payments/{id}/status
```

Updates payment lifecycle status.

### Payment History

```
GET /payments/{id}/history
```

Returns all status transitions.

---

# Error Handling

The system uses predefined error codes:

| Error Code                | Description                 |
| ------------------------- | --------------------------- |
| INVALID_AMOUNT            | Amount is invalid           |
| INVALID_ACCOUNT           | Account does not exist      |
| INVALID_CURRENCY          | Currency not supported      |
| DUPLICATE_PAYMENT         | Duplicate payment request   |
| PAYMENT_NOT_FOUND         | Payment does not exist      |
| INVALID_STATUS_TRANSITION | Invalid payment movement    |
| PROCESSING_ERROR          | Internal processing failure |
| NETWORK_ERROR             | Communication failure       |

---

# Future Enhancements

Possible future improvements:

* Real payment gateway integration
* Authentication and authorization
* Advanced reporting
* Notifications
* Multi-user support
* Payment reversal
* Currency exchange rate integration

---

# Team

FlowOps Payment Processing System
