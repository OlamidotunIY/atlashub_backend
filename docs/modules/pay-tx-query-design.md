# Pay Transaction Query Design (`atlashub-pay:tx-query`)

## Role & Purpose

The `tx-query` subpackage is a **dedicated read model** for querying the full transaction history across all Pay submodules (`charges`, `transfers`, `ledger`, `mandates`, `settlement`). It is populated exclusively by consuming Kafka events — no commands originate from user actions. All writes are listener-triggered.

This module exists to avoid cross-submodule queries and to provide a unified, performant transaction history view for dashboards, exports, and fee tier calculations.

Gradle module: `atlashub-pay:tx-query`  
Package: `com.atlashub.pay.txquery`

---

## Domain Layer

### `TransactionRecord` (Aggregate Root — Read Model)

**Package:** `com.atlashub.pay.txquery.domain.entities`

```
TransactionRecord
├── id                   : Long
├── organizationId       : Long
├── type                 : TransactionType       ← CHARGE | PAYOUT | SETTLEMENT | LEDGER_POST
├── status               : TransactionStatus     ← PENDING | SUCCESSFUL | FAILED | REFUNDED
├── amount               : Money
├── channel              : String                ← CARD | BANK_TRANSFER | USSD | POS_TERMINAL | INTERNAL
├── provider             : String                ← PAYSTACK | MONIEPOINT | ANCHOR | INTERNAL
├── reference            : String                ← source module's reference
├── sourceSystem         : String                ← SourceSystem enum value as string
├── sourceReferenceId    : String
├── recipientName        : String                ← nullable; for payouts
├── recipientAccountNumber: String               ← nullable; for payouts
├── customerId           : String                ← nullable; for B2B2C charges
├── metadata             : Map<String, String>
├── createdAt            : ZonedDateTime
└── completedAt          : ZonedDateTime         ← nullable
```

**No business methods.** This is a pure read-model aggregate — it is only ever created or updated by event listeners. No domain events are published from this aggregate.

---

### Domain Exceptions — `com.atlashub.pay.txquery.domain.exceptions`

```java
public class TransactionRecordNotFoundException extends NotFoundException {
    public TransactionRecordNotFoundException(Long id) { super("Transaction record not found: " + id); }
    public TransactionRecordNotFoundException(String reference) {
        super("Transaction not found for reference: " + reference);
    }
}
```

---

## Application Layer

### Commands — `com.atlashub.pay.txquery.application.commands`

All commands are listener-triggered (void response). No HTTP-exposed commands.

#### `RecordChargeTransactionCommand`
```java
record RecordChargeTransactionCommand(
    Long organizationId, String reference, Money amount,
    String channel, String provider, TransactionStatus status,
    String sourceSystem, String sourceReferenceId, String customerId,
    ZonedDateTime createdAt, ZonedDateTime completedAt
)
```
**Handler:** `RecordChargeTransactionHandler` | **Response:** `void`  
**Invocation source:** `ChargeSuccessfulListener`, `ChargeFailedListener`

---

#### `UpdateChargeStatusCommand`
```java
record UpdateChargeStatusCommand(String reference, TransactionStatus newStatus)
```
**Handler:** `UpdateChargeStatusHandler` | **Response:** `void`  
**Invocation source:** `ChargeRefundedListener`

---

#### `RecordPayoutTransactionCommand`
```java
record RecordPayoutTransactionCommand(
    Long organizationId, String reference, Money amount,
    String recipientName, String recipientAccountNumber,
    TransactionStatus status, String sourceSystem, String sourceReferenceId,
    ZonedDateTime createdAt, ZonedDateTime completedAt
)
```
**Handler:** `RecordPayoutTransactionHandler` | **Response:** `void`  
**Invocation source:** `PayoutCompletedListener`, `PayoutFailedListener`

---

#### `RecordSettlementTransactionCommand`
```java
record RecordSettlementTransactionCommand(
    Long organizationId, String providerSettlementId, Money amount,
    String provider, ZonedDateTime settledAt
)
```
**Handler:** `RecordSettlementTransactionHandler` | **Response:** `void`  
**Invocation source:** `SettlementConfirmedListener`

---

### Queries — `com.atlashub.pay.txquery.application.queries`

#### `ListTransactionsQuery`
```java
record ListTransactionsQuery(
    Long organizationId, TransactionType type, TransactionStatus status,
    ZonedDateTime dateFrom, ZonedDateTime dateTo, String channel,
    int page, int size
)
```
**Handler:** `ListTransactionsHandler` | **Result:** `PageResult<TransactionResult>`  
**Justification:** Transactions accumulate indefinitely — pagination required.

---

#### `GetTransactionDetailsQuery`
```java
record GetTransactionDetailsQuery(Long transactionId)
```
**Handler:** `GetTransactionDetailsHandler` | **Result:** `TransactionDetailsResult`

`TransactionDetailsResult`: All `TransactionRecord` fields plus computed display fields.

---

#### `GetChargeByReferenceQuery`
```java
record GetChargeByReferenceQuery(String reference)
```
**Handler:** `GetChargeByReferenceHandler` | **Result:** `TransactionResult` (single)

---

