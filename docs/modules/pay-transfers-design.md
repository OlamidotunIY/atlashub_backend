# Pay Transfers Design (`atlashub-pay:transfers`)

## Role & Purpose

The `transfers` submodule manages all **outbound bank transfers (payouts)** originating from the platform. Every salary disbursement, supplier payment, marketplace vendor disbursement, customer refund, loan disbursement, and inter-outlet float transfer is a `Payout` record managed by this submodule.

This submodule enforces the **Maker-Checker** pattern: any payout above the organization's configurable approval threshold (default ₦100,000) must be approved by a second authorized user before execution. It also owns the integration with outbound payment gateways (Paystack Transfers API, Moniepoint disbursement) via domain ports.

`transfers` does **not** manage ledger entries directly. After a payout completes, it publishes `PayoutCompletedEvent`, which is consumed by `pay:ledger` to post the corresponding double-entry journal.

---

## Domain Layer

### Aggregate Root: `Payout`

**Package**: `com.atlashub.pay.transfers.domain.entities`

```
Payout
├── id: Long
├── organizationId: Long
├── amount: Money
├── recipientBankCode: String
├── recipientAccountNumber: String
├── recipientAccountName: String
├── narration: String
├── status: PayoutStatus           ← PENDING_APPROVAL | APPROVED | PROCESSING | SUCCESSFUL | FAILED
├── reference: String              ← unique; client-supplied or system-generated
├── provider: PaymentProvider      ← PAYSTACK | MONIEPOINT
├── providerReference: String      ← nullable; set after gateway accepts the transfer
├── sourceSystem: SourceSystem     ← PAYROLL | COMMERCE_REFUND | SUPPLIER_PAYMENT | MANUAL | LOAN_DISBURSEMENT | VENDOR_DISBURSEMENT
├── sourceReferenceId: String      ← e.g., payrollRunId, salesOrderId, loanId
├── initiatedBy: Long              ← userId of the maker
├── approvedBy: Long               ← userId of the checker; nullable until approved
├── approvedAt: ZonedDateTime      ← nullable
├── failureReason: String          ← nullable; set on FAILED
├── createdAt: ZonedDateTime
└── completedAt: ZonedDateTime     ← nullable; set on SUCCESSFUL or FAILED
```

**State Machine:**
```
PENDING_APPROVAL ──approve()──► APPROVED ──markProcessing()──► PROCESSING
                                                                     │
                                           ┌────complete()───────────┘
                                           │         └──────fail()────┐
                                           ▼                          ▼
                                       SUCCESSFUL                  FAILED
```

> [!NOTE] Payouts below the org threshold bypass PENDING_APPROVAL and are created directly in APPROVED status, then immediately queued for execution.

**Business Methods:**

| Method | Inputs | Guard | Effect | Event Registered |
|---|---|---|---|---|
| `approve(approverId)` | `Long approverId` | `approverId` must not equal `initiatedBy` (self-approval); status must be `PENDING_APPROVAL` | sets `approvedBy`, `approvedAt`, `status = APPROVED` | `PayoutApprovedEvent` |
| `markProcessing()` | — | status must be `APPROVED` | sets `status = PROCESSING` | — |
| `complete(providerRef)` | `String providerRef` | status must be `PROCESSING` | sets `providerReference`, `status = SUCCESSFUL`, `completedAt = now()` | `PayoutCompletedEvent` |
| `fail(reason)` | `String reason` | status must be `PROCESSING` | sets `failureReason`, `status = FAILED`, `completedAt = now()` | `PayoutFailedEvent` |

**Exceptions thrown inside business methods:**

| Exception | When |
|---|---|
| `SelfApprovalNotAllowedException` | `approve()` called with `approverId == initiatedBy` |
| `InvalidPayoutStateException` | Any transition called when status is incompatible |

---

### Value Objects

**Package**: `com.atlashub.pay.transfers.domain.valueobject`

