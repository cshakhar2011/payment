# Enterprise Payment Service

Spring Boot 3 + Java 21 + PostgreSQL + Redis.

## Run locally

Requirements:
- Java 21
- Maven 3.9+
- Docker Desktop

Start infrastructure:

```bash
docker compose up -d postgres redis
```

Configure the local database connection before starting the application:

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/paymentdb"
$env:DATABASE_USERNAME = "payment"
$env:DATABASE_PASSWORD = "payment123"
```

Build:

```bash
mvn clean package
```

Run:

```bash
mvn spring-boot:run
```

Set Razorpay test credentials in the PowerShell session before starting the API:

```powershell
$env:RAZORPAY_KEY_ID = "rzp_test_your_key_id"
$env:RAZORPAY_KEY_SECRET = "your_test_key_secret"
$env:RAZORPAY_WEBHOOK_SECRET = "your_webhook_secret"
$env:LOCAL_SECURITY_PERMIT = "true"
```

The API listens on `http://localhost:8092`.

- Swagger: http://localhost:8092/swagger-ui.html
- Health: http://localhost:8092/actuator/health

## Create payment order

```http
POST /api/v1/payments/orders
Idempotency-Key: <unique-key>
Content-Type: application/json
```

```json
{
  "customerId": "11111111-1111-1111-1111-111111111111",
  "amount": 4999.00,
  "currency": "INR",
  "description": "Enterprise subscription"
}
```

## Verify a payment

After Razorpay returns a payment result, send its three signed values to:

```http
POST /api/v1/payments/razorpay/verify
Content-Type: application/json
```

The backend verifies `razorpay_order_id`, `razorpay_payment_id`, and
`razorpay_signature` using the server-side key secret. The order is not marked
`COMPLETED` merely because an order was created or a signature was received;
provider status and webhook processing must confirm capture.

## Production notes

1. Configure a real OAuth2/OIDC issuer.
2. Implement persistent idempotency lookup before creating an order.
3. The Razorpay SDK creates orders only on the backend; React receives public order data.
4. Verify payment signatures server-side.
5. Verify webhook signatures and persist webhook events with the existing unique provider/event constraint.
6. Process webhook events asynchronously and make state transitions idempotent.
7. Store secrets in a secret manager, not Git.
8. Set `LOCAL_SECURITY_PERMIT=false` and require JWT authentication in production.
9. Add rate limiting, audit logging, reconciliation, monitoring and alerts.
10. Never store raw card/CVV data.
