# Accounting & ERP Module Design (`atlashub-accounting`)

## Role & Purpose

The Accounting module is the **financial record and reporting engine** of the AtlasHub platform. While `atlashub-pay` manages real-time money movement, the Accounting module maintains the **authoritative financial records** of everything that happened, expressed in terms of debits, credits, assets, liabilities, expenses, and equity.

Every significant operational event in the platform — a POS sale, a payroll disbursement, a supplier payment, a cash deposit, a stock delivery — produces a corresponding journal entry in Accounting. This ensures that the organization has an always up-to-date, legally defensible set of books.

This module answers questions like: *What is our net profit this month? How much tax have we collected vs remitted? What is the value of our inventory across all outlets? Are we within budget for Q3?*

It is an **event-driven passive module** — it never initiates operations. Instead, it listens to events from Commerce, HR, Pay, and Logistics, and automatically posts the appropriate journal entries. Finance managers can also post manual entries for adjustments, or reconcile the books against their bank statements.

---

## 1. How It Fits Into the Organization

### The Relationship with Pay
There are **two ledgers** in the AtlasHub system:

| Ledger | Location | Purpose |
|---|---|---|
| **Operational Ledger** | `atlashub-pay:ledger` | Real-time cash tracking. Double-entry. Records every actual money movement as it happens. Drives wallet balances. |
| **Accounting Ledger** | `atlashub-accounting:gl` | Financial reporting ledger. Chart of Accounts. Records business transactions as economic events. Drives P&L, Balance Sheet, Cash Flow statements. |

These two ledgers are separate by design. The operational ledger in Pay cares about *"did the money move?"*. The accounting ledger cares about *"what does this money movement mean for the business?"*

They are bridged by the **`LedgerBridgeListener`**: when `LedgerTransactionPostedEvent` is published by Pay, Accounting maps the Pay ledger entry to the appropriate accounting journal entry using a configurable `LedgerAccountMapping` table per organization.

Example: A Paystack card payment processed in Pay (SourceSystem: `CARD_CHARGE`) maps to:
```
DEBIT  Cash / Bank Account (Asset)           ₦25,000
CREDIT Sales Revenue (Revenue)               ₦25,000
```

### Automatic vs Manual Entries
Most journal entries are posted **automatically** by event listeners:
- `PosSaleCompletedListener` → posts sales revenue entry
- `PayrollApprovedListener` → posts salary expense and payroll payable
- `StockReceivedListener` → posts inventory asset and accounts payable
- `SettlementConfirmedListener` → posts bank settlement entry

Finance managers can also post **manual entries** for adjustments, accruals, or corrections via `RecordJournalEntryUseCase`.

---

## 2. Features

### Chart of Accounts
Organizations define their own Chart of Accounts (COA) — the master list of all account codes used for bookkeeping. Account types: ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE. Standard accounts are pre-seeded on org onboarding (e.g., "Cash in Hand", "Accounts Receivable", "Sales Revenue", "PAYE Tax Payable").

### General Ledger & Journal Entries
All financial transactions flow through `JournalEntry` aggregates. Each `JournalEntry` has a set of balanced `JournalLine`s (Debits = Credits). Once posted, entries cannot be edited — only voided and re-posted. Balance queries use the latest `BalanceSnapshot` + delta journal lines to avoid scanning the full history.

### Accounts Payable & Receivable
- **`Expense` (Accounts Payable)**: Records business expenses with approval workflow. On approval, Accounts Payable is credited. On payment, cash/bank is debited and AP is cleared.
- **Accounts Receivable**: Tracked via journal lines on `SalesOrder` completion. Customer credit sales are posted as Debit AR, Credit Revenue. Cash collection clears AR.

### Asset Management & Depreciation
Organizations can register fixed assets (computers, vehicles, machinery). `RunDepreciationUseCase` is a scheduled monthly task that calculates straight-line depreciation for each active asset and automatically posts:
```
DEBIT  Depreciation Expense      ₦X
CREDIT Accumulated Depreciation  ₦X
```