#### `ListPayoutsQuery`
```java
record ListPayoutsQuery(Long organizationId, TransactionStatus status,
                        String sourceSystem, int page, int size)
```
**Handler:** `ListPayoutsHandler` | **Result:** `PageResult<PayoutTransactionResult>`

---

#### `GetTransactionVolumeQuery`
```java
record GetTransactionVolumeQuery(Long organizationId, YearMonth month)
```
**Handler:** `GetTransactionVolumeHandler` | **Result:** `TransactionVolumeResult`

`TransactionVolumeResult`: `organizationId`, `month`, `totalChargeCount`, `totalChargeVolume: Money`, `totalPayoutCount`, `totalPayoutVolume: Money`

**Used by:** Billing module to calculate fee tier for the monthly invoice.

---

## Infrastructure Layer

### Persistence

| JPA Entity | Table | Locking |
|---|---|---|
| `TransactionRecordJpaEntity` | `pay_transaction_records` | — (append-only; updated by status changes only) |

**Spring Data:**
```
SpringDataTransactionRecordRepository
  + findByOrganizationIdAndTypeAndStatus(Long orgId, TransactionType type, TransactionStatus status, Pageable p): Page<TransactionRecordJpaEntity>
  + findByReference(String reference): Optional<TransactionRecordJpaEntity>
  + sumChargeVolumeByOrganizationIdAndMonth(Long orgId, YearMonth month): Money
```

**Repository Adapter:** `TransactionRecordRepositoryAdapter` → `pay_tx_record_seq`

### Listeners — `infrastructure/messaging/listeners/`

All listeners consume from `pay-events` topic.

| Listener | Group ID | Event Consumed | Command Called |
|---|---|---|---|
| `ChargeSuccessfulListener` | `pay-txquery-charge-successful` | `ChargeSuccessfulEvent` | `RecordChargeTransactionHandler` (status=SUCCESSFUL) |
| `ChargeFailedListener` | `pay-txquery-charge-failed` | `ChargeFailedEvent` | `RecordChargeTransactionHandler` (status=FAILED) |
| `ChargeRefundedListener` | `pay-txquery-charge-refunded` | `ChargeRefundInitiatedEvent` | `UpdateChargeStatusHandler` (status=REFUNDED) |
| `PayoutCompletedListener` | `pay-txquery-payout-completed` | `PayoutCompletedEvent` | `RecordPayoutTransactionHandler` (status=SUCCESSFUL) |
| `PayoutFailedListener` | `pay-txquery-payout-failed` | `PayoutFailedEvent` | `RecordPayoutTransactionHandler` (status=FAILED) |
| `SettlementConfirmedListener` | `pay-txquery-settlement` | `SettlementConfirmedEvent` | `RecordSettlementTransactionHandler` |

**Idempotency:** All listeners keyed on `(eventId, groupId)`. Duplicate event delivery must not create duplicate transaction records.

---

## Presentation Layer

### Controller: `TxQueryController` — `/api/v1/pay`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `GET` | `/transactions` | — | `?orgId&type&status&from&to&channel&page&size` | `PageResult<TransactionResult>` |
| `GET` | `/transactions/{id}` | — | — | `TransactionDetailsResult` |
| `GET` | `/transactions/by-reference` | — | `?reference` | `TransactionResult` |
| `GET` | `/payouts` | — | `?orgId&status&sourceSystem&page&size` | `PageResult<PayoutTransactionResult>` |
| `GET` | `/transactions/volume` | — | `?orgId&month` | `TransactionVolumeResult` |

---

## Complete File List

```
atlashub-pay/tx-query/src/main/java/com/atlashub/pay/txquery/
├── domain/
│   ├── entities/
│   │   └── TransactionRecord.java
│   ├── exceptions/
│   │   └── TransactionRecordNotFoundException.java
│   ├── repositories/
│   │   └── TransactionRecordRepository.java
│   └── valueobject/
│       ├── TransactionType.java
│       └── TransactionStatus.java
├── application/
│   ├── commands/
│   │   ├── RecordChargeTransaction/ ...
│   │   ├── UpdateChargeStatus/ ...
│   │   ├── RecordPayoutTransaction/ ...
│   │   └── RecordSettlementTransaction/ ...
│   └── queries/
│       ├── ListTransactions/ ...
│       ├── GetTransactionDetails/ ...
│       ├── GetChargeByReference/ ...
│       ├── ListPayouts/ ...
│       └── GetTransactionVolume/ ...
├── infrastructure/
│   ├── messaging/
│   │   ├── events/ [ChargeSuccessfulPayload, ChargeFailedPayload, PayoutCompletedPayload, PayoutFailedPayload, SettlementConfirmedPayload]
│   │   └── listeners/ [ChargeSuccessfulListener, ChargeFailedListener, ChargeRefundedListener, PayoutCompletedListener, PayoutFailedListener, SettlementConfirmedListener]
│   └── persistence/ [adapters, entities, mappers, repositories]
└── presentation/
    ├── dto/ [TransactionResult, TransactionDetailsResult, PayoutTransactionResult, TransactionVolumeResult]
    └── rest/
        └── TxQueryController.java
```
