# Pay Accounts Design (`atlashub-pay:accounts`)

## Role & Purpose

The `accounts` submodule manages **virtual bank accounts (NUBANs)** issued to organizations and their end-customers via the Anchor banking-as-a-service API. Every organization that passes compliance onboarding is automatically issued an Anchor-backed NUBAN. Organizations offering B2B2C collection products can also provision dedicated NUBANs per end-customer.

This submodule does **not** manage balances — balance tracking is the responsibility of `pay:ledger`. The accounts submodule owns the lifecycle of the virtual account record itself: issuance, activation, suspension, and closure.

---

## Domain Layer

### Aggregate Root: `VirtualAccount`

**Package**: `com.atlashub.pay.accounts.domain.entities`

```
VirtualAccount
├── id: Long
├── organizationId: Long
├── ownerType: OwnerType           ← ORGANIZATION | CUSTOMER
├── customerId: String             ← nullable; only set for CUSTOMER-owned accounts
├── accountName: String
├── bankName: String               ← nullable until activated
├── nuban: String                  ← actual 10-digit bank account number; nullable until activated
├── bankCode: String
├── anchorAccountId: String        ← Anchor's internal reference; set on issuance
├── currency: Currency             ← NGN only for MVP
├── status: VirtualAccountStatus   ← PENDING_ISSUANCE → ACTIVE → SUSPENDED | CLOSED
├── createdAt: ZonedDateTime
└── activatedAt: ZonedDateTime     ← nullable; set by activate()
```

**State Machine:**
```
PENDING_ISSUANCE ──activate()──► ACTIVE ──suspend()──► SUSPENDED
                                   │                        │
                                   └────────close()─────────┘
                                              │
                                              ▼
                                           CLOSED
```

**Invariants:**
- `nuban` and `bankName` are `null` until `activate()` is called.
- `activate()` may only be called when `status == PENDING_ISSUANCE`.
- `suspend()` may only be called when `status == ACTIVE`.
- `close()` may be called from `ACTIVE` or `SUSPENDED`.
- Once `CLOSED`, no further transitions are allowed.

**Business Methods:**

| Method | Inputs | Guard | Effect | Event Registered |
|---|---|---|---|---|
| `activate(nuban, bankName)` | `String nuban`, `String bankName` | status must be `PENDING_ISSUANCE` | sets `nuban`, `bankName`, `activatedAt = now()`, `status = ACTIVE` | `VirtualAccountActivatedEvent` |
| `suspend()` | — | status must be `ACTIVE` | sets `status = SUSPENDED` | `VirtualAccountSuspendedEvent` |
| `close()` | — | status must be `ACTIVE` or `SUSPENDED` | sets `status = CLOSED` | `VirtualAccountClosedEvent` |

**Exceptions thrown inside business methods:**

| Exception | When |
|---|---|
| `VirtualAccountAlreadyActiveException` | `activate()` called on non-`PENDING_ISSUANCE` account |
| `VirtualAccountNotActiveException` | `suspend()` called on non-`ACTIVE` account |
| `VirtualAccountAlreadyClosedException` | Any transition attempted on a `CLOSED` account |

---

### Value Objects

**Package**: `com.atlashub.pay.accounts.domain.valueobject`

| Class | Fields | Description |
|---|---|---|
| `OwnerType` | enum: `ORGANIZATION`, `CUSTOMER` | Distinguishes org-level vs per-customer NUBANs |
| `VirtualAccountStatus` | enum: `PENDING_ISSUANCE`, `ACTIVE`, `SUSPENDED`, `CLOSED` | Full lifecycle |

---

### Domain Events

**Package**: `com.atlashub.pay.accounts.domain.events`

All events are published to Kafka topic **`pay-events`**.

| Event | Published When | Consumed By |
|---|---|---|
| `VirtualAccountActivatedEvent` | NUBAN is assigned and account goes ACTIVE | `notifications` (alert org admin), `pay:ledger` (bootstrap ledger accounts for org) |
| `VirtualAccountSuspendedEvent` | Account suspended | `notifications` |
| `VirtualAccountClosedEvent` | Account closed | `notifications` |
| `WalletFundedEvent` | Anchor collection webhook received and credited | `accounting`, `notifications`, `pay:tx-query` |