### Bank Reconciliation
Finance managers can upload bank statements and reconcile them against the accounting ledger (`BankReconciliation`). The reconciliation identifies: matched entries, unmatched ledger entries (outstanding deposits), and unmatched bank entries (outstanding payments).

### Cash Evacuation
When an outlet's till cash is physically moved to the bank, `EvacuateCashUseCase` records the movement:
```
DEBIT  Bank Account (Asset)      ₦X
CREDIT Cash in Outlet Till (Asset) ₦X
```
This also triggers `ReconcileTillCommand` in `pay` to update the operational ledger.

### Budget Management
Organizations define `Budget`s per account per period (e.g., ₦2M for Marketing in Q3 2026). `GetBudgetVarianceQuery` compares budgeted vs actual spend by reading journal line totals for that account and period — no separate tracking table needed.

### Financial Reports
All reports are computed on-demand from journal lines + balance snapshots:
- **Trial Balance**: All accounts with their debit/credit totals
- **Income Statement (P&L)**: Revenue - Expenses for a period
- **Balance Sheet**: Assets = Liabilities + Equity at a date
- **Cash Flow Statement**: Operating, investing, and financing cash flows

---

## 3. Domain Entities & Aggregates

### `gl` (General Ledger) Submodule

**`Account` (Aggregate Root — Chart of Accounts)**
- **Fields**: `id`, `organizationId`, `code` (e.g., "1001"), `name` (e.g., "Cash in Hand"), `type`: `AccountType` (ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE), `isActive`: Boolean
- **Note**: `Account` has NO mutable `balance` field. Balance is always derived from `JournalLine` entries + `BalanceSnapshot`. This deliberately avoids lock contention on high-volume accounts.
- **Methods**: `deactivate()`

**`BalanceSnapshot` (Entity)**
Periodically computed balance for fast queries.
- **Fields**: `id`, `accountId`, `snapshotDate`: LocalDate, `balance`: `Money`, `createdAt`: ZonedDateTime

**`JournalEntry` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `date`: LocalDate, `reference`: String, `description`: String, `lines`: `List<JournalLine>`, `status` (DRAFT, POSTED, VOIDED)
- **Methods**: `addLine(Long accountId, Money amount, EntryType type)`, `post()` (validates Debits == Credits), `voidEntry()`

**`JournalLine` (Entity)**
- **Fields**: `id`, `journalEntryId`, `accountId`, `amount`: `Money`, `type`: `EntryType` (DEBIT, CREDIT)

### `ap` & `ar` Submodule

**`Expense` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `categoryId`, `amount`: `Money`, `vendorName`, `date`, `description`, `status` (PENDING, APPROVED, PAID)
- **Methods**: `approve()`, `markPaid()`

### `assets` Submodule

**`Asset` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `name`, `tagNumber`, `purchaseDate`, `purchaseValue`: `Money`, `salvageValue`: `Money`, `usefulLifeMonths`, `accumulatedDepreciation`: `Money`, `currentValue`: `Money`, `status` (ACTIVE, DISPOSED)
- **Methods**: `depreciate(Money amount)`, `dispose(Money saleValue)`

### `cash` Submodule

**`BankReconciliation` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `bankAccountId`, `statementDate`, `statementBalance`: `Money`, `bookBalance`: `Money`, `difference`: `Money`, `status` (DRAFT, RECONCILED)

**`CashEvacuation` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `outletId`, `amount`: `Money`, `date`, `evacuatedBy`, `status` (PENDING, CONFIRMED)

### `budget` Submodule

**`Budget` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `accountId`, `name`, `period` (e.g., "2026-Q3"), `budgetedAmount`: `Money`, `status` (DRAFT, APPROVED, CLOSED)
- **Methods**: `approve()`, `close()`

**`BudgetVariance` (Read-only Projection)**
Computed on-demand by comparing `Budget.budgetedAmount` against `JournalLine` totals.
- **Fields**: `budgetId`, `accountId`, `period`, `budgetedAmount`, `actualAmount`, `variance`, `variancePercent`

---

## 4. Standard Journal Entry Templates

