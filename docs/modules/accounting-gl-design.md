# Accounting GL Design (`atlashub-accounting` / `com.atlashub.accounting.gl`)

## Role & Purpose

The `gl` (General Ledger) subpackage is the financial backbone of the Accounting module. It maintains the Chart of Accounts, posts all journal entries (both automatic event-driven and manual), enforces double-entry balance constraints, and provides financial reporting (Trial Balance, P&L, Balance Sheet, Cash Flow).

This is a **passive, event-driven module.** The GL listens to events from Commerce, HR, Pay, and Logistics and posts the appropriate journal entries automatically. Finance managers may also post manual entries — large manual entries require Maker-Checker approval.

The GL also maintains `LedgerAccountMapping` — the bridge between `atlashub-pay:ledger`'s `SourceSystem` codes and the corresponding GL accounts to debit and credit.

Gradle module: `atlashub-accounting`  
Package: `com.atlashub.accounting.gl`

---

## Two Ledgers — One Platform

| | `atlashub-pay:ledger` | `atlashub-accounting:gl` |
|---|---|---|
| **Purpose** | Real-time cash tracking | Financial reporting |
| **Model** | Double-entry, balance snapshots | Journal entries, Chart of Accounts |
| **Granularity** | Every money movement as it happens | Business-event level |
| **Users** | Treasury / cash management | Finance / accounting team |
| **Drives** | Wallet balances, available funds | P&L, Balance Sheet, Cash Flow |

Bridged by `LedgerBridgeListener`: when `LedgerTransactionPostedEvent` arrives from Pay, it maps the source system to the appropriate GL accounts via `LedgerAccountMapping`.

---

## Domain Layer

### `Account` (Aggregate Root — Chart of Accounts)

**Package:** `com.atlashub.accounting.gl.domain.entities`

```
Account
├── id              : Long
├── organizationId  : Long
├── code            : String          ← e.g., "1001" — unique per org
├── name            : String          ← e.g., "Cash in Hand"
├── type            : AccountType     ← ASSET | LIABILITY | EQUITY | REVENUE | EXPENSE
├── parentAccountId : Long            ← nullable — for hierarchical COA
├── isSystemAccount : Boolean         ← true = auto-seeded, cannot be deleted
├── isActive        : Boolean
└── createdAt       : ZonedDateTime
```

**Domain Rule:** `Account` has **NO mutable balance field**. Balance is always derived dynamically from `JournalLine` entries + `BalanceSnapshot`. This eliminates lock contention on high-volume accounts (Revenue, Cash).

**Business methods:**
- `deactivate()` — guard: not referenced by open journal lines; registers no event (admin action)

---

### `BalanceSnapshot` (Entity)

```
BalanceSnapshot
├── id           : Long
├── accountId    : Long
├── snapshotDate : LocalDate
├── balance      : Money
└── createdAt    : ZonedDateTime
```

Periodically computed (nightly job). Balance queries use: `latest snapshot balance + sum of journal lines since snapshot date`.

---

### `JournalEntry` (Aggregate Root)

```
JournalEntry
├── id            : Long
├── organizationId: Long
├── entryNumber   : String              ← unique, e.g., "JE-2026-09-00042"
├── date          : LocalDate
├── reference     : String              ← links to originating business event
├── description   : String
├── lines         : List<JournalLine>
├── status        : JournalEntryStatus  ← DRAFT | PENDING_APPROVAL | POSTED | VOIDED
├── source        : EntrySource         ← AUTOMATIC | MANUAL
├── initiatedBy   : Long                ← userId; nullable for automatic entries
├── approvedBy    : Long                ← nullable
├── approvedAt    : ZonedDateTime       ← nullable
└── createdAt     : ZonedDateTime
```

**Business methods (on entity):**