**Event payload shape (example — `VirtualAccountActivatedEvent`):**
```java
public record VirtualAccountActivatedEvent(
    String eventId,
    String aggregateId,          // virtualAccountId as String
    ZonedDateTime occurredAt,
    Payload payload
) {
    public record Payload(
        Long virtualAccountId,
        Long organizationId,
        String ownerType,
        String customerId,       // nullable
        String nuban,
        String bankName,
        String bankCode,
        String currency
    ) {}
}
```

---

### Domain Exceptions

**Package**: `com.atlashub.pay.accounts.domain.exceptions`

```java
public class VirtualAccountNotFoundException extends NotFoundException {
    public VirtualAccountNotFoundException() { super("Virtual account not found"); }
    public VirtualAccountNotFoundException(String message) { super(message); }
}

public class VirtualAccountAlreadyActiveException extends BusinessRuleException {
    public VirtualAccountAlreadyActiveException() { super("Virtual account is already active"); }
}

public class VirtualAccountNotActiveException extends BusinessRuleException {
    public VirtualAccountNotActiveException() { super("Virtual account is not active"); }
}

public class VirtualAccountAlreadyClosedException extends BusinessRuleException {
    public VirtualAccountAlreadyClosedException() { super("Virtual account is already closed"); }
}

public class VirtualAccountInactiveException extends BusinessRuleException {
    public VirtualAccountInactiveException() { super("Virtual account is not active"); }
}
```

---

### Domain Ports

**Package**: `com.atlashub.pay.accounts.domain.ports`

```java
public interface AnchorPort {
    /**
     * Calls Anchor API to create a virtual account linked to AtlasHub's pool account.
     * Returns Anchor's internal account reference ID immediately.
     * The NUBAN itself arrives asynchronously via Anchor's webhook.
     */
    String issueVirtualAccount(String accountName, String currency, String organizationId);

    /**
     * Issues a dedicated customer virtual account linked to the organization's pool.
     */
    String issueCustomerVirtualAccount(String customerName, String email, String organizationId, String customerId);
}
```

---

### Domain Repository

**Package**: `com.atlashub.pay.accounts.domain.repositories`

```java
public interface VirtualAccountRepository {
    VirtualAccount save(VirtualAccount account);
    Optional<VirtualAccount> findById(Long id);
    Optional<VirtualAccount> findByAnchorAccountId(String anchorAccountId);
    Optional<VirtualAccount> findByOrganizationIdAndOwnerType(Long organizationId, OwnerType ownerType);
    Optional<VirtualAccount> findByOrganizationIdAndCustomerId(Long organizationId, String customerId);
    List<VirtualAccount> findAllByOrganizationId(Long organizationId);
}
```

---

## Application Layer

### Commands

#### `IssueVirtualAccountCommand`

**Package**: `com.atlashub.pay.accounts.application.commands.IssueVirtualAccount`

```java
public record IssueVirtualAccountCommand(
    Long organizationId,
    String accountName,
    String currency
) {}
```

**Handler**: `IssueVirtualAccountHandler extends Command<IssueVirtualAccountCommand, Void>`

**RBAC**: System-internal only (triggered by listener — no direct RBAC check). Listener is `OrganizationComplianceApprovedListener`.

**Processing steps:**
1. Verify no existing `ACTIVE` or `PENDING_ISSUANCE` org-level virtual account exists for `organizationId`. If one exists, throw `VirtualAccountAlreadyExistsException`.
2. Call `anchorPort.issueVirtualAccount(accountName, currency, organizationId)` to get `anchorAccountId`.
3. Construct `VirtualAccount` with `status = PENDING_ISSUANCE`, `anchorAccountId` set, `nuban = null`.
4. Save via `VirtualAccountRepository`.
5. No events published here — activation event is published later by `ActivateVirtualAccountHandler`.

**Response**: `void`

---

#### `ActivateVirtualAccountCommand`

**Package**: `com.atlashub.pay.accounts.application.commands.ActivateVirtualAccount`

