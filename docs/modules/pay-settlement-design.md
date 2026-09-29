# Pay Settlement Design (`atlashub-pay:settlement`)

## Role & Purpose

The `settlement` subpackage tracks batches of funds settled by payment providers (Paystack, Moniepoint) to the organization's linked bank account. When a provider settles, they send a webhook; this module records the settlement and posts the corresponding ledger credit.

Gradle module: `atlashub-pay:settlement`  
Package: `com.atlashub.pay.settlement`

---

## Domain Layer

### `Settlement` (Aggregate Root)

**Package:** `com.atlashub.pay.settlement.domain.entities`

```
Settlement
├── id                    : Long
├── organizationId        : Long
├── provider              : PaymentProvider     ← PAYSTACK | MONIEPOINT
├── providerSettlementId  : String              ← provider's settlement batch reference
├── amount                : Money
├── settledAt             : ZonedDateTime
├── status                : SettlementStatus    ← PENDING | CONFIRMED | DISPUTED
└── description           : String
```

**Business methods (on entity):**

| Method | Guard | Events | Exceptions |
|---|---|---|---|
| `confirm()` | status == PENDING | `SettlementConfirmedEvent` | `InvalidSettlementStateException` |
| `dispute(reason)` | status == PENDING or CONFIRMED | `SettlementDisputedEvent` | `InvalidSettlementStateException` |
| `resolveDispute()` | status == DISPUTED | — | — |

**On `confirm()`:** The handler also calls `PostLedgerTransactionHandler` to credit the organization's Operating Account: Dr Bank Account, Cr Operating Account.

---

### Domain Events — `com.atlashub.pay.settlement.domain.events`

| Event | Published When | Consumed By | Topic |
|---|---|---|---|
| `SettlementConfirmedEvent` | Settlement confirmed | `accounting:gl` (Dr Bank, Cr Clearing), `notifications` | `pay-events` |
| `SettlementDisputedEvent` | Settlement disputed | `notifications` (alert finance team) | `pay-events` |

---

### Domain Exceptions — `com.atlashub.pay.settlement.domain.exceptions`

```java
public class SettlementNotFoundException extends NotFoundException {
    public SettlementNotFoundException(Long id) { super("Settlement not found: " + id); }
    public SettlementNotFoundException(String providerSettlementId) {
        super("Settlement not found: " + providerSettlementId);
    }
}
public class InvalidSettlementStateException extends BusinessRuleException {
    public InvalidSettlementStateException(String message) { super(message); }
}
public class DuplicateSettlementException extends ConflictException {
    public DuplicateSettlementException(String providerSettlementId) {
        super("Settlement already recorded: " + providerSettlementId);
    }
}
```

---

## Application Layer

### Commands — `com.atlashub.pay.settlement.application.commands`

#### `RecordSettlementCommand`
```java
record RecordSettlementCommand(
    Long organizationId, PaymentProvider provider,
    String providerSettlementId, Money amount,
    ZonedDateTime settledAt, String description
)
```
**Handler:** `RecordSettlementHandler` | **Response:** `Long settlementId`  
**Invocation source:** Provider webhook listener (automatic)  
**Flow:**
1. Check `providerSettlementId` uniqueness → `DuplicateSettlementException`
2. Create `Settlement` (status = PENDING)
3. `repository.save()`

---

#### `ConfirmSettlementCommand`
```java
record ConfirmSettlementCommand(Long settlementId)
```
**Handler:** `ConfirmSettlementHandler` | **Response:** `void`  
**Invocation source:** Controller (finance manager confirms) OR auto-confirm webhook listener  
**Flow:**
1. Load `Settlement` → `settlement.confirm()`
2. Post ledger transaction: Dr Bank Account, Cr Operating Account (via `PostLedgerTransactionHandler`)
3. `repository.save()` → `SettlementConfirmedEvent` published

---

#### `DisputeSettlementCommand`
```java
record DisputeSettlementCommand(Long settlementId, String reason)
```
**Handler:** `DisputeSettlementHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('pay:settlement:manage')")`

---

### Queries — `com.atlashub.pay.settlement.application.queries`

#### `ListSettlementsQuery`
```java
record ListSettlementsQuery(Long organizationId, SettlementStatus status,
                            ZonedDateTime dateFrom, ZonedDateTime dateTo, int page, int size)
```
**Handler:** `ListSettlementsHandler` | **Result:** `PageResult<SettlementResult>`  
**Justification:** Settlements accumulate over time — pagination required.

---

