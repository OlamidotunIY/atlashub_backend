# Pay Charges Design (`atlashub-pay:charges`)

## Role & Purpose

The `charges` submodule manages **inbound payment collection**. It is the entry point for all money flowing INTO an organization from their customers — whether via card, bank transfer, USSD, or a physical Moniepoint POS terminal.

A `Charge` represents a single payment attempt. The submodule integrates with **Paystack** (card, bank transfer, USSD) and **Moniepoint** (physical POS terminals) via a `PaymentGatewayPort` domain port. Both providers notify AtlasHub of payment outcomes via inbound webhook callbacks, which are validated and processed here.

On a successful charge, this submodule posts the corresponding ledger entry (via `pay:ledger`) and publishes `ChargeSuccessfulEvent`, which drives downstream modules: Commerce (complete sale), Billing (mark invoice paid), Webhooks (notify merchant server), and WebSocket (notify cashier).

---

## Domain Layer

### Aggregate Root: `Charge`

**Package**: `com.atlashub.pay.charges.domain.entities`

```
Charge
├── id: Long
├── organizationId: Long
├── customerId: String             ← nullable; set for B2B2C payments
├── amount: Money
├── channel: PaymentChannel        ← CARD, BANK_TRANSFER, USSD, POS_TERMINAL
├── status: ChargeStatus           ← PENDING, SUCCESSFUL, FAILED, REFUNDED
├── reference: String              ← unique, caller-supplied; used to correlate gateway webhook
├── provider: PaymentProvider      ← PAYSTACK, MONIEPOINT
├── checkoutUrl: String            ← nullable; for redirect-based payments (card, USSD)
├── gatewayReference: String       ← provider's own transaction reference; nullable until confirmed
├── gatewayResponse: String        ← raw provider response; nullable
├── splitId: Long                  ← nullable; which split rule to apply on success
├── sourceSystem: SourceSystem     ← e.g., CARD_CHARGE, COMMERCE_CHECKOUT
├── sourceReferenceId: String      ← e.g., salesOrderId, billingInvoiceId
├── metadata: Map<String, String>  ← arbitrary key-value for merchant use
├── cashierId: Long                ← nullable; the POS cashier who initiated (for WS push)
├── createdAt: ZonedDateTime
└── completedAt: ZonedDateTime     ← nullable; set by markSuccessful() or markFailed()
```

**State Machine:**
```
PENDING ──markSuccessful()──► SUCCESSFUL ──refund()──► REFUNDED
        ──markFailed()──────► FAILED
```

**Invariants:**
- `reference` must be unique across all charges.
- `markSuccessful()` may only be called when `status == PENDING`.
- `markFailed()` may only be called when `status == PENDING`.
- `refund(amount)` may only be called when `status == SUCCESSFUL`.
- Refund `amount` must be ≤ original `amount`.

**Business Methods:**

| Method | Inputs | Guard | Effect | Event Registered |
|---|---|---|---|---|
| `markSuccessful(gatewayRef, gatewayResponse)` | `String gatewayRef`, `String gatewayResponse` | status must be `PENDING` | sets `gatewayReference`, `gatewayResponse`, `completedAt = now()`, `status = SUCCESSFUL` | `ChargeSuccessfulEvent` |
| `markFailed(reason)` | `String reason` | status must be `PENDING` | sets `gatewayResponse = reason`, `completedAt = now()`, `status = FAILED` | `ChargeFailedEvent` |
| `refund(amount)` | `Money amount` | status must be `SUCCESSFUL`; amount ≤ original | sets `status = REFUNDED` | `ChargeRefundInitiatedEvent` |

---

### Value Objects

**Package**: `com.atlashub.pay.charges.domain.valueobject`

| Class | Values | Description |
|---|---|---|
| `PaymentChannel` | `CARD`, `BANK_TRANSFER`, `USSD`, `POS_TERMINAL` | How the customer paid |
| `ChargeStatus` | `PENDING`, `SUCCESSFUL`, `FAILED`, `REFUNDED` | Charge lifecycle state |
| `PaymentProvider` | `PAYSTACK`, `MONIEPOINT` | Which gateway processed this charge |
| `Money` | `value: BigDecimal`, `currency: String` | Immutable monetary amount |

---

### Domain Events