| Class | Fields | Description |
|---|---|---|
| `PayoutStatus` | enum: `PENDING_APPROVAL`, `APPROVED`, `PROCESSING`, `SUCCESSFUL`, `FAILED` | Full payout lifecycle |
| `PaymentProvider` | enum: `PAYSTACK`, `MONIEPOINT` | Which gateway executes the transfer |
| `SourceSystem` | enum: `PAYROLL`, `COMMERCE_REFUND`, `SUPPLIER_PAYMENT`, `MANUAL`, `LOAN_DISBURSEMENT`, `VENDOR_DISBURSEMENT` | Originating business context |

---

### Domain Events

**Package**: `com.atlashub.pay.transfers.domain.events`

All events are published to Kafka topic **`pay-events`**.

| Event | Published When | Consumed By |
|---|---|---|
| `PayoutApprovedEvent` | Checker approves a payout | `pay:transfers` internal (`PayoutApprovedListener` → `ExecutePayoutHandler`) |
| `PayoutCompletedEvent` | Payout confirmed as SUCCESSFUL by gateway | `hr` (mark salary disbursed), `accounting`, `notifications`, `pay:webhooks`, `pay:tx-query` |
| `PayoutFailedEvent` | Payout rejected or timed out by gateway | `hr` (revert payroll), `notifications`, `pay:webhooks`, `pay:tx-query` |
| `BulkPayoutCompletedEvent` | All payouts in a bulk batch reach terminal state | `hr` (mark payroll run DISBURSED) |
| `BulkPayoutFailedEvent` | One or more payouts in a bulk batch failed | `hr` (revert payroll run to APPROVED) |

**Event payload shape (example — `PayoutCompletedEvent`):**
```java
public record PayoutCompletedEvent(
    String eventId,
    String aggregateId,          // payoutId as String
    ZonedDateTime occurredAt,
    Payload payload
) {
    public record Payload(
        Long payoutId,
        Long organizationId,
        String reference,
        String providerReference,
        String sourceSystem,
        String sourceReferenceId,
        Long recipientUserId,    // nullable — set for salary/loan payouts
        BigDecimal amount,
        String currency
    ) {}
}
```

---

### Domain Exceptions

**Package**: `com.atlashub.pay.transfers.domain.exceptions`

```java
public class PayoutNotFoundException extends NotFoundException {
    public PayoutNotFoundException() { super("Payout not found"); }
    public PayoutNotFoundException(String message) { super(message); }
}

public class InsufficientFundsException extends BusinessRuleException {
    public InsufficientFundsException() { super("Insufficient funds for this payout"); }
    public InsufficientFundsException(String message) { super(message); }
}

public class InvalidBankDetailsException extends ValidationException {
    public InvalidBankDetailsException() { super("Invalid recipient bank details"); }
    public InvalidBankDetailsException(String message) { super(message); }
}

public class PayoutApprovalRequiredException extends BusinessRuleException {
    public PayoutApprovalRequiredException() { super("This payout requires approval before execution"); }
}

public class SelfApprovalNotAllowedException extends BusinessRuleException {
    public SelfApprovalNotAllowedException() { super("The initiator of a payout cannot approve it"); }
}

public class DuplicateReferenceException extends ConflictException {
    public DuplicateReferenceException() { super("A payout with this reference already exists"); }
    public DuplicateReferenceException(String message) { super(message); }
}

public class InvalidPayoutStateException extends BusinessRuleException {
    public InvalidPayoutStateException(String message) { super(message); }
}
```

---

### Domain Ports

**Package**: `com.atlashub.pay.transfers.domain.ports`

```java
/**
 * Abstraction over outbound payment gateway APIs (Paystack Transfers, Moniepoint).
 * Implemented per-provider in infrastructure/services/.
 */
public interface PaymentGatewayPort {

    /**
     * Validates that the given bank account number and bank code exist and returns
     * the account name as confirmed by the provider.
     */
    String resolveAccountName(String accountNumber, String bankCode);

    /**
     * Initiates a single transfer via the payment provider.
     * Returns the provider's own transaction reference.
     * This call is fire-and-forget; the final status arrives via provider webhook.
     */
    String initiateTransfer(String reference, BigDecimal amount, String currency,
                            String bankCode, String accountNumber, String narration);
}
```

