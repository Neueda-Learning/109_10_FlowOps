# Payment Processing System

REST API for simple payment lifecycle management using Spring Boot, Java, and MySQL.

## Architecture

The project uses a clean layered structure:

- `controller`: REST endpoints
- `service`: business rules and payment lifecycle transitions
- `repository`: database access
- `entity`: JPA entities and enums
- `dto`: API request/response models
- `exception`: custom exceptions and global error handling

## Payment Types

- `INDIVIDUAL_PAYMENT`
- `ENTITY_PAYMENT`
- `INTERNATIONAL_PAYMENT`

Payment type-specific validation applied on creation.

## Lifecycle

`CREATED -> VALIDATED -> SENT -> COMPLETED`

`FAILED` can happen during validation or at any stage.
`REFUNDED` - allowed only from `COMPLETED`.

## Main Endpoints

- `POST /api/payments` - create payment
- `GET /api/payments/{id}` - get payment by id
- `GET /api/payments?paymentType=INDIVIDUAL_PAYMENT&status=CREATED` - list payments
- `GET /api/payments/history` - search payments with filters:
  - `paymentType` - filter by `INDIVIDUAL_PAYMENT`, `ENTITY_PAYMENT`, `INTERNATIONAL_PAYMENT`
  - `status` - filter by status
  - `paymentReference` - partial text search (case-insensitive)
- `POST /api/payments/{id}/validate` - move to `VALIDATED`
- `POST /api/payments/{id}/send` - move to `SENT`
- `POST /api/payments/{id}/complete` - move to `COMPLETED`
- `POST /api/payments/{id}/refund` - move to `REFUNDED`
- `POST /api/payments/{id}/fail` - mark as `FAILED`

## Status History

Every status transition is automatically recorded in `payment_status_history` table with:
- Previous status
- Current status
- Timestamp
- Description/reason

## CORS Configuration

The API allows CORS requests from localhost frontend origins:

```
http://localhost:3000
http://localhost:3001
http://localhost:4200
http://localhost:5173
http://127.0.0.1:3000
http://127.0.0.1:3001
http://127.0.0.1:4200
http://127.0.0.1:5173
```

Add origins in `src/main/java/com/payment/processing_system/config/CorsConfig.java` as needed.

## Example Requests

Create individual payment (auto-processes through full lifecycle):

```json
{
  "paymentReference": "PAY-001",
  "paymentType": "INDIVIDUAL_PAYMENT",
  "amount": 100.00,
  "currency": "USD",
  "recipientName": "John Doe",
  "recipientAccount": "ACC-100"
}
```

Refund a completed payment:

```json
POST /api/payments/123/refund
```

## Local Configuration

Application defaults in `src/main/resources/application.properties` use MySQL:

- URL: `jdbc:mysql://localhost:3306/payment_processing_db`
- Username: `root`
- Password: `root`

Update these values for your local environment.

## Run

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Test

Tests use in-memory H2 (`src/test/resources/application.properties`):

```powershell
.\mvnw.cmd test
```