**Package**: `com.atlashub.pay.charges.domain.events`

All events published to Kafka topic **`pay-events`**.

| Event | Published When | Consumed By |
|---|---|---|
| `ChargeSuccessfulEvent` | Customer payment confirmed by gateway | `commerce` (complete sale), `billing` (mark invoice paid), `pay:ledger` (post entry), `pay:webhooks` (notify merchant), `pay:tx-query`, `SelectiveWebSocketBroadcaster` (cashier WS push) |
| `ChargeFailedEvent` | Payment declined/timed out | `commerce` (release reserved stock), `notifications`, `pay:webhooks`, `pay:tx-query`, `SelectiveWebSocketBroadcaster` (cashier WS push) |
| `ChargeRefundInitiatedEvent` | Refund initiated | `pay:transfers` (create payout to customer), `accounting`, `pay:tx-query` |

**`ChargeSuccessfulEvent` payload:**
```java
public record ChargeSuccessfulEvent(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) {
    public record Payload(
        Long chargeId,
        Long organizationId,
        String customerId,
        BigDecimal amount,
        String currency,
        String formattedAmount,       // e.g., "₦25,000.00"
        String channel,
        String provider,
        String reference,
        String gatewayReference,
        Long splitId,
        String sourceSystem,
        String sourceReferenceId,
        Long cashierId                // for WS push to cashier session
    ) {}
}
```

---

### Domain Exceptions

**Package**: `com.atlashub.pay.charges.domain.exceptions`

```java
public class ChargeNotFoundException extends NotFoundException {
    public ChargeNotFoundException() { super("Charge not found"); }
    public ChargeNotFoundException(String reference) { super("Charge not found for reference: " + reference); }
}

public class DuplicateChargeReferenceException extends ConflictException {
    public DuplicateChargeReferenceException(String reference) {
        super("A charge with reference '" + reference + "' already exists");
    }
}

public class InvalidChargeStateException extends BusinessRuleException {
    public InvalidChargeStateException(String message) { super(message); }
}

public class InvalidRefundAmountException extends ValidationException {
    public InvalidRefundAmountException() { super("Refund amount must be greater than zero and not exceed the original charge amount"); }
}

public class WebhookSignatureInvalidException extends AuthorizationException {
    public WebhookSignatureInvalidException() { super("Webhook signature validation failed"); }
}

public class WebhookAlreadyProcessedException extends ConflictException {
    public WebhookAlreadyProcessedException(String webhookId) {
        super("Webhook '" + webhookId + "' has already been processed");
    }
}
```

---

### Domain Ports

**Package**: `com.atlashub.pay.charges.domain.ports`

```java
public interface PaymentGatewayPort {
    /**
     * Initializes a charge with the payment provider.
     * Returns the checkout URL (for card/USSD redirect) or null (for POS terminal pushes).
     */
    InitializeChargeResult initialize(InitializeChargeRequest request);

    /**
     * Validates the HMAC signature on an inbound provider webhook.
     * Throws WebhookSignatureInvalidException if invalid.
     */
    void validateWebhookSignature(String payload, String signature, String secretKey);
}
```

---

### Domain Repository

**Package**: `com.atlashub.pay.charges.domain.repositories`

```java
public interface ChargeRepository {
    Charge save(Charge charge);
    Optional<Charge> findById(Long id);
    Optional<Charge> findByReference(String reference);
    Optional<Charge> findByGatewayReference(String gatewayReference);
    List<Charge> findByOrganizationId(Long organizationId, Pageable pageable);
}
```

---

## Application Layer

### Commands

#### `InitializeChargeCommand`

**Package**: `com.atlashub.pay.charges.application.commands.InitializeCharge`

```java
public record InitializeChargeCommand(
    Long organizationId,
    String customerId,           // nullable
    BigDecimal amount,
    String currency,
    String channel,
    String reference,            // caller-supplied idempotency key
    String provider,
    Long splitId,                // nullable
    String sourceSystem,
    String sourceReferenceId,
    Map<String, String> metadata,
    Long cashierId               // nullable; for WS push
) {}
```

**Handler**: `InitializeChargeHandler extends Command<InitializeChargeCommand, InitializeChargeResponse>`

**RBAC**: `pay:charges:create`

