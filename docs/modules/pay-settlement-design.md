# Pay Settlement Design (`atlashub-pay:settlement`)

## Role & Purpose

The `settlement` module tracks Paystack settlement batches paid into an organization's Anchor operating account. Paystack's Settlement API is the source of the provider payout state. Anchor's verified inbound-credit event is the source of the bank-receipt state. A settlement is complete only after both facts match; Paystack webhooks and `transfer.success` are not settlement confirmation.

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
├── environment           : ApiEnvironment
├── provider              : PaymentProvider     ← PAYSTACK
├── providerSettlementId  : String              ← provider's settlement batch reference
├── grossAmount            : Money
├── netAmount              : Money
├── providerFeeAmount      : Money
├── providerSubaccountCode : String
├── anchorDepositAccountId : Long
├── transactionReferences  : List<String>       ← charge linkage/audit evidence
├── anchorTransferReference: String?       ← required before confirmation
├── settledAt             : ZonedDateTime
├── status                : SettlementStatus    ← PROVIDER_PENDING | AWAITING_ANCHOR_CREDIT
│                                               | CONFIRMED | RECONCILIATION_REQUIRED
│                                               | DISPUTED | FAILED
└── description           : String
```

**Business methods (on entity):**

| Method | Guard | Events | Exceptions |
|---|---|---|---|
| `confirm(anchorTransferReference)` | status == AWAITING_ANCHOR_CREDIT and the handler has verified destination/currency/net amount | `ProviderSettlementReceivedEvent` | `InvalidSettlementStateException` |
| `markProviderConfirmed()` | status == PROVIDER_PENDING | — | `InvalidSettlementStateException` |
| `requireReconciliation(reason)` | unmatched for three business days | — | `InvalidSettlementStateException` |
| `dispute(reason)` | status is not FAILED | `SettlementDisputedEvent` | `InvalidSettlementStateException` |
| `resolveDispute()` | status == DISPUTED | — | — |

Confirmation publishes `ProviderSettlementReceivedEvent`. `pay:ledger` consumes its own compatible copy and performs the idempotent `PROVIDER_SETTLED` posting keyed by provider settlement ID. Settlement never calls a ledger handler synchronously.

### Supporting aggregates

- `SettlementCreditEvidence` persists every verified Anchor inbound credit before matching. This allows both credit-before-settlement and settlement-before-credit delivery orders without losing evidence.
- `SettlementPollCursor` stores the last provider settlement ID per environment and Paystack subaccount. Polling can restart safely and still uses provider/local uniqueness as the final idempotency guard.

---

### Domain Events — `com.atlashub.pay.settlement.domain.events`

| Event | Published When | Consumed By | Topic |
|---|---|---|---|
| `ProviderSettlementReceivedEvent` | Paystack payout is matched to an Anchor inbound credit | `pay:ledger`, `pay:tx-query`, `accounting:gl`, `notifications` | `pay-events` |
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
    Long organizationId, ApiEnvironment environment, PaymentProvider provider,
    String providerSettlementId, String providerSubaccountCode,
    Long anchorDepositAccountId, Money grossAmount, Money netAmount,
    Money providerFeeAmount, ZonedDateTime providerSettledAt,
    List<String> transactionReferences
)
```
**Handler:** `RecordSettlementHandler` | **Response:** `Long settlementId`  
**Invocation source:** `PaystackSettlementPollingScheduler` through its command handler
**Flow:**
1. Check `(provider, environment, providerSettlementId)` uniqueness.
2. Create `Settlement` in `AWAITING_ANCHOR_CREDIT` when Paystack reports success.
3. `repository.save()`

---

#### `ConfirmSettlementCommand`
```java
record ConfirmSettlementCommand(
    Long settlementId, Long anchorDepositAccountId,
    String anchorTransferReference, Money receivedAmount,
    ZonedDateTime receivedAt
)
```
**Handler:** `ConfirmSettlementHandler` | **Response:** `void`  
**Invocation source:** `AnchorFundingSettlementListener` only
**Flow:**
1. Load the single unmatched candidate by Anchor deposit account, currency, net amount, and pending status.
2. Validate the destination, currency, amount, and environment.
3. Call `settlement.confirm(anchorTransferReference)`.
4. Save once; the outbox publishes `ProviderSettlementReceivedEvent` for ledger consumption.

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
| `SettlementJpa` | `pay_settlements` | `@Version` optimistic |
| `SettlementCreditEvidenceJpa` | `pay_settlement_credit_evidence` | unique Anchor credit reference |
| `SettlementPollCursorJpa` | `pay_settlement_poll_cursors` | unique environment/provider/subaccount route |

**Spring Data:**
```
SpringDataSettlementRepository
  + findByProviderSettlementId(String id): Optional<SettlementJpaEntity>
  + findByOrganizationIdAndStatus(Long orgId, SettlementStatus status, Pageable p): Page<SettlementJpaEntity>
```