---

### Domain Repository

**Package**: `com.atlashub.pay.transfers.domain.repositories`

```java
public interface PayoutRepository {
    Payout save(Payout payout);
    Optional<Payout> findById(Long id);
    Optional<Payout> findByReference(String reference);
    Optional<Payout> findByIdWithPessimisticLock(Long id);
    PageResult<Payout> findByOrganizationIdAndStatus(Long organizationId, PayoutStatus status, int page, int size);
    List<Payout> findBySourceSystemAndSourceReferenceId(SourceSystem sourceSystem, String sourceReferenceId);
}
```

---

## Application Layer

### Commands

#### `InitiatePayoutCommand`

**Package**: `com.atlashub.pay.transfers.application.commands.InitiatePayout`

```java
public record InitiatePayoutCommand(
    Long organizationId,
    Long initiatedByUserId,
    BigDecimal amount,
    String currency,
    String recipientBankCode,
    String recipientAccountNumber,
    String recipientAccountName,
    String narration,
    String reference,             // client-supplied idempotency key
    PaymentProvider provider,
    SourceSystem sourceSystem,
    String sourceReferenceId
) {}
```

**Handler**: `InitiatePayoutHandler extends Command<InitiatePayoutCommand, InitiatePayoutResponse>`

**RBAC**: `pay:transfers:initiate`

**Processing steps:**
1. Check `PayoutRepository.findByReference(reference)` — if exists, return existing result (idempotent).
2. Check org ledger balance (via `pay:ledger` read port) — if insufficient, throw `InsufficientFundsException`.
3. Resolve recipient account name via `PaymentGatewayPort.resolveAccountName(...)` if not supplied — throw `InvalidBankDetailsException` on failure.
4. Determine if payout requires approval: if `amount > orgApprovalThreshold` (loaded from org config), set `status = PENDING_APPROVAL`; otherwise `status = APPROVED`.
5. Persist `Payout` via `PayoutRepository.save(...)`.
6. If `status == APPROVED`, publish `PayoutApprovedEvent` directly so `PayoutApprovedListener` queues execution immediately.
7. Return `InitiatePayoutResponse` with payout ID and status.

**Response:**
```java
public record InitiatePayoutResponse(
    Long payoutId,
    String reference,
    String status,
    boolean requiresApproval
) {}
```

---

#### `ApprovePayoutCommand`

**Package**: `com.atlashub.pay.transfers.application.commands.ApprovePayout`

```java
public record ApprovePayoutCommand(
    Long payoutId,
    Long approverUserId
) {}
```

**Handler**: `ApprovePayoutHandler extends Command<ApprovePayoutCommand, Void>`

**RBAC**: `pay:transfers:approve`

**Maker-Checker enforcement**: `payout.approve(approverUserId)` — entity method throws `SelfApprovalNotAllowedException` if `approverUserId.equals(initiatedBy)`.

**Processing steps:**
1. Load `Payout` by ID with **pessimistic write lock** to prevent double approval. Throw `PayoutNotFoundException` if not found.
2. Call `payout.approve(approverUserId)` — registers `PayoutApprovedEvent`, transitions to `APPROVED`.
3. Save via repository.
4. Publish `PayoutApprovedEvent` via outbox.

**Response**: `void`

---

#### `ExecutePayoutCommand`

**Package**: `com.atlashub.pay.transfers.application.commands.ExecutePayout`

```java
public record ExecutePayoutCommand(
    Long payoutId
) {}
```

**Handler**: `ExecutePayoutHandler extends Command<ExecutePayoutCommand, Void>`

**RBAC**: System-internal only (triggered by `PayoutApprovedListener` — no HTTP endpoint).