**Processing steps:**
1. Check idempotency: if a `Charge` with `reference` already exists, return its data (no re-initialization).
2. Construct `Charge` domain object with `status = PENDING`.
3. Call `paymentGatewayPort.initialize(...)` to get `checkoutUrl` and provider-assigned reference.
4. Set `checkoutUrl` on the charge.
5. Save via `ChargeRepository`.
6. Return `InitializeChargeResponse` with checkout URL.

**Response**: `InitializeChargeResponse`
```java
public record InitializeChargeResponse(
    Long chargeId,
    String reference,
    String checkoutUrl,    // nullable for POS_TERMINAL
    String status
) {}
```

---

#### `ProcessPaystackWebhookCommand`

**Package**: `com.atlashub.pay.charges.application.commands.ProcessPaystackWebhook`

```java
public record ProcessPaystackWebhookCommand(
    String rawPayload,
    String signature,
    String paystackWebhookId
) {}
```

**Handler**: `ProcessPaystackWebhookHandler extends Command<ProcessPaystackWebhookCommand, Void>`

**Triggered by**: `PaystackWebhookController` (public endpoint, no JWT — HMAC-validated instead).

**Processing steps:**
1. Check inbox: if `(paystackWebhookId, "pay-charges-paystack")` already processed, discard silently (idempotent).
2. Call `paymentGatewayPort.validateWebhookSignature(rawPayload, signature, paystackSecretKey)`. If invalid, throw `WebhookSignatureInvalidException`.
3. Parse the event type from `rawPayload`.
4. For `charge.success`: find `Charge` by gateway reference. Call `charge.markSuccessful(gatewayRef, rawPayload)`. Save and publish events via outbox.
5. For `charge.failed`: find `Charge`. Call `charge.markFailed(reason)`. Save and publish.
6. Record the webhook in the inbox tracker.

**Response**: `void`

---

#### `ProcessMoniepointWebhookCommand`

**Package**: `com.atlashub.pay.charges.application.commands.ProcessMoniepointWebhook`

```java
public record ProcessMoniepointWebhookCommand(
    String rawPayload,
    String signature,
    String moniepointWebhookId
) {}
```

**Handler**: `ProcessMoniepointWebhookHandler extends Command<ProcessMoniepointWebhookCommand, Void>`

Follows the same pattern as `ProcessPaystackWebhookHandler` using the Moniepoint secret key.

**Response**: `void`

---

#### `RefundChargeCommand`

**Package**: `com.atlashub.pay.charges.application.commands.RefundCharge`

```java
public record RefundChargeCommand(
    Long chargeId,
    BigDecimal refundAmount,
    String reason,
    Long requestedByUserId
) {}
```

**Handler**: `RefundChargeHandler extends Command<RefundChargeCommand, Void>`

**RBAC**: `pay:charges:refund`

**Processing steps:**
1. Load `Charge` by ID. Throw `ChargeNotFoundException` if absent.
2. Call `charge.refund(refundAmount)` — validates amount, registers `ChargeRefundInitiatedEvent`.
3. Save and publish events (the `pay:transfers` module will create a `Payout` when it consumes `ChargeRefundInitiatedEvent`).

**Response**: `void`

---

### Queries

Queries for charges are served by `pay:tx-query` (the unified read model). The `charges` submodule does not expose its own query endpoints to avoid duplication.

---

## Infrastructure Layer

### Persistence

**JPA Entity**: `ChargeJpa`
**Package**: `com.atlashub.pay.charges.infrastructure.persistence.entities`

```java
@Entity
@Table(name = "charges")
public class ChargeJpa {
    @Id @GeneratedValue Long id;
    Long organizationId;
    String customerId;
    BigDecimal amount;
    String currency;
    @Enumerated(EnumType.STRING) PaymentChannel channel;
    @Enumerated(EnumType.STRING) ChargeStatus status;
    @Column(unique = true) String reference;
    @Enumerated(EnumType.STRING) PaymentProvider provider;
    String checkoutUrl;
    String gatewayReference;
    String gatewayResponse;
    Long splitId;
    @Enumerated(EnumType.STRING) SourceSystem sourceSystem;
    String sourceReferenceId;
    @ElementCollection
    @CollectionTable(name = "charge_metadata")
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    Map<String, String> metadata;
    Long cashierId;
    ZonedDateTime createdAt;
    ZonedDateTime completedAt;
    @Version long version;   // optimistic locking
}
```