| Method | Guard | Events | Exceptions |
|---|---|---|---|
| `addLine(accountId, amount, entryType)` | status == DRAFT | — | — |
| `submitForApproval(initiatorId)` | status == DRAFT | `JournalEntryPendingApprovalEvent` | `InvalidEntryStateException` |
| `approve(approverId)` | status == PENDING_APPROVAL **AND** approverId ≠ initiatedBy | `JournalEntryPostedEvent` | `SelfApprovalNotAllowedException`, `InvalidEntryStateException` |
| `post()` | ∑DEBIT == ∑CREDIT | `JournalEntryPostedEvent` | `JournalUnbalancedException`, `InvalidEntryStateException` |
| `voidEntry(voidedBy, reason)` | status == POSTED | — (caller must create reversing entry) | `InvalidEntryStateException` |

**Maker-Checker domain enforcement:** Same pattern as HR Payroll — `approve()` rejects self-approval at the domain level.

---

### `JournalLine` (Entity — immutable after posting)

```
JournalLine
├── id             : Long
├── journalEntryId : Long
├── accountId      : Long
├── amount         : Money
└── type           : EntryType   ← DEBIT | CREDIT
```

---

### `LedgerAccountMapping` (Entity)

Maps a Pay `SourceSystem` to the GL accounts to debit and credit. Used by `LedgerBridgeListener`.

```
LedgerAccountMapping
├── id             : Long
├── organizationId : Long
├── sourceSystem   : SourceSystem   ← COMMERCE_CHECKOUT | PAYROLL | LOAN_DISBURSEMENT | SETTLEMENT | PAYOUT | ...
├── debitAccountId : Long
├── creditAccountId: Long
└── description    : String
```

---

### Domain Events — `com.atlashub.accounting.gl.domain.events`

| Event | Published When | Consumed By |
|---|---|---|
| `JournalEntryPostedEvent` | Entry posted | `audit` (log), `analytics` (update financial metrics) |
| `JournalEntryPendingApprovalEvent` | Large manual entry submitted for approval | `notifications` (WS push + email to approvers) |

---

### Domain Exceptions — `com.atlashub.accounting.gl.domain.exceptions`

```java
public class AccountNotFoundException extends NotFoundException {
    public AccountNotFoundException(Long id) { super("Account not found: " + id); }
    public AccountNotFoundException(String code) { super("Account not found with code: " + code); }
}
public class DuplicateAccountCodeException extends ConflictException {
    public DuplicateAccountCodeException(String code) { super("Account code already exists: " + code); }
}
public class SystemAccountImmutableException extends BusinessRuleException {
    public SystemAccountImmutableException() { super("System accounts cannot be modified or deleted"); }
}
public class JournalEntryNotFoundException extends NotFoundException {
    public JournalEntryNotFoundException(Long id) { super("Journal entry not found: " + id); }
}
public class JournalUnbalancedException extends BusinessRuleException {
    public JournalUnbalancedException(Money totalDebits, Money totalCredits) {
        super("Journal entry is unbalanced: debits=" + totalDebits + " credits=" + totalCredits);
    }
}
public class InvalidEntryStateException extends BusinessRuleException {
    public InvalidEntryStateException(String message) { super(message); }
}
public class SelfApprovalNotAllowedException extends AuthorizationException {
    public SelfApprovalNotAllowedException() {
        super("The approver cannot be the same person who initiated the journal entry");
    }
}
public class EntryAmountExceedsApprovalThresholdException extends BusinessRuleException {
    public EntryAmountExceedsApprovalThresholdException(Money threshold) {
        super("Manual entry amount exceeds approval threshold of " + threshold + " — must be submitted for approval");
    }
}
public class LedgerMappingNotFoundException extends NotFoundException {
    public LedgerMappingNotFoundException(String sourceSystem) {
        super("No ledger account mapping found for source system: " + sourceSystem);
    }
}
```

---

### Domain Service: `LedgerMappingService`

**Package:** `com.atlashub.accounting.gl.domain.services`  
**Declared as:** `@Bean` in `ApplicationConfig` (not `@Component` — lives in domain layer)

**Responsibility:** Given a Pay `SourceSystem` code, look up the `LedgerAccountMapping` for the organization and return the debit and credit account IDs. Called by `LedgerBridgeListener` for automatic journal posting.