**Processing steps:**
1. Load `Payout` by ID. Throw `PayoutNotFoundException` if not found.
2. Call `payout.markProcessing()` — transitions to `PROCESSING`.
3. Save the updated payout.
4. Call `paymentGatewayPort.initiateTransfer(...)` using the payout's provider. Capture `providerReference`.
5. If the gateway call throws, call `payout.fail(reason)` → save → publish `PayoutFailedEvent` via outbox.
6. Otherwise, gateway result is asynchronous — final status arrives via Paystack/Moniepoint webhook which calls `MarkPayoutSuccessfulHandler` or `MarkPayoutFailedHandler`.

**Response**: `void`

---

#### `MarkPayoutFailedCommand`

**Package**: `com.atlashub.pay.transfers.application.commands.MarkPayoutFailed`

```java
public record MarkPayoutFailedCommand(
    Long payoutId,
    String reason
) {}
```

**Handler**: `MarkPayoutFailedHandler extends Command<MarkPayoutFailedCommand, Void>`

**RBAC**: System-internal (called by gateway webhook adapter).

**Processing steps:**
1. Load `Payout` by ID. Throw `PayoutNotFoundException` if not found.
2. Call `payout.fail(reason)` → registers `PayoutFailedEvent`.
3. Save and publish via outbox.

**Response**: `void`

---

#### `InitiateBulkPayoutCommand`

**Package**: `com.atlashub.pay.transfers.application.commands.InitiateBulkPayout`

```java
public record InitiateBulkPayoutCommand(
    Long organizationId,
    Long initiatedByUserId,
    String bulkReference,          // correlates the batch (e.g., payrollRunId)
    SourceSystem sourceSystem,
    String sourceReferenceId,
    List<BulkPayoutItem> items
) {
    public record BulkPayoutItem(
        String recipientBankCode,
        String recipientAccountNumber,
        String recipientAccountName,
        BigDecimal amount,
        String currency,
        String narration,
        String itemReference       // unique per item in the batch
    ) {}
}
```

**Handler**: `InitiateBulkPayoutHandler extends Command<InitiateBulkPayoutCommand, InitiateBulkPayoutResponse>`

**RBAC**: `pay:transfers:initiate`

**Processing steps:**
1. Validate total batch amount against org ledger balance. Throw `InsufficientFundsException` if insufficient.
2. For each item, construct a `Payout` with `status = PENDING_APPROVAL` (bulk payouts always require approval, regardless of threshold).
3. Persist all `Payout` records.
4. Return response with batch reference and list of payout IDs.

**Response:**
```java
public record InitiateBulkPayoutResponse(
    String bulkReference,
    int totalCount,
    List<Long> payoutIds
) {}
```

---

### Queries

#### `ListPayoutsQuery`

**Package**: `com.atlashub.pay.transfers.application.queries.ListPayouts`

```java
public record ListPayoutsQuery(
    Long organizationId,
    PayoutStatus status,  // nullable filter
    int page,
    int size
) {}
```

**Handler**: `ListPayoutsHandler extends Query<ListPayoutsQuery, PageResult<PayoutResult>>`

**Returns**: `PageResult<PayoutResult>` — payouts are user-facing with potentially thousands of records per org; pagination is required.

---

#### `GetPayoutQuery`

**Package**: `com.atlashub.pay.transfers.application.queries.GetPayout`

```java
public record GetPayoutQuery(
    Long organizationId,
    Long payoutId
) {}
```

**Handler**: `GetPayoutHandler extends Query<GetPayoutQuery, PayoutResult>`

**Result:**
```java
public record PayoutResult(
    Long id,
    Long organizationId,
    BigDecimal amount,
    String currency,
    String recipientBankCode,
    String recipientAccountNumber,
    String recipientAccountName,
    String narration,
    String status,
    String reference,
    String provider,
    String providerReference,
    String sourceSystem,
    String sourceReferenceId,
    Long initiatedBy,
    Long approvedBy,
    ZonedDateTime approvedAt,
    String failureReason,
    ZonedDateTime createdAt,
    ZonedDateTime completedAt
) {}
```