**Spring Data Repository**: `SpringDataChargeRepository`
```java
public interface SpringDataChargeRepository extends JpaRepository<ChargeJpa, Long> {
    Optional<ChargeJpa> findByReference(String reference);
    Optional<ChargeJpa> findByGatewayReference(String gatewayReference);
}
```

**Adapter**: `ChargeRepositoryAdapter implements ChargeRepository`
**Mapper**: `ChargeMapper`

---

### External Service Adapters

#### `PaystackGatewayAdapter implements PaymentGatewayPort`

**Package**: `com.atlashub.pay.charges.infrastructure.services`

| Method | Paystack Endpoint | Description |
|---|---|---|
| `initialize(...)` | `POST /transaction/initialize` | Initializes charge, returns `checkoutUrl` |
| `validateWebhookSignature(...)` | — (local HMAC-SHA512 computation) | Validates `X-Paystack-Signature` header |

---

#### `MoniepointGatewayAdapter implements PaymentGatewayPort`

**Package**: `com.atlashub.pay.charges.infrastructure.services`

| Method | Moniepoint Endpoint | Description |
|---|---|---|
| `initialize(...)` | `POST /v1/transactions/initiate` | Initiates terminal push request |
| `validateWebhookSignature(...)` | — (local HMAC computation) | Validates Moniepoint signature header |

---

## Presentation Layer

### Controller: `ChargesController`

**Package**: `com.atlashub.pay.charges.presentation.rest`

| Method | Path | Auth | RBAC | Request DTO | Response DTO |
|---|---|---|---|---|---|
| `POST` | `/api/v1/pay/charges` | Bearer JWT | `pay:charges:create` | `InitializeChargeRequest` | `InitializeChargeResponse` |
| `POST` | `/api/v1/pay/charges/{id}/refund` | Bearer JWT | `pay:charges:refund` | `RefundChargeRequest` | `ApiResponse<Void>` |
| `POST` | `/api/v1/webhooks/paystack` | HMAC signature | Public (HMAC-validated) | Raw body | `ApiResponse<Void>` |
| `POST` | `/api/v1/webhooks/moniepoint` | HMAC signature | Public (HMAC-validated) | Raw body | `ApiResponse<Void>` |

### DTOs

**Package**: `com.atlashub.pay.charges.presentation.dto`

**`InitializeChargeRequest`**
```java
public record InitializeChargeRequest(
    @NotNull @Positive BigDecimal amount,
    @NotBlank String currency,
    @NotBlank String channel,
    @NotBlank String reference,
    @NotBlank String provider,
    Long splitId,
    @NotBlank String sourceSystem,
    @NotBlank String sourceReferenceId,
    Map<String, String> metadata,
    String customerId,
    Long cashierId
) {}
```

**`InitializeChargeResponse`**
```java
public record InitializeChargeResponse(
    Long chargeId,
    String reference,
    String checkoutUrl,
    String status
) {}
```

**`RefundChargeRequest`**
```java
public record RefundChargeRequest(
    @NotNull @Positive BigDecimal refundAmount,
    @NotBlank String reason
) {}
```

---

## RBAC Table

| Permission | Granted To | Operation |
|---|---|---|
| `pay:charges:create` | `OWNER`, `ADMIN`, `CASHIER`, `DEVELOPER` | Initialize a new charge |
| `pay:charges:refund` | `OWNER`, `ADMIN`, `FINANCE` | Issue a refund on a completed charge |

---

## Maker-Checker

Not applicable. Charges are direct customer-initiated payments. No dual authorization is required.

---

## Socket Events

`ChargeSuccessfulEvent` and `ChargeFailedEvent` are consumed by the **`SelectiveWebSocketBroadcaster`**, which pushes real-time payment confirmation to the cashier's active session.

**Push pattern:**
```
ChargeSuccessfulEvent → SelectiveWebSocketBroadcaster → ws.convertAndSendToUser(
    cashierId.toString(),
    "/queue/notifications",
    PushNotification.of("PAYMENT_CONFIRMED", "Payment of ₦25,000.00 confirmed")
)

ChargeFailedEvent → SelectiveWebSocketBroadcaster → ws.convertAndSendToUser(
    cashierId.toString(),
    "/queue/notifications",
    PushNotification.of("PAYMENT_FAILED", "Payment failed: " + reason)
)
```

