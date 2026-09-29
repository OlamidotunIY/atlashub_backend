# Accounting Cash Design (`atlashub-accounting` / `com.atlashub.accounting.cash`)

## Role & Purpose

The `cash` subpackage handles bank reconciliation (matching GL entries to bank statement lines) and cash evacuation tracking (moving physical cash from tills to bank accounts). It ensures the GL's cash accounts match the actual bank statement.

Gradle module: `atlashub-accounting`  
Package: `com.atlashub.accounting.cash`

---

## Domain Layer

### `BankReconciliation` (Aggregate Root)

**Package:** `com.atlashub.accounting.cash.domain.entities`

```
BankReconciliation
├── id                     : Long
├── organizationId         : Long
├── bankAccountCode        : String        ← the GL account code for this bank account
├── statementDate          : LocalDate
├── statementClosingBalance: Money
├── bookBalance            : Money         ← sum of GL entries for this bank account
├── difference             : Money         ← statementClosingBalance - bookBalance
├── matchedItems           : List<ReconciliationItem>
├── unmatchedBankItems     : List<BankStatementLine>
├── unmatchedBookItems     : List<Long>    ← JournalLine IDs
└── status                 : ReconciliationStatus ← DRAFT | RECONCILED
```

**Business methods:**
- `matchItem(bankLineId, journalLineId)` — links a bank statement line to a journal line; moves both from unmatched → matched lists
- `finalize()` → DRAFT → RECONCILED; guard: `difference.isZero()` or explicit force-reconcile; registers `BankReconciliationCompletedEvent`

### `BankStatementLine` (Entity)

```
BankStatementLine
├── id                : Long
├── reconciliationId  : Long
├── transactionDate   : LocalDate
├── description       : String
├── amount            : Money
└── type              : StatementEntryType ← CREDIT | DEBIT
```

### `ReconciliationItem` (Entity)

```
ReconciliationItem
├── id                : Long
├── reconciliationId  : Long
├── bankStatementLineId: Long
└── journalLineId     : Long
```

---

### `CashEvacuation` (Aggregate Root)

```
CashEvacuation
├── id             : Long
├── organizationId : Long
├── outletId       : Long
├── tillId         : Long
├── amount         : Money
├── evacuatedBy    : Long
├── evacuatedAt    : ZonedDateTime
└── status         : EvacuationStatus ← PENDING | CONFIRMED
```

**Business methods:**
- `confirm()` → PENDING → CONFIRMED; registers `CashEvacuatedEvent`

---

### Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `CashEvacuatedEvent` | Cash confirmed moved to bank | `pay` (trigger `ReconcileTillCommand`) |
| `BankReconciliationCompletedEvent` | Reconciliation finalized | `notifications`, `audit` |

---

### Domain Exceptions — `com.atlashub.accounting.cash.domain.exceptions`

```java
public class BankReconciliationNotFoundException extends NotFoundException {
    public BankReconciliationNotFoundException(Long id) { super("Bank reconciliation not found: " + id); }
}
public class InvalidReconciliationStateException extends BusinessRuleException {
    public InvalidReconciliationStateException(String message) { super(message); }
}
public class CashEvacuationNotFoundException extends NotFoundException {
    public CashEvacuationNotFoundException(Long id) { super("Cash evacuation not found: " + id); }
}
public class ReconciliationNotBalancedException extends BusinessRuleException {
    public ReconciliationNotBalancedException(Money difference) {
        super("Cannot finalize reconciliation: unresolved difference of " + difference);
    }
}
```

---

## Application Layer

### Commands — `com.atlashub.accounting.cash.application.commands`

#### `StartBankReconciliationCommand`
```java
record StartBankReconciliationCommand(
    Long organizationId, String bankAccountCode, LocalDate statementDate,
    Money statementClosingBalance, List<BankStatementLineDto> bankLines
)
```
**Handler:** `StartBankReconciliationHandler` | **Response:** `Long reconciliationId`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:accounts:create')")`  
**Flow:** Load GL book balance for `bankAccountCode` up to `statementDate` → compute `difference` → create `BankReconciliation` (DRAFT) with all bank lines as unmatched → save

---

#### `MatchReconciliationItemCommand`
```java
record MatchReconciliationItemCommand(Long reconciliationId, Long bankLineId, Long journalLineId)
```
**Handler:** `MatchReconciliationItemHandler` | **Response:** `void`  
**Flow:** Load `BankReconciliation` → `reconciliation.matchItem(bankLineId, journalLineId)` → save

---