---

#### `ListPayoutsBySourceQuery`

**Package**: `com.atlashub.pay.transfers.application.queries.ListPayoutsBySource`

```java
public record ListPayoutsBySourceQuery(
    SourceSystem sourceSystem,
    String sourceReferenceId
) {}
```

**Handler**: `ListPayoutsBySourceHandler extends Query<ListPayoutsBySourceQuery, List<PayoutResult>>`

**Returns**: `List<PayoutResult>` — always bounded by a specific source entity (e.g., one payroll run); never unbounded.

---

## Infrastructure Layer

### Persistence

**JPA Entity**: `PayoutJpa`
**Package**: `com.atlashub.pay.transfers.infrastructure.persistence.entities`

```java
@Entity
@Table(name = "payouts")
public class PayoutJpa {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payout_seq")
    @SequenceGenerator(name = "payout_seq", sequenceName = "payout_id_seq", allocationSize = 1)
    Long id;
    Long organizationId;
    BigDecimal amount;
    String currency;
    String recipientBankCode;
    String recipientAccountNumber;
    String recipientAccountName;
    String narration;
    @Enumerated(EnumType.STRING) PayoutStatus status;
    @Column(unique = true) String reference;
    @Enumerated(EnumType.STRING) PaymentProvider provider;
    String providerReference;
    @Enumerated(EnumType.STRING) SourceSystem sourceSystem;
    String sourceReferenceId;
    Long initiatedBy;
    Long approvedBy;
    ZonedDateTime approvedAt;
    String failureReason;
    ZonedDateTime createdAt;
    ZonedDateTime completedAt;
}
```

**Spring Data Repository**: `SpringDataPayoutRepository`
**Package**: `com.atlashub.pay.transfers.infrastructure.persistence.repositories`

```java
public interface SpringDataPayoutRepository extends JpaRepository<PayoutJpa, Long> {
    Optional<PayoutJpa> findByReference(String reference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PayoutJpa p WHERE p.id = :id")
    Optional<PayoutJpa> findByIdWithPessimisticLock(@Param("id") Long id);

    Page<PayoutJpa> findByOrganizationIdAndStatus(Long organizationId, PayoutStatus status, Pageable pageable);
    Page<PayoutJpa> findByOrganizationId(Long organizationId, Pageable pageable);
    List<PayoutJpa> findBySourceSystemAndSourceReferenceId(SourceSystem sourceSystem, String sourceReferenceId);
}
```

**Mapper**: `PayoutMapper`
**Package**: `com.atlashub.pay.transfers.infrastructure.persistence.mappers`

**Adapter**: `PayoutRepositoryAdapter implements PayoutRepository`
**Package**: `com.atlashub.pay.transfers.infrastructure.persistence.adapters`

---

### Kafka Listeners

#### `PayoutApprovedListener`

**Package**: `com.atlashub.pay.transfers.infrastructure.messaging.listeners`

```java
@Component
public class PayoutApprovedListener extends BaseKafkaEventListener {
    private static final String GROUP_ID = "pay-transfers-approved-group";

    @PostConstruct
    public void init() { registerSubscription("PayoutApprovedEvent", GROUP_ID); }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "PayoutApprovedEvent",
            PayoutApprovedPayload.class, log, GROUP_ID,
            e -> e instanceof TimeoutException,
            event -> {
                executePayoutHandler.execute(new ExecutePayoutCommand(
                    event.payload().payoutId()
                ));
            });
    }
}
```

**Topic**: `pay-events`
**Group ID**: `pay-transfers-approved-group`
**Event**: `PayoutApprovedEvent`
**Command Called**: `ExecutePayoutCommand` → `ExecutePayoutHandler`
**Flow**: Payout approved (by checker or auto-approved below threshold) → listener triggers gateway execution.

---

### External Service Adapters

#### `PaystackPayoutAdapter implements PaymentGatewayPort`