```java
public record ActivateVirtualAccountCommand(
    String anchorAccountId,
    String nuban,
    String bankName
) {}
```

**Handler**: `ActivateVirtualAccountHandler extends Command<ActivateVirtualAccountCommand, Void>`

**Triggered by**: `AnchorWebhookAdapter` when Anchor sends the account activation webhook.

**Processing steps:**
1. Load `VirtualAccount` by `anchorAccountId`. If not found, throw `VirtualAccountNotFoundException`.
2. Call `account.activate(nuban, bankName)` — this transitions status and registers `VirtualAccountActivatedEvent`.
3. Save the updated account.
4. Publish domain events via outbox.

**Response**: `void`

---

#### `SuspendVirtualAccountCommand`

**Package**: `com.atlashub.pay.accounts.application.commands.SuspendVirtualAccount`

```java
public record SuspendVirtualAccountCommand(
    Long virtualAccountId,
    Long requestedByUserId
) {}
```

**Handler**: `SuspendVirtualAccountHandler extends Command<SuspendVirtualAccountCommand, Void>`

**RBAC**: `pay:accounts:suspend`

**Processing steps:**
1. Load `VirtualAccount` by `virtualAccountId`. If not found, throw `VirtualAccountNotFoundException`.
2. Call `account.suspend()` — registers `VirtualAccountSuspendedEvent`.
3. Save and publish events.

**Response**: `void`

---

#### `CloseVirtualAccountCommand`

**Package**: `com.atlashub.pay.accounts.application.commands.CloseVirtualAccount`

```java
public record CloseVirtualAccountCommand(
    Long virtualAccountId,
    Long requestedByUserId
) {}
```

**Handler**: `CloseVirtualAccountHandler extends Command<CloseVirtualAccountCommand, Void>`

**RBAC**: `pay:accounts:close`

**Processing steps:**
1. Load `VirtualAccount` by `virtualAccountId`. If not found, throw `VirtualAccountNotFoundException`.
2. Call `account.close()` — registers `VirtualAccountClosedEvent`.
3. Save and publish events.

**Response**: `void`

---

#### `IssueCustomerVirtualAccountCommand`

**Package**: `com.atlashub.pay.accounts.application.commands.IssueCustomerVirtualAccount`

```java
public record IssueCustomerVirtualAccountCommand(
    Long organizationId,
    String customerId,
    String customerName,
    String email,
    String currency
) {}
```

**Handler**: `IssueCustomerVirtualAccountHandler extends Command<IssueCustomerVirtualAccountCommand, IssueCustomerVirtualAccountResponse>`

**RBAC**: `pay:accounts:create`

**Processing steps:**
1. Verify no existing active customer virtual account for `(organizationId, customerId)`. If one exists, return its details (idempotent).
2. Call `anchorPort.issueCustomerVirtualAccount(customerName, email, organizationId, customerId)`.
3. Construct `VirtualAccount` with `ownerType = CUSTOMER`, `customerId` set, `status = PENDING_ISSUANCE`.
4. Save via repository.
5. Return `IssueCustomerVirtualAccountResponse` with the account ID and pending status.

**Response**: `IssueCustomerVirtualAccountResponse`
```java
public record IssueCustomerVirtualAccountResponse(
    Long virtualAccountId,
    String status,
    String anchorAccountId
) {}
```

---

### Queries

#### `GetVirtualAccountQuery`

**Package**: `com.atlashub.pay.accounts.application.queries.GetVirtualAccount`

```java
public record GetVirtualAccountQuery(
    Long organizationId,
    Long virtualAccountId
) {}
```

**Handler**: `GetVirtualAccountHandler extends Query<GetVirtualAccountQuery, VirtualAccountResult>`

**Result**:
```java
public record VirtualAccountResult(
    Long id,
    Long organizationId,
    String ownerType,
    String customerId,
    String accountName,
    String bankName,
    String nuban,
    String bankCode,
    String currency,
    String status,
    ZonedDateTime createdAt,
    ZonedDateTime activatedAt
) {}
```

**Returns**: `VirtualAccountResult` (single entity — not pageable).

---

#### `ListVirtualAccountsQuery`