#### `FinalizeReconciliationCommand`
```java
record FinalizeReconciliationCommand(Long reconciliationId, boolean forceFinalize)
```
**Handler:** `FinalizeReconciliationHandler` | **Response:** `void`  
**Flow:** Load `BankReconciliation` → if `difference != 0 AND NOT forceFinalize` → `ReconciliationNotBalancedException`; else `reconciliation.finalize()` → save

---

#### `RecordCashEvacuationCommand`
```java
record RecordCashEvacuationCommand(Long organizationId, Long outletId, Long tillId, Money amount, Long evacuatedBy)
```
**Handler:** `RecordCashEvacuationHandler` | **Response:** `Long evacuationId`  
**Invocation source:** Controller (finance manager records physical cash movement)

---

#### `ConfirmCashEvacuationCommand`
```java
record ConfirmCashEvacuationCommand(Long evacuationId)
```
**Handler:** `ConfirmCashEvacuationHandler` | **Response:** `void`  
**Flow:** Load `CashEvacuation` → `evacuation.confirm()` → save → `CashEvacuatedEvent` published → GL: Dr Bank Account, Cr Cash in Till

---

### Queries — `com.atlashub.accounting.cash.application.queries`

#### `GetBankReconciliationQuery`
```java
record GetBankReconciliationQuery(Long reconciliationId)
```
**Handler:** `GetBankReconciliationHandler` | **Result:** `BankReconciliationResult`

---

#### `ListBankReconciliationsQuery`
```java
record ListBankReconciliationsQuery(Long organizationId, ReconciliationStatus status)
```
**Handler:** `ListBankReconciliationsHandler` | **Result:** `List<BankReconciliationSummaryResult>` (bounded per org)

---

## Infrastructure Layer

### Persistence

| JPA Entity | Table | Locking |
|---|---|---|
| `BankReconciliationJpaEntity` | `accounting_bank_reconciliations` | `@Version` optimistic |
| `BankStatementLineJpaEntity` | `accounting_bank_statement_lines` | — |
| `ReconciliationItemJpaEntity` | `accounting_reconciliation_items` | — |
| `CashEvacuationJpaEntity` | `accounting_cash_evacuations` | — |

**Repository Adapters:**
- `BankReconciliationRepositoryAdapter` → `accounting_reconciliation_seq`
- `CashEvacuationRepositoryAdapter` → `accounting_cash_evacuation_seq`

### Listener

#### `TillClosedListener`
| Topic | `commerce-events` |
|---|---|
| **Group ID** | `accounting-till-closed` |
| **Action** | Calls `RecordCashEvacuationHandler` to log the closing cash balance |

---

## Presentation Layer

### Controller: `AccountingCashController` — `/api/v1/accounting/cash`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/reconciliations` | `accounting:accounts:create` | `StartBankReconciliationRequest` | `Long` |
| `POST` | `/reconciliations/{id}/match` | — | `MatchReconciliationItemRequest` | `void` |
| `POST` | `/reconciliations/{id}/finalize` | `accounting:accounts:create` | `FinalizeReconciliationRequest` | `void` |
| `GET` | `/reconciliations/{id}` | — | — | `BankReconciliationResult` |
| `GET` | `/reconciliations` | — | `?orgId&status` | `List<BankReconciliationSummaryResult>` |
| `POST` | `/evacuations` | — | `RecordCashEvacuationRequest` | `Long` |
| `POST` | `/evacuations/{id}/confirm` | — | — | `void` |

---

## Complete File List

```
atlashub-accounting/src/main/java/com/atlashub/accounting/cash/
├── domain/
│   ├── entities/ [BankReconciliation, BankStatementLine, ReconciliationItem, CashEvacuation]
│   ├── events/ [CashEvacuatedEvent, BankReconciliationCompletedEvent]
│   ├── exceptions/ [BankReconciliationNotFoundException, InvalidReconciliationStateException, CashEvacuationNotFoundException, ReconciliationNotBalancedException]
│   ├── repositories/ [BankReconciliationRepository, CashEvacuationRepository]
│   └── valueobject/ [ReconciliationStatus, EvacuationStatus, StatementEntryType]
├── application/
│   ├── commands/ [StartBankReconciliation, MatchReconciliationItem, FinalizeReconciliation, RecordCashEvacuation, ConfirmCashEvacuation]
│   └── queries/ [GetBankReconciliation, ListBankReconciliations]
├── infrastructure/
│   ├── messaging/listeners/ [TillClosedListener]
│   └── persistence/ [adapters, entities, mappers, repositories]
└── presentation/
    ├── dto/ [...]
    └── rest/
        └── AccountingCashController.java
```