**Package**: `com.atlashub.pay.transfers.infrastructure.services`

Wraps the Paystack Transfers API. Uses Paystack's `POST /transfer` endpoint.

| Method | Paystack Endpoint | Description |
|---|---|---|
| `resolveAccountName(...)` | `GET /bank/resolve` | Resolves account name before initiating transfer |
| `initiateTransfer(...)` | `POST /transfer` | Initiates transfer; returns `transfer_code` as providerReference |

---

#### `MoniepointPayoutAdapter implements PaymentGatewayPort`

**Package**: `com.atlashub.pay.transfers.infrastructure.services`

Wraps the Moniepoint disbursement API. Used for payouts initiated via Moniepoint terminals.

| Method | Moniepoint Endpoint | Description |
|---|---|---|
| `resolveAccountName(...)` | `GET /v1/accounts/resolve` | Bank account name resolution |
| `initiateTransfer(...)` | `POST /v1/transfers` | Initiates outbound transfer |

---

## Presentation Layer

### Controller: `PayoutController`

**Package**: `com.atlashub.pay.transfers.presentation.rest`

```java
@RestController
@RequestMapping("/api/v1/pay/transfers")
@Tag(name = "Transfers")
public class PayoutController { ... }
```

| Method | Path | Auth | RBAC | Request DTO | Response DTO |
|---|---|---|---|---|---|
| `POST` | `/api/v1/pay/transfers` | Bearer JWT | `pay:transfers:initiate` | `InitiatePayoutRequest` | `ApiResponse<InitiatePayoutResponse>` |
| `POST` | `/api/v1/pay/transfers/bulk` | Bearer JWT | `pay:transfers:initiate` | `InitiateBulkPayoutRequest` | `ApiResponse<InitiateBulkPayoutResponse>` |
| `POST` | `/api/v1/pay/transfers/{id}/approve` | Bearer JWT | `pay:transfers:approve` | — | `ApiResponse<Void>` |
| `GET` | `/api/v1/pay/transfers` | Bearer JWT | `pay:transfers:read` | Query params: `status`, `page`, `size` | `ApiResponse<PageResult<PayoutResponse>>` |
| `GET` | `/api/v1/pay/transfers/{id}` | Bearer JWT | `pay:transfers:read` | — | `ApiResponse<PayoutResponse>` |

### DTOs

**Package**: `com.atlashub.pay.transfers.presentation.dto`

**`InitiatePayoutRequest`**
```java
public record InitiatePayoutRequest(
    @NotNull BigDecimal amount,
    @NotBlank String currency,
    @NotBlank String recipientBankCode,
    @NotBlank String recipientAccountNumber,
    String recipientAccountName,    // optional; resolved automatically if blank
    @NotBlank String narration,
    @NotBlank String reference,
    @NotNull PaymentProvider provider,
    @NotNull SourceSystem sourceSystem,
    String sourceReferenceId
) {}
```

**`InitiateBulkPayoutRequest`**
```java
public record InitiateBulkPayoutRequest(
    @NotBlank String bulkReference,
    @NotNull SourceSystem sourceSystem,
    String sourceReferenceId,
    @NotEmpty List<BulkPayoutItemRequest> items
) {
    public record BulkPayoutItemRequest(
        @NotBlank String recipientBankCode,
        @NotBlank String recipientAccountNumber,
        String recipientAccountName,
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String narration,
        @NotBlank String itemReference
    ) {}
}
```

**`PayoutResponse`**
```java
public record PayoutResponse(
    Long id,
    Long organizationId,
    BigDecimal amount,
    String currency,
    String recipientBankCode,
    String recipientAccountNumber,
    String recipientAccountName,
    String narration,
    String status,
    String reference,
    String provider,
    String providerReference,
    String sourceSystem,
    String sourceReferenceId,
    Long initiatedBy,
    Long approvedBy,
    ZonedDateTime approvedAt,
    String failureReason,
    ZonedDateTime createdAt,
    ZonedDateTime completedAt
) {}
```