**Why a domain service:** Requires access to `LedgerAccountMappingRepository` to look up mappings. Too many external repository dependencies to be a static entity method. Centralizes the source-system → GL account resolution logic.

```java
public class LedgerMappingService {
    public LedgerAccountMapping resolveMapping(Long organizationId, String sourceSystem);
}
```

---

## Application Layer

### Commands — `com.atlashub.accounting.gl.application.commands`

#### `CreateAccountCommand`
```java
record CreateAccountCommand(Long organizationId, String code, String name,
                            AccountType type, Long parentAccountId)
```
**Handler:** `CreateAccountHandler` | **Response:** `CreateAccountResponse(Long accountId)`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:accounts:create')")`  
**Flow:** Check unique code per org → `DuplicateAccountCodeException`; create `Account` → `repository.save()`

---

#### `DeactivateAccountCommand`
```java
record DeactivateAccountCommand(Long accountId)
```
**Handler:** `DeactivateAccountHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:accounts:create')")`  
**Flow:** Load `Account` → guard isSystemAccount → `SystemAccountImmutableException`; `account.deactivate()` → save

---

#### `SeedDefaultChartOfAccountsCommand`
```java
record SeedDefaultChartOfAccountsCommand(Long organizationId)
```
**Handler:** `SeedDefaultChartOfAccountsHandler` | **Response:** `void`  
**Invocation source:** `OrganizationComplianceApprovedListener`  
**Flow:** Creates standard accounts (Cash in Hand, Accounts Receivable, Sales Revenue, PAYE Tax Payable, etc.) for the new org

---

#### `RecordJournalEntryCommand`
```java
record RecordJournalEntryCommand(Long organizationId, LocalDate date, String reference,
                                 String description, List<JournalLineDto> lines,
                                 Long initiatedBy, EntrySource source)
```
**Handler:** `RecordJournalEntryHandler` | **Response:** `RecordJournalEntryResponse(Long entryId, JournalEntryStatus status)`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:journal:create')")` (for manual entries; no annotation for automatic listener-invoked calls)  
**Flow:**
1. Create `JournalEntry` (DRAFT), add all lines
2. Calculate total: `if totalAmount > approvalThreshold AND source == MANUAL` → `entry.submitForApproval(initiatedBy)` → status = PENDING_APPROVAL → `JournalEntryPendingApprovalEvent`
3. Else: `entry.post()` → validates ∑DEBIT == ∑CREDIT → status = POSTED → `JournalEntryPostedEvent`
4. `repository.save(entry)`

---

#### `ApproveJournalEntryCommand`
```java
record ApproveJournalEntryCommand(Long entryId, Long approverId)
```
**Handler:** `ApproveJournalEntryHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:journal:approve')")`  
**Maker-Checker:** `entry.approve(approverId)` validates `approverId ≠ initiatedBy`

---

#### `VoidJournalEntryCommand`
```java
record VoidJournalEntryCommand(Long entryId, Long voidedBy, String reason)
```
**Handler:** `VoidJournalEntryHandler` | **Response:** `void`  
**Flow:** Load `JournalEntry` → `entry.voidEntry(voidedBy, reason)` → create equal-and-opposite reversing `JournalEntry` and post it → save both

---

#### `SetLedgerMappingCommand`
```java
record SetLedgerMappingCommand(Long organizationId, String sourceSystem,
                               Long debitAccountId, Long creditAccountId, String description)
```
**Handler:** `SetLedgerMappingHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:accounts:create')")`  
**Flow:** Upsert `LedgerAccountMapping` for the given sourceSystem + org

---

### Queries — `com.atlashub.accounting.gl.application.queries`

#### `GetChartOfAccountsQuery`
```java
record GetChartOfAccountsQuery(Long organizationId)
```
**Handler:** `GetChartOfAccountsHandler` | **Result:** `List<AccountResult>` (bounded — few hundred accounts per org)

---

#### `GetAccountBalanceQuery`
```java
record GetAccountBalanceQuery(Long organizationId, Long accountId, LocalDate asOf)
```
**Handler:** `GetAccountBalanceHandler` | **Result:** `Money`  
**Flow:** Load latest `BalanceSnapshot` before `asOf` → sum all `JournalLine` entries since snapshot date → return computed balance