#### `GetSettlementDetailsQuery`
```java
record GetSettlementDetailsQuery(Long settlementId)
```
**Handler:** `GetSettlementDetailsHandler` | **Result:** `SettlementResult`

`SettlementResult`: `id`, `organizationId`, `provider`, `providerSettlementId`, `amount`, `settledAt`, `status`, `description`

---

## Infrastructure Layer

### Persistence

| JPA Entity | Table | Locking |
|---|---|---|
| `SettlementJpaEntity` | `pay_settlements` | `@Version` optimistic |

**Spring Data:**
```
SpringDataSettlementRepository
  + findByProviderSettlementId(String id): Optional<SettlementJpaEntity>
  + findByOrganizationIdAndStatus(Long orgId, SettlementStatus status, Pageable p): Page<SettlementJpaEntity>
```

**Repository Adapter:** `SettlementRepositoryAdapter` → `pay_settlement_seq`

### Listeners — `infrastructure/messaging/listeners/`

#### `PaystackSettlementListener`
| Attribute | Value |
|---|---|
| **Source** | Paystack webhook (HTTP, not Kafka) |
| **Adapter** | `PaystackSettlementWebhookAdapter` in `infrastructure/services/` |
| **Event** | Paystack `transfer.success` settlement event |
| **Payload** | `settlementId`, `amount`, `settledAt`, `merchantCode` |
| **Command called** | `RecordSettlementCommand` + `ConfirmSettlementCommand` |
| **Idempotency** | `providerSettlementId` uniqueness check → `DuplicateSettlementException` |

#### `MoniepointSettlementListener`
| Attribute | Value |
|---|---|
| **Source** | Moniepoint webhook (HTTP) |
| **Adapter** | `MoniepointSettlementWebhookAdapter` |
| **Command called** | `RecordSettlementCommand` + `ConfirmSettlementCommand` |

---

## Presentation Layer

### Controller: `SettlementController` — `/api/v1/pay`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `GET` | `/settlements` | `pay:settlement:manage` | `?orgId&status&from&to&page&size` | `PageResult<SettlementResult>` |
| `GET` | `/settlements/{id}` | `pay:settlement:manage` | — | `SettlementResult` |
| `POST` | `/settlements/{id}/confirm` | `pay:settlement:manage` | — | `void` |
| `POST` | `/settlements/{id}/dispute` | `pay:settlement:manage` | `DisputeSettlementRequest` | `void` |

---

## RBAC Table

| Permission | Commands |
|---|---|
| `pay:settlement:manage` | `ConfirmSettlementHandler`, `DisputeSettlementHandler` |

---

## Distributed Architecture

### Locking
- `Settlement` — Optimistic (`@Version`) — low contention

### Outbox
- `SettlementConfirmedEvent` — triggers GL accounting entry; must not be lost

### Idempotency
- Provider webhook idempotency via `providerSettlementId` uniqueness constraint in DB
- `DuplicateSettlementException` returned on replay

---

## Complete File List

```
atlashub-pay/settlement/src/main/java/com/atlashub/pay/settlement/
├── domain/
│   ├── entities/
│   │   └── Settlement.java
│   ├── events/
│   │   ├── SettlementConfirmedEvent.java
│   │   └── SettlementDisputedEvent.java
│   ├── exceptions/
│   │   ├── SettlementNotFoundException.java
│   │   ├── InvalidSettlementStateException.java
│   │   └── DuplicateSettlementException.java
│   ├── repositories/
│   │   └── SettlementRepository.java
│   └── valueobject/
│       ├── PaymentProvider.java
│       └── SettlementStatus.java
├── application/
│   ├── commands/
│   │   ├── RecordSettlement/ [RecordSettlementCommand, RecordSettlementHandler]
│   │   ├── ConfirmSettlement/ [ConfirmSettlementCommand, ConfirmSettlementHandler]
│   │   └── DisputeSettlement/ [DisputeSettlementCommand, DisputeSettlementHandler]
│   └── queries/
│       ├── ListSettlements/ [ListSettlementsQuery, ListSettlementsHandler]
│       └── GetSettlementDetails/ [GetSettlementDetailsQuery, GetSettlementDetailsHandler, SettlementResult]
├── infrastructure/
│   ├── persistence/ [adapters, entities, mappers, repositories]
│   └── services/
│       ├── PaystackSettlementWebhookAdapter.java
│       └── MoniepointSettlementWebhookAdapter.java
└── presentation/
    ├── dto/ [DisputeSettlementRequest, SettlementResult]
    └── rest/
        └── SettlementController.java
```