**Package**: `com.atlashub.pay.accounts.application.queries.ListVirtualAccounts`

```java
public record ListVirtualAccountsQuery(
    Long organizationId,
    String ownerType   // nullable filter
) {}
```

**Handler**: `ListVirtualAccountsHandler extends Query<ListVirtualAccountsQuery, List<VirtualAccountResult>>`

**Returns**: `List<VirtualAccountResult>` — bounded per org (orgs do not have thousands of virtual accounts; `PageResult` not needed).

---

## Infrastructure Layer

### Persistence

**JPA Entity**: `VirtualAccountJpa`
**Package**: `com.atlashub.pay.accounts.infrastructure.persistence.entities`

```java
@Entity
@Table(name = "virtual_accounts")
@Version long version;  // optimistic locking
public class VirtualAccountJpa {
    @Id @GeneratedValue Long id;
    Long organizationId;
    @Enumerated(EnumType.STRING) OwnerType ownerType;
    String customerId;
    String accountName;
    String bankName;
    String nuban;
    String bankCode;
    String anchorAccountId;
    String currency;
    @Enumerated(EnumType.STRING) VirtualAccountStatus status;
    ZonedDateTime createdAt;
    ZonedDateTime activatedAt;
    long version;
}
```

**Spring Data Repository**: `SpringDataVirtualAccountRepository`
**Package**: `com.atlashub.pay.accounts.infrastructure.persistence.repositories`

```java
public interface SpringDataVirtualAccountRepository extends JpaRepository<VirtualAccountJpa, Long> {
    Optional<VirtualAccountJpa> findByAnchorAccountId(String anchorAccountId);
    Optional<VirtualAccountJpa> findByOrganizationIdAndOwnerType(Long organizationId, OwnerType ownerType);
    Optional<VirtualAccountJpa> findByOrganizationIdAndCustomerId(Long organizationId, String customerId);
    List<VirtualAccountJpa> findAllByOrganizationId(Long organizationId);
}
```

**Mapper**: `VirtualAccountMapper`
**Package**: `com.atlashub.pay.accounts.infrastructure.persistence.mappers`

**Adapter**: `VirtualAccountRepositoryAdapter implements VirtualAccountRepository`
**Package**: `com.atlashub.pay.accounts.infrastructure.persistence.adapters`

---

### Kafka Listeners

#### `OrganizationComplianceApprovedListener`

**Package**: `com.atlashub.pay.accounts.infrastructure.messaging.listeners`

```java
@Component
public class OrganizationComplianceApprovedListener extends BaseKafkaEventListener {
    private static final String GROUP_ID = "pay-accounts-compliance-group";

    @PostConstruct
    public void init() { registerSubscription("OrganizationComplianceApprovedEvent", GROUP_ID); }

    @KafkaListener(topics = "compliance-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "OrganizationComplianceApprovedEvent",
            OrganizationComplianceApprovedPayload.class, log, GROUP_ID,
            e -> e instanceof TimeoutException,
            event -> {
                issueVirtualAccountHandler.execute(new IssueVirtualAccountCommand(
                    event.payload().organizationId(),
                    event.payload().organizationName(),
                    "NGN"
                ));
            });
    }
}
```

**Topic**: `compliance-events`
**Group ID**: `pay-accounts-compliance-group`
**Event**: `OrganizationComplianceApprovedEvent`
**Command Called**: `IssueVirtualAccountCommand` → `IssueVirtualAccountHandler`
**Flow**: Compliance approval → auto-issue org virtual account → Anchor issues NUBAN asynchronously.

---

### External Service Adapters

#### `AnchorVirtualAccountAdapter implements AnchorPort`

**Package**: `com.atlashub.pay.accounts.infrastructure.services`

Wraps the Anchor REST API. Uses an HTTP client with OAuth2 bearer tokens to call Anchor endpoints. Translates Anchor API responses into domain primitives.

**Methods:**

| Method | Anchor Endpoint | Description |
|---|---|---|
| `issueVirtualAccount(...)` | `POST /v1/accounts` | Creates a pool sub-account for an org |
| `issueCustomerVirtualAccount(...)` | `POST /v1/customers/{customerId}/accounts` | Issues a dedicated customer NUBAN |