These are the pre-defined journal entry patterns posted automatically by listeners:

| Business Event | Dr | Cr |
|---|---|---|
| POS Cash Sale | Cash in Till (Asset) | Sales Revenue (Revenue) |
| POS Card Sale | Accounts Receivable / Clearing (Asset) | Sales Revenue (Revenue) |
| Payment Gateway Settlement | Bank Account (Asset) | Clearing Account (Asset) |
| Supplier Invoice Received | Inventory Asset (Asset) | Accounts Payable (Liability) |
| Supplier Paid | Accounts Payable (Liability) | Bank Account (Asset) |
| Customer Return Approved | Sales Revenue (Revenue) | Cash / Accounts Receivable (Asset) |
| Payroll Approved | Salary Expense (Expense) | Payroll Payable (Liability) |
| Payroll Disbursed | Payroll Payable (Liability) | Payroll Reserve Account (Asset via Pay) |
| PAYE Tax Accrued | PAYE Expense (Expense) | PAYE Tax Payable (Liability) |
| PAYE Remitted | PAYE Tax Payable (Liability) | Bank Account (Asset) |
| Loan Disbursed to Employee | Loan Receivable (Asset) | Bank Account (Asset) |
| Loan Repayment Received | Bank Account (Asset) | Loan Receivable (Asset) |
| Asset Depreciation | Depreciation Expense (Expense) | Accumulated Depreciation (Contra-Asset) |
| Inter-Outlet Stock Transfer | Inventory Asset – Dest Outlet (Asset) | Inventory Asset – Source Outlet (Asset) |
| Cash Evacuation (Till → Bank) | Bank Account (Asset) | Cash in Till (Asset) |
| Platform Subscription Invoice Paid | Platform Subscription Expense (Expense) | Bank Account / Clearing (Asset) |

---

## 5. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `JournalEntryPostedEvent` | Entry posted | `audit` (log), `notifications` (alert finance manager on manual posts) |
| `ExpenseApprovedEvent` | Expense approved | Internal (triggers AP liability posting) |
| `AssetDepreciatedEvent` | Depreciation run completes | `notifications` (monthly depreciation report) |
| `CashEvacuatedEvent` | Cash physically moved to bank | `pay` (trigger `ReconcileTillCommand`) |
| `BudgetApprovedEvent` | Budget activated | Internal |

---

## 6. Exceptions & Errors

**`AccountingErrorCode`**:
- `ACCOUNT_NOT_FOUND`, `DUPLICATE_ACCOUNT_CODE`
- `JOURNAL_UNBALANCED` — Debits do not equal Credits
- `INVALID_ENTRY_STATE`, `ASSET_NOT_FOUND`
- `EXPENSE_NOT_FOUND`, `INVALID_RECONCILIATION`
- `BUDGET_NOT_FOUND`, `BUDGET_ALREADY_APPROVED`

---

## 7. Commands & Use Cases

- `CreateAccountCommand(orgId, code, name, type)` → `CreateAccountUseCase`
- `RecordJournalEntryCommand(orgId, date, ref, desc, lines)` → `RecordJournalEntryUseCase`
- `VoidJournalEntryCommand(entryId, reason)` → `VoidJournalEntryUseCase`
- `RecordExpenseCommand(orgId, categoryId, amount, vendor, date, desc)` → `RecordExpenseUseCase`
- `ApproveExpenseCommand(expenseId)` → `ApproveExpenseUseCase`
- `MarkExpensePaidCommand(expenseId)` → `MarkExpensePaidUseCase`
- `RegisterAssetCommand(...)` → `RegisterAssetUseCase`
- `RunDepreciationCommand(orgId, date)` → `RunDepreciationUseCase` (scheduled monthly)
- `DisposeAssetCommand(assetId, saleValue)` → `DisposeAssetUseCase`
- `ReconcileBankStatementCommand(...)` → `ReconcileBankStatementUseCase`
- `EvacuateCashCommand(outletId, amount)` → `EvacuateCashUseCase`
- `CreateBudgetCommand(orgId, accountId, name, period, amount)` → `CreateBudgetUseCase`
- `ApproveBudgetCommand(budgetId)` → `ApproveBudgetUseCase`