**Repository Adapter:** `SettlementRepositoryAdapter` → `pay_settlement_seq`

### Listeners — `infrastructure/messaging/listeners/`

#### `PaystackSettlementPollingScheduler`
| Attribute | Value |
|---|---|
| **Source** | Paystack Settlement API, polled per active Paystack subaccount |
| **Adapter** | `atlashub-infrastructure:paystack` implements shared `SettlementProviderPort`; this module depends only on that provider-neutral port |
| **Provider fact** | successful settlement batch and its transaction references |
| **Command called** | `RecordSettlementCommand`; confirmation waits for Anchor evidence |
| **Idempotency** | `(provider, environment, providerSettlementId)` uniqueness |

#### `AnchorFundingSettlementListener`

Consumes `OrganizationAccountFundedEvent` from `pay:accounts`. It matches only the same Anchor operating account, currency, and net settlement amount. A missing, late, or mismatched credit leaves the batch pending for reconciliation; Paystack success alone never confirms a settlement.

## Presentation Layer

### Controller: `SettlementController` — `/api/v1/pay`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `GET` | `/settlements` | `pay:settlement:manage` | `?status&from&to&page&size` | `PageResult<SettlementResult>` |
| `GET` | `/settlements/{id}` | `pay:settlement:manage` | — | `SettlementResult` |
| `POST` | `/settlements/{id}/dispute` | `pay:settlement:manage` | `DisputeSettlementRequest` | `void` |
| `POST` | `/settlements/{id}/retry-reconciliation` | `pay:settlement:manage` | — | `void` |
| `POST` | `/settlements/{id}/resolve-dispute` | `pay:settlement:manage` | — | `void` |

---

## RBAC Table

| Permission | Commands |
|---|---|
| `pay:settlement:manage` | `DisputeSettlementHandler`, retry reconciliation, resolve dispute |

---

## Distributed Architecture

### Locking
- `Settlement` — Optimistic (`@Version`) — low contention

### Outbox
- `ProviderSettlementReceivedEvent` — triggers the idempotent ledger/GL entries and unified transaction projection; must not be lost

### Idempotency
- Provider discovery idempotency via `(provider, environment, providerSettlementId)` uniqueness.
- Anchor matching idempotency via unique `anchorTransferReference` once attached.
- Scheduler polling uses a persisted per-subaccount watermark and overlapping lookback; replay returns the existing settlement.
- `AWAITING_ANCHOR_CREDIT` records older than three business days transition to `RECONCILIATION_REQUIRED`; they are never auto-confirmed from Paystack alone.

---

## Complete File List

```
atlashub-pay/settlement/src/main/java/com/atlashub/pay/settlement/
├── domain/
│   ├── entities/
│   │   ├── Settlement.java
│   │   ├── SettlementCreditEvidence.java
│   │   └── SettlementPollCursor.java
│   ├── events/
│   │   ├── ProviderSettlementReceivedEvent.java
│   │   └── SettlementDisputedEvent.java
│   ├── exceptions/
│   │   ├── SettlementNotFoundException.java
│   │   ├── InvalidSettlementStateException.java
│   │   └── DuplicateSettlementException.java
│   ├── repositories/
│   │   ├── SettlementRepository.java
│   │   ├── SettlementCreditEvidenceRepository.java
│   │   └── SettlementPollCursorRepository.java
│   └── valueobject/
│       ├── PaymentProvider.java
│       └── SettlementStatus.java
├── application/
│   ├── commands/
│   │   ├── RecordSettlement/ [RecordSettlementCommand, RecordSettlementHandler]
│   │   ├── ConfirmSettlement/ [ConfirmSettlementCommand, ConfirmSettlementHandler]
│   │   ├── PollPaystackSettlements/ [PollPaystackSettlementsCommand, PollPaystackSettlementsHandler]
│   │   ├── EscalateUnmatchedSettlements/ [command, handler]
│   │   ├── RetrySettlementReconciliation/ [command, handler]
│   │   ├── ResolveSettlementDispute/ [command, handler]
│   │   └── DisputeSettlement/ [DisputeSettlementCommand, DisputeSettlementHandler]
│   └── queries/
│       ├── ListSettlements/ [ListSettlementsQuery, ListSettlementsHandler]
│       └── GetSettlementDetails/ [GetSettlementDetailsQuery, GetSettlementDetailsHandler, SettlementResult]
├── infrastructure/
│   └── persistence/ [settlement, credit-evidence and poll-cursor adapters/entities/mappers/repositories]
├── infrastructure/messaging/
│   ├── listeners/AnchorFundingSettlementListener.java
│   └── schedulers/ [PaystackSettlementPollingScheduler, SettlementExceptionScheduler]
└── presentation/
    ├── dto/ [DisputeSettlementRequest, SettlementResult]
    └── rest/
        └── SettlementController.java
```