---

#### `GetLedgerQuery`
```java
record GetLedgerQuery(Long organizationId, Long accountId, LocalDate dateFrom, LocalDate dateTo)
```
**Handler:** `GetLedgerHandler` | **Result:** `List<JournalLineResult>` (bounded date range)

---

#### `GetTrialBalanceQuery`
```java
record GetTrialBalanceQuery(Long organizationId, LocalDate date)
```
**Handler:** `GetTrialBalanceHandler` | **Result:** `TrialBalanceReport`

---

#### `GetIncomeStatementQuery`
```java
record GetIncomeStatementQuery(Long organizationId, LocalDate dateFrom, LocalDate dateTo)
```
**Handler:** `GetIncomeStatementHandler` | **Result:** `IncomeStatementReport`

---

#### `GetBalanceSheetQuery`
```java
record GetBalanceSheetQuery(Long organizationId, LocalDate date)
```
**Handler:** `GetBalanceSheetHandler` | **Result:** `BalanceSheetReport`

---

#### `ListJournalEntriesQuery`
```java
record ListJournalEntriesQuery(Long organizationId, JournalEntryStatus status,
                               LocalDate dateFrom, LocalDate dateTo, int page, int size)
```
**Handler:** `ListJournalEntriesHandler` | **Result:** `PageResult<JournalEntryResult>`  
**Justification:** Journal entries accumulate over time — pagination required.

---

## Infrastructure Layer

### Persistence

| JPA Entity | Table | Locking |
|---|---|---|
| `AccountJpaEntity` | `accounting_accounts` | — (no balance stored; derived dynamically) |
| `BalanceSnapshotJpaEntity` | `accounting_balance_snapshots` | `@Lock(PESSIMISTIC_WRITE)` during snapshot generation |
| `JournalEntryJpaEntity` | `accounting_journal_entries` | `@Version` optimistic |
| `JournalLineJpaEntity` | `accounting_journal_lines` | — (immutable after posting) |
| `LedgerAccountMappingJpaEntity` | `accounting_ledger_mappings` | — |

**Spring Data key methods:**
```
AccountJpaRepository
  + findByOrganizationIdAndCode(Long orgId, String code): Optional<AccountJpaEntity>
  + findByOrganizationId(Long orgId): List<AccountJpaEntity>

JournalEntryJpaRepository
  + findByOrganizationIdAndStatus(Long orgId, JournalEntryStatus status, Pageable p): Page<JournalEntryJpaEntity>
  + findByOrganizationIdAndDateBetween(Long orgId, LocalDate from, LocalDate to, Pageable p): Page<JournalEntryJpaEntity>

BalanceSnapshotJpaRepository
  + findTopByAccountIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(Long accountId, LocalDate asOf): Optional<BalanceSnapshotJpaEntity>

LedgerAccountMappingJpaRepository
  + findByOrganizationIdAndSourceSystem(Long orgId, String sourceSystem): Optional<LedgerAccountMappingJpaEntity>
```

**Repository Adapters + Sequences:**
- `AccountRepositoryAdapter` → `accounting_account_seq`
- `JournalEntryRepositoryAdapter` → `accounting_journal_entry_seq`
- `LedgerAccountMappingRepositoryAdapter` → `accounting_ledger_mapping_seq`

### Listeners — `infrastructure/messaging/listeners/`

#### `PosSaleCompletedListener`
| Topic | `commerce-events` |
|---|---|
| **Group ID** | `accounting-pos-sale-completed` |
| **Payload** | `salesOrderId`, `organizationId`, `totalNet`, `paymentMethod`, `items` |
| **Action** | Calls `RecordJournalEntryHandler`: Dr Cash/AR, Cr Sales Revenue |
| **Idempotency** | `(eventId, "accounting-pos-sale-completed")` — **CRITICAL**: duplicate must not post duplicate entries |

#### `PosSaleRefundedListener`
| Topic | `commerce-events` |
|---|---|
| **Group ID** | `accounting-pos-refunded` |
| **Action** | Posts reversing entry for the original sale |