---

#### `AnchorWebhookAdapter`

**Package**: `com.atlashub.pay.accounts.infrastructure.services`

Receives and validates inbound webhooks from Anchor (HMAC-SHA256 signature on `X-Anchor-Signature` header).

Handles two webhook types:
1. **Account activated** → calls `ActivateVirtualAccountHandler`
2. **Collection (bank transfer received)** → calls `CreditWalletFromTransferHandler` in `pay:ledger`

---

## Presentation Layer

### Controller: `VirtualAccountController`

**Package**: `com.atlashub.pay.accounts.presentation.rest`

```java
@RestController
@RequestMapping("/api/v1/pay/accounts")
@Tag(name = "Virtual Accounts")
public class VirtualAccountController { ... }
```

| Method | Path | Auth | RBAC | Request DTO | Response DTO |
|---|---|---|---|---|---|
| `POST` | `/api/v1/pay/accounts/customer` | Bearer JWT | `pay:accounts:create` | `IssueCustomerVirtualAccountRequest` | `VirtualAccountResponse` |
| `GET` | `/api/v1/pay/accounts` | Bearer JWT | `pay:accounts:read` | Query params: `ownerType` | `List<VirtualAccountResponse>` |
| `GET` | `/api/v1/pay/accounts/{id}` | Bearer JWT | `pay:accounts:read` | — | `VirtualAccountResponse` |
| `POST` | `/api/v1/pay/accounts/{id}/suspend` | Bearer JWT | `pay:accounts:suspend` | — | `ApiResponse<Void>` |
| `POST` | `/api/v1/pay/accounts/{id}/close` | Bearer JWT | `pay:accounts:close` | — | `ApiResponse<Void>` |
| `POST` | `/api/v1/webhooks/anchor` | HMAC signature | Public (HMAC-validated) | Raw body | `ApiResponse<Void>` |

### DTOs

**Package**: `com.atlashub.pay.accounts.presentation.dto`

**`IssueCustomerVirtualAccountRequest`**
```java
public record IssueCustomerVirtualAccountRequest(
    @NotBlank String customerId,
    @NotBlank String customerName,
    @Email @NotBlank String email,
    @NotBlank String currency
) {}
```

**`VirtualAccountResponse`**
```java
public record VirtualAccountResponse(
    Long id,
    Long organizationId,
    String ownerType,
    String customerId,
    String accountName,
    String bankName,
    String nuban,
    String bankCode,
    String currency,
    String status,
    ZonedDateTime createdAt,
    ZonedDateTime activatedAt
) {}
```

---

## RBAC Table

| Permission | Granted To | Operation |
|---|---|---|
| `pay:accounts:create` | `OWNER`, `ADMIN`, `DEVELOPER` | Issue customer virtual accounts |
| `pay:accounts:read` | `OWNER`, `ADMIN`, `FINANCE`, `DEVELOPER` | List and view virtual accounts |
| `pay:accounts:suspend` | `OWNER`, `ADMIN` | Suspend an active account |
| `pay:accounts:close` | `OWNER` | Permanently close an account |

---

## Maker-Checker

Not applicable for this submodule. Account issuance is system-initiated (driven by compliance approval). Suspend and Close operations do not require dual authorization.

---

## Socket Events

`ChargeSuccessful` and `ChargeFailed` events from `pay:charges` are pushed to cashier sessions via the **`SelectiveWebSocketBroadcaster`**. This submodule does not publish events that trigger WebSocket push. The `WalletFundedEvent` is informational — it updates a balance display on next query, no real-time push.

---

## Domain Events Table

| Event | Kafka Topic | Published When | Consumed By |
|---|---|---|---|
| `VirtualAccountActivatedEvent` | `pay-events` | Anchor confirms NUBAN assignment | `notifications`, `pay:ledger` |
| `VirtualAccountSuspendedEvent` | `pay-events` | Account suspended by admin | `notifications` |
| `VirtualAccountClosedEvent` | `pay-events` | Account permanently closed | `notifications` |
| `WalletFundedEvent` | `pay-events` | Anchor collection webhook processed | `accounting`, `notifications`, `pay:tx-query` |