---

## 8. Queries

- `GetChartOfAccountsQuery(orgId)` → `List<AccountResult>`
- `GetLedgerQuery(orgId, accountId, dateFrom, dateTo)` → `List<JournalLineResult>`
- `GetAccountBalanceQuery(orgId, accountId, asOf)` → `Money`
- `GetTrialBalanceQuery(orgId, date)` → `TrialBalanceReport`
- `GetIncomeStatementQuery(orgId, dateFrom, dateTo)` → `IncomeStatementReport`
- `GetBalanceSheetQuery(orgId, date)` → `BalanceSheetReport`
- `GetCashFlowStatementQuery(orgId, dateFrom, dateTo)` → `CashFlowReport`
- `ListExpensesQuery(orgId, status, dateFrom, dateTo)`
- `ListAssetsQuery(orgId, status)`
- `GetBudgetVarianceQuery(budgetId)` → `BudgetVariance`
- `ListBudgetsQuery(orgId, period, status)` → `List<BudgetResult>`

---

## 9. Listeners

- **`PosSaleCompletedListener`**: Listens to `PosSaleCompletedEvent` (from Commerce). Posts: Debit Cash/AR, Credit Sales Revenue.
- **`PosSaleRefundedListener`**: Reverses the POS sale journal entry.
- **`TillClosedListener`**: Listens to `TillClosedEvent` (from Commerce). Posts: Debit Bank Account, Credit Cash in Till (on evacuation confirmation).
- **`PurchaseOrderReceivedListener`**: Listens to `PurchaseOrderReceivedEvent` (from Commerce). Posts: Debit Inventory Asset, Credit Accounts Payable.
- **`StockTransferReceivedListener`**: Listens to `StockTransferReceivedEvent` (from Logistics). Posts inter-outlet inventory movement: Debit Inventory (Dest), Credit Inventory (Source).
- **`PayrollApprovedListener`**: Listens to `PayrollApprovedEvent` (from HR). Posts: Debit Salary Expense, Credit Payroll Payable. Posts PAYE statutory deduction entry separately.
- **`PayrollDisbursedListener`**: Listens to `PayrollDisbursedEvent` (from HR). Posts: Debit Payroll Payable, Credit Bank/Payroll Reserve Account.
- **`EmployeeLoanApprovedListener`**: Listens to `EmployeeLoanApprovedEvent` (from HR). Posts: Debit Loan Receivable, Credit Bank Account.
- **`SettlementConfirmedListener`**: Listens to `SettlementConfirmedEvent` (from Pay). Posts: Debit Bank Account, Credit Clearing Account.
- **`LedgerBridgeListener`**: Listens to `LedgerTransactionPostedEvent` (from Pay). Maps each Pay ledger transaction to the appropriate accounting journal entry using the organization's `LedgerAccountMapping` configuration. This is the **primary bridge** between the operational and accounting ledgers.
- **`CashEvacuatedListener`**: Listens to `CashEvacuatedEvent` (from Accounting's own cash submodule after confirmation). Ensures the operational ledger in Pay is updated via `ReconcileTillCommand`.

---

## 10. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **No Locks on raw Account**: The `Account` entity has no mutable balance field — eliminating lock contention on high-volume accounts (Revenue, Cash). Balances are derived dynamically.
- **Pessimistic Locking**: Applied only during `BalanceSnapshot` generation to prevent concurrent snapshots from writing incorrect values.

### Inbox & Outbox Patterns
- **Inbox (`EventDeliveryTracker`)**: **CRITICAL**. The entire Accounting module relies on consuming events from other modules. Every listener uses the Inbox pattern with `(eventId, consumerId)` composite key to guarantee that a `JournalEntry` is **never posted twice** for the same operational event. Without this, a retry storm from the event bus would create duplicate journal entries and corrupt the books.
- **Outbox**: Publishes `JournalEntryPostedEvent` for audit and notification purposes.