#### `PurchaseOrderReceivedListener`
| Topic | `commerce-events` |
|---|---|
| **Group ID** | `accounting-po-received` |
| **Action** | Posts: Dr Inventory Asset, Cr Accounts Payable |

#### `StockTransferReceivedListener`
| Topic | `logistics-events` |
|---|---|
| **Group ID** | `accounting-stock-transfer-received` |
| **Action** | Posts: Dr Inventory (Dest), Cr Inventory (Source) |

#### `PayrollApprovedListener`
| Topic | `hr-events` |
|---|---|
| **Group ID** | `accounting-payroll-approved` |
| **Action** | Posts: Dr Salary Expense, Cr Payroll Payable; also posts PAYE deduction entry |

#### `PayrollDisbursedListener`
| Topic | `hr-events` |
|---|---|
| **Group ID** | `accounting-payroll-disbursed` |
| **Action** | Posts: Dr Payroll Payable, Cr Payroll Reserve Account |

#### `LoanApprovedListener`
| Topic | `hr-events` |
|---|---|
| **Group ID** | `accounting-loan-approved` |
| **Action** | Posts: Dr Loan Receivable, Cr Bank Account |

#### `SettlementConfirmedListener`
| Topic | `pay-events` |
|---|---|
| **Group ID** | `accounting-settlement-confirmed` |
| **Action** | Posts: Dr Bank Account, Cr Clearing Account |

#### `LedgerBridgeListener`
| Topic | `pay-events` |
|---|---|
| **Group ID** | `accounting-ledger-bridge` |
| **Event** | `LedgerTransactionPostedEvent` |
| **Action** | Calls `LedgerMappingService.resolveMapping(orgId, sourceSystem)` → posts corresponding GL journal entry |
| **Idempotency** | **CRITICAL** — this is the primary bridge; duplicates would corrupt books permanently |

#### `OrganizationComplianceApprovedListener`
| Topic | `compliance-events` |
|---|---|
| **Group ID** | `accounting-org-approved` |
| **Action** | Calls `SeedDefaultChartOfAccountsHandler` to create standard GL accounts for the new org |

### Infrastructure Services
- `AccountingDomainConfig` — declares `LedgerMappingService` as `@Bean`

---

## Presentation Layer

### Controller: `AccountingGlController` — `/api/v1/accounting`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/accounts` | `accounting:accounts:create` | `CreateAccountRequest` | `CreateAccountResponse` |
| `GET` | `/accounts` | — | `?orgId` | `List<AccountResult>` |
| `GET` | `/accounts/balance` | — | `?orgId&accountId&asOf` | `Money` |
| `GET` | `/accounts/ledger` | — | `?orgId&accountId&from&to` | `List<JournalLineResult>` |
| `POST` | `/journal` | `accounting:journal:create` | `RecordJournalEntryRequest` | `RecordJournalEntryResponse` |
| `POST` | `/journal/{id}/approve` | `accounting:journal:approve` | — | `void` |
| `POST` | `/journal/{id}/void` | `accounting:journal:approve` | `VoidJournalEntryRequest` | `void` |
| `GET` | `/journal` | — | `?orgId&status&from&to&page&size` | `PageResult<JournalEntryResult>` |
| `GET` | `/reports/trial-balance` | — | `?orgId&date` | `TrialBalanceReport` |
| `GET` | `/reports/income-statement` | — | `?orgId&from&to` | `IncomeStatementReport` |
| `GET` | `/reports/balance-sheet` | — | `?orgId&date` | `BalanceSheetReport` |
| `POST` | `/ledger-mappings` | `accounting:accounts:create` | `SetLedgerMappingRequest` | `void` |

---

## RBAC Table

| Permission | Commands |
|---|---|
| `accounting:accounts:create` | `CreateAccountHandler`, `DeactivateAccountHandler`, `SetLedgerMappingHandler` |
| `accounting:journal:create` | `RecordJournalEntryHandler` (manual only) |
| `accounting:journal:approve` | `ApproveJournalEntryHandler`, `VoidJournalEntryHandler` |