---

## Distributed Architecture

### Locking
- **Optimistic locking** (`@Version`) on `VirtualAccountJpa` — sufficient because concurrent modifications to a single account are rare.
- No pessimistic locking needed (balance operations are in `pay:ledger`).

### Idempotency
- `AnchorWebhookAdapter` uses `EventDeliveryTracker` keyed on `(anchorWebhookId, "pay-accounts")` to deduplicate Anchor webhook redeliveries.
- `IssueVirtualAccountHandler` is idempotent: if a `PENDING_ISSUANCE` or `ACTIVE` account already exists for the org, it returns without re-calling Anchor.

### Outbox
- `VirtualAccountActivatedEvent` and `WalletFundedEvent` are written to the outbox table in the same DB transaction as the state change — never published mid-flight.

---

## Complete File List

```
com.atlashub.pay.accounts/
├── application/
│   ├── commands/
│   │   ├── ActivateVirtualAccount/
│   │   │   ├── ActivateVirtualAccountCommand.java
│   │   │   └── ActivateVirtualAccountHandler.java
│   │   ├── CloseVirtualAccount/
│   │   │   ├── CloseVirtualAccountCommand.java
│   │   │   └── CloseVirtualAccountHandler.java
│   │   ├── IssueCustomerVirtualAccount/
│   │   │   ├── IssueCustomerVirtualAccountCommand.java
│   │   │   ├── IssueCustomerVirtualAccountHandler.java
│   │   │   └── IssueCustomerVirtualAccountResponse.java
│   │   ├── IssueVirtualAccount/
│   │   │   ├── IssueVirtualAccountCommand.java
│   │   │   └── IssueVirtualAccountHandler.java
│   │   └── SuspendVirtualAccount/
│   │       ├── SuspendVirtualAccountCommand.java
│   │       └── SuspendVirtualAccountHandler.java
│   └── queries/
│       ├── GetVirtualAccount/
│       │   ├── GetVirtualAccountQuery.java
│       │   ├── GetVirtualAccountHandler.java
│       │   └── VirtualAccountResult.java
│       └── ListVirtualAccounts/
│           ├── ListVirtualAccountsQuery.java
│           ├── ListVirtualAccountsHandler.java
│           └── VirtualAccountResult.java  (shared or same record)
├── domain/
│   ├── entities/
│   │   └── VirtualAccount.java
│   ├── events/
│   │   ├── VirtualAccountActivatedEvent.java
│   │   ├── VirtualAccountClosedEvent.java
│   │   ├── VirtualAccountSuspendedEvent.java
│   │   └── WalletFundedEvent.java
│   ├── exceptions/
│   │   ├── VirtualAccountAlreadyActiveException.java
│   │   ├── VirtualAccountAlreadyClosedException.java
│   │   ├── VirtualAccountAlreadyExistsException.java
│   │   ├── VirtualAccountInactiveException.java
│   │   └── VirtualAccountNotFoundException.java
│   ├── ports/
│   │   └── AnchorPort.java
│   ├── repositories/
│   │   └── VirtualAccountRepository.java
│   └── valueobject/
│       ├── OwnerType.java
│       └── VirtualAccountStatus.java
├── infrastructure/
│   ├── messaging/
│   │   ├── events/
│   │   │   └── OrganizationComplianceApprovedPayload.java
│   │   └── listeners/
│   │       └── OrganizationComplianceApprovedListener.java
│   ├── persistence/
│   │   ├── adapters/
│   │   │   └── VirtualAccountRepositoryAdapter.java
│   │   ├── entities/
│   │   │   └── VirtualAccountJpa.java
│   │   ├── mappers/
│   │   │   └── VirtualAccountMapper.java
│   │   └── repositories/
│   │       └── SpringDataVirtualAccountRepository.java
│   └── services/
│       ├── AnchorVirtualAccountAdapter.java
│       └── AnchorWebhookAdapter.java
└── presentation/
    ├── dto/
    │   ├── IssueCustomerVirtualAccountRequest.java
    │   └── VirtualAccountResponse.java
    └── rest/
        └── VirtualAccountController.java
```