---

## RBAC Table

| Permission | Granted To | Operation |
|---|---|---|
| `pay:transfers:initiate` | `OWNER`, `ADMIN`, `FINANCE` | Initiate single or bulk payout |
| `pay:transfers:approve` | `OWNER`, `ADMIN`, `FINANCE` (second user — cannot be same as initiator) | Approve a pending payout |
| `pay:transfers:read` | `OWNER`, `ADMIN`, `FINANCE`, `AUDITOR` | List and view payouts |

---

## Maker-Checker

**Which operations require it:** All payouts with `amount > orgApprovalThreshold` (configurable per organization, defaulting to ₦100,000).

**How it is enforced:**
1. `InitiatePayoutHandler` sets `status = PENDING_APPROVAL` when the threshold is exceeded. The payout is saved but not sent to the gateway.
2. A second user with `pay:transfers:approve` calls `POST /api/v1/pay/transfers/{id}/approve`.
3. `ApprovePayoutHandler` loads the payout with a **pessimistic write lock** and calls `payout.approve(approverUserId)`.
4. Inside `approve()`, if `approverId.equals(initiatedBy)`, `SelfApprovalNotAllowedException` is thrown — preventing self-approval at the domain entity level. This guard is not bypassed by any infrastructure code.
5. On successful approval, `PayoutApprovedEvent` is published, triggering `PayoutApprovedListener` → `ExecutePayoutHandler`.

---

## Socket Events

`PayoutCompletedEvent` triggers a WebSocket push to the recipient (for salary and loan payouts where `recipientUserId` is populated in the event payload):

| Event | Kafka Topic | WebSocket Destination | Payload |
|---|---|---|---|
| `PayoutCompletedEvent` | `pay-events` | `/user/{recipientUserId}/queue/notifications` | `{ type: "PAYOUT_COMPLETED", payoutId, amount, currency, narration }` |
| `PayoutFailedEvent` | `pay-events` | `/user/{initiatedBy}/queue/notifications` | `{ type: "PAYOUT_FAILED", payoutId, reference, reason }` |

The `SelectiveWebSocketBroadcaster` in the shared infrastructure consumes `pay-events` and decides which events to push.

---

## Domain Events Table

| Event | Kafka Topic | Published When | Consumed By |
|---|---|---|---|
| `PayoutApprovedEvent` | `pay-events` | Payout transitions to APPROVED | `pay:transfers` (self — `PayoutApprovedListener`) |
| `PayoutCompletedEvent` | `pay-events` | Gateway confirms transfer as SUCCESSFUL | `hr`, `accounting`, `notifications`, `pay:webhooks`, `pay:tx-query` |
| `PayoutFailedEvent` | `pay-events` | Gateway rejects transfer or times out | `hr`, `notifications`, `pay:webhooks`, `pay:tx-query` |
| `BulkPayoutCompletedEvent` | `pay-events` | All items in a bulk batch complete | `hr` (mark payroll run DISBURSED) |
| `BulkPayoutFailedEvent` | `pay-events` | One or more items in a bulk batch fail | `hr` (revert payroll run to APPROVED) |

---

## Distributed Architecture

### Locking
- **Pessimistic locking (`PESSIMISTIC_WRITE`)**: Applied to `Payout` during `ApprovePayoutHandler`. Prevents two concurrent checkers from both approving the same payout. Accounts are locked by ascending `id` order to avoid deadlocks.
- No locking on `InitiatePayoutHandler` — idempotency key (`reference`) guards against duplicates at DB constraint level.

### Idempotency
- **Client-supplied `reference`**: `SpringDataPayoutRepository` has a `UNIQUE` constraint on `reference`. `InitiatePayoutHandler` checks for an existing payout by reference first and returns the existing result without re-processing.
- **`Idempotency-Key` header**: All `POST` mutation endpoints require an `Idempotency-Key` HTTP header. The shared gateway layer deduplicates at HTTP level before the handler is called.