---

## Maker-Checker

| Operation | Maker Permission | Checker Permission | Domain Enforcement |
|---|---|---|---|
| Manual Journal Entry > ₦500k | `accounting:journal:create` | `accounting:journal:approve` | `JournalEntry.approve()` throws `SelfApprovalNotAllowedException` if `approverId.equals(initiatedBy)` |

---

## Socket Events

| Event | WS Channel | Payload |
|---|---|---|
| `JournalEntryPendingApprovalEvent` | `/user/{approverId}/queue/notifications` per approver | `type: "APPROVAL_REQUIRED"`, `message: "A manual journal entry requires your approval"`, `data: { entryId, amount }` |

---

## Standard Journal Entry Templates

| Business Event | Dr | Cr |
|---|---|---|
| POS Cash Sale | Cash in Till (Asset) | Sales Revenue (Revenue) |
| POS Card/Transfer Sale | Clearing / AR (Asset) | Sales Revenue (Revenue) |
| Supplier Invoice Received | Inventory Asset (Asset) | Accounts Payable (Liability) |
| Payroll Approved | Salary Expense (Expense) | Payroll Payable (Liability) |
| Payroll Disbursed | Payroll Payable (Liability) | Payroll Reserve Account (Asset) |
| Loan Disbursed | Loan Receivable (Asset) | Bank Account (Asset) |
| Inter-Outlet Transfer | Inventory – Dest (Asset) | Inventory – Source (Asset) |
| Cash Evacuation | Bank Account (Asset) | Cash in Till (Asset) |
| Settlement Confirmed | Bank Account (Asset) | Clearing Account (Asset) |

---

## Complete File List

```
atlashub-accounting/src/main/java/com/atlashub/accounting/gl/
├── domain/
│   ├── entities/ [Account, BalanceSnapshot, JournalEntry, JournalLine, LedgerAccountMapping]
│   ├── events/ [JournalEntryPostedEvent, JournalEntryPendingApprovalEvent]
│   ├── exceptions/ [AccountNotFoundException, DuplicateAccountCodeException, SystemAccountImmutableException, JournalEntryNotFoundException, JournalUnbalancedException, InvalidEntryStateException, SelfApprovalNotAllowedException, EntryAmountExceedsApprovalThresholdException, LedgerMappingNotFoundException]
│   ├── repositories/ [AccountRepository, JournalEntryRepository, BalanceSnapshotRepository, LedgerAccountMappingRepository]
│   ├── services/
│   │   └── LedgerMappingService.java
│   └── valueobject/ [AccountType, JournalEntryStatus, EntrySource, EntryType]
├── application/
│   ├── commands/ [CreateAccount, DeactivateAccount, SeedDefaultChartOfAccounts, RecordJournalEntry, ApproveJournalEntry, VoidJournalEntry, SetLedgerMapping]
│   └── queries/ [GetChartOfAccounts, GetAccountBalance, GetLedger, GetTrialBalance, GetIncomeStatement, GetBalanceSheet, ListJournalEntries]
├── infrastructure/
│   ├── messaging/
│   │   ├── events/ [PosSaleCompletedPayload, PurchaseOrderReceivedPayload, PayrollApprovedPayload, LedgerTransactionPostedPayload, ...]
│   │   └── listeners/ [PosSaleCompletedListener, PosSaleRefundedListener, PurchaseOrderReceivedListener, StockTransferReceivedListener, PayrollApprovedListener, PayrollDisbursedListener, LoanApprovedListener, SettlementConfirmedListener, LedgerBridgeListener, OrganizationComplianceApprovedListener]
│   ├── persistence/ [adapters, entities, mappers, repositories]
└── presentation/
    ├── dto/ [CreateAccountRequest, CreateAccountResponse, RecordJournalEntryRequest, RecordJournalEntryResponse, VoidJournalEntryRequest, SetLedgerMappingRequest, AccountResult, JournalEntryResult, JournalLineResult, TrialBalanceReport, IncomeStatementReport, BalanceSheetReport]
    └── rest/
        └── AccountingGlController.java
```