This submodule does **not** push to WebSocket directly. It publishes domain events; the `SelectiveWebSocketBroadcaster` decides whether to push.

---

## Domain Events Table

| Event | Kafka Topic | Published When | Consumed By |
|---|---|---|---|
| `ChargeSuccessfulEvent` | `pay-events` | Payment confirmed by gateway | `commerce`, `billing`, `pay:ledger`, `pay:webhooks`, `pay:tx-query`, `SelectiveWebSocketBroadcaster` |
| `ChargeFailedEvent` | `pay-events` | Payment declined or timed out | `commerce`, `notifications`, `pay:webhooks`, `pay:tx-query`, `SelectiveWebSocketBroadcaster` |
| `ChargeRefundInitiatedEvent` | `pay-events` | Refund initiated by operator | `pay:transfers`, `accounting`, `pay:tx-query` |

---

## Distributed Architecture

### Locking
- **Optimistic locking** (`@Version`) on `ChargeJpa` — adequate for the charge lifecycle (status transitions are rare concurrency targets).

### Idempotency
- **Reference idempotency**: `InitializeChargeHandler` checks for existing charge by `reference`. Duplicate calls return the existing charge.
- **Webhook inbox**: `ProcessPaystackWebhookHandler` and `ProcessMoniepointWebhookHandler` use `EventDeliveryTracker` keyed on `(webhookId, "pay-charges-paystack")` and `(webhookId, "pay-charges-moniepoint")` to discard duplicate webhook deliveries.

### Outbox
- `ChargeSuccessfulEvent`, `ChargeFailedEvent`, and `ChargeRefundInitiatedEvent` are written to the outbox table in the **same DB transaction** as the status transition — never lost even if the JVM crashes post-commit.

---

## Complete File List

```
com.atlashub.pay.charges/
├── application/
│   └── commands/
│       ├── InitializeCharge/
│       │   ├── InitializeChargeCommand.java
│       │   ├── InitializeChargeHandler.java
│       │   └── InitializeChargeResponse.java
│       ├── ProcessMoniepointWebhook/
│       │   ├── ProcessMoniepointWebhookCommand.java
│       │   └── ProcessMoniepointWebhookHandler.java
│       ├── ProcessPaystackWebhook/
│       │   ├── ProcessPaystackWebhookCommand.java
│       │   └── ProcessPaystackWebhookHandler.java
│       └── RefundCharge/
│           ├── RefundChargeCommand.java
│           └── RefundChargeHandler.java
├── domain/
│   ├── entities/
│   │   └── Charge.java
│   ├── events/
│   │   ├── ChargeFailedEvent.java
│   │   ├── ChargeRefundInitiatedEvent.java
│   │   └── ChargeSuccessfulEvent.java
│   ├── exceptions/
│   │   ├── ChargeNotFoundException.java
│   │   ├── DuplicateChargeReferenceException.java
│   │   ├── InvalidChargeStateException.java
│   │   ├── InvalidRefundAmountException.java
│   │   ├── WebhookAlreadyProcessedException.java
│   │   └── WebhookSignatureInvalidException.java
│   ├── ports/
│   │   └── PaymentGatewayPort.java
│   ├── repositories/
│   │   └── ChargeRepository.java
│   └── valueobject/
│       ├── ChargeStatus.java
│       ├── Money.java
│       ├── PaymentChannel.java
│       └── PaymentProvider.java
├── infrastructure/
│   ├── persistence/
│   │   ├── adapters/
│   │   │   └── ChargeRepositoryAdapter.java
│   │   ├── entities/
│   │   │   └── ChargeJpa.java
│   │   ├── mappers/
│   │   │   └── ChargeMapper.java
│   │   └── repositories/
│   │       └── SpringDataChargeRepository.java
│   └── services/
│       ├── MoniepointGatewayAdapter.java
│       └── PaystackGatewayAdapter.java
└── presentation/
    ├── dto/
    │   ├── InitializeChargeRequest.java
    │   ├── InitializeChargeResponse.java
    │   └── RefundChargeRequest.java
    └── rest/
        └── ChargesController.java
```