### Outbox
- `PayoutApprovedEvent`, `PayoutCompletedEvent`, `PayoutFailedEvent`, `BulkPayoutCompletedEvent`, `BulkPayoutFailedEvent` — written to the shared outbox table in the same DB transaction as the state change. These events drive financial downstream effects and must never be lost.

### Inbox
- `PayoutApprovedListener` uses `EventDeliveryTracker` keyed on `(eventId, "pay-transfers-approved-group")` to deduplicate Kafka redeliveries of `PayoutApprovedEvent`.

---

## Complete File List

```
com.atlashub.pay.transfers/
├── application/
│   ├── commands/
│   │   ├── ApprovePayout/
│   │   │   ├── ApprovePayoutCommand.java
│   │   │   └── ApprovePayoutHandler.java
│   │   ├── ExecutePayout/
│   │   │   ├── ExecutePayoutCommand.java
│   │   │   └── ExecutePayoutHandler.java
│   │   ├── InitiateBulkPayout/
│   │   │   ├── InitiateBulkPayoutCommand.java
│   │   │   ├── InitiateBulkPayoutHandler.java
│   │   │   └── InitiateBulkPayoutResponse.java
│   │   ├── InitiatePayout/
│   │   │   ├── InitiatePayoutCommand.java
│   │   │   ├── InitiatePayoutHandler.java
│   │   │   └── InitiatePayoutResponse.java
│   │   └── MarkPayoutFailed/
│   │       ├── MarkPayoutFailedCommand.java
│   │       └── MarkPayoutFailedHandler.java
│   └── queries/
│       ├── GetPayout/
│       │   ├── GetPayoutQuery.java
│       │   ├── GetPayoutHandler.java
│       │   └── PayoutResult.java
│       ├── ListPayouts/
│       │   ├── ListPayoutsQuery.java
│       │   └── ListPayoutsHandler.java
│       └── ListPayoutsBySource/
│           ├── ListPayoutsBySourceQuery.java
│           └── ListPayoutsBySourceHandler.java
├── domain/
│   ├── entities/
│   │   └── Payout.java
│   ├── events/
│   │   ├── BulkPayoutCompletedEvent.java
│   │   ├── BulkPayoutFailedEvent.java
│   │   ├── PayoutApprovedEvent.java
│   │   ├── PayoutCompletedEvent.java
│   │   └── PayoutFailedEvent.java
│   ├── exceptions/
│   │   ├── DuplicateReferenceException.java
│   │   ├── InsufficientFundsException.java
│   │   ├── InvalidBankDetailsException.java
│   │   ├── InvalidPayoutStateException.java
│   │   ├── PayoutApprovalRequiredException.java
│   │   ├── PayoutNotFoundException.java
│   │   └── SelfApprovalNotAllowedException.java
│   ├── ports/
│   │   └── PaymentGatewayPort.java
│   ├── repositories/
│   │   └── PayoutRepository.java
│   └── valueobject/
│       ├── PaymentProvider.java
│       ├── PayoutStatus.java
│       └── SourceSystem.java
├── infrastructure/
│   ├── messaging/
│   │   ├── events/
│   │   │   └── PayoutApprovedPayload.java
│   │   └── listeners/
│   │       └── PayoutApprovedListener.java
│   ├── persistence/
│   │   ├── adapters/
│   │   │   └── PayoutRepositoryAdapter.java
│   │   ├── entities/
│   │   │   └── PayoutJpa.java
│   │   ├── mappers/
│   │   │   └── PayoutMapper.java
│   │   └── repositories/
│   │       └── SpringDataPayoutRepository.java
│   └── services/
│       ├── MoniepointPayoutAdapter.java
│       └── PaystackPayoutAdapter.java
└── presentation/
    ├── dto/
    │   ├── InitiateBulkPayoutRequest.java
    │   ├── InitiatePayoutRequest.java
    │   └── PayoutResponse.java
    └── rest/
        └── PayoutController.java
```
