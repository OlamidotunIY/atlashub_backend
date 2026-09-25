# Accounting & ERP Module Design (`atlashub-accounting`)

## Role & Purpose

The Accounting module is the **financial record and reporting engine** of AtlasHub. While `atlashub-pay` manages real-time money movement, Accounting maintains the **authoritative set of books** — debits, credits, assets, liabilities, expenses, and equity — expressed as journal entries in a double-entry general ledger.

Every significant operational event in the platform — a POS sale, a payroll disbursement, a supplier payment, a stock delivery, a cash deposit — automatically produces a corresponding journal entry in Accounting. This ensures organizations always have an up-to-date, legally defensible set of books without manual bookkeeping.

**This is a passive, event-driven module.** It never initiates operations. It listens to events from Commerce, HR, Pay, and Logistics and posts the appropriate journal entries. Finance managers can also post manual journal entries, subject to **Maker-Checker approval** for entries above a configured threshold.

Key questions this module answers:
- *What is our net profit this month?*
- *How much VAT have we collected versus remitted?*
- *What is the total value of our inventory across all outlets?*
- *Are we within budget for Q3?*
- *What does our balance sheet look like today?*

---

## 1. Two Ledgers — One Platform

AtlasHub operates two complementary ledgers:

| | `atlashub-pay:ledger` | `atlashub-accounting:gl` |
|---|---|---|
| **Purpose** | Real-time cash tracking | Financial reporting |
| **Model** | Double-entry, balance snapshots | Journal entries, Chart of Accounts |
| **Granularity** | Every money movement as it happens | Business-event level |
| **Users** | Treasury / cash management | Finance / accounting team |
| **Drives** | Wallet balances, available funds | P&L, Balance Sheet, Cash Flow |

They are bridged by the **`LedgerBridgeListener`**: when `LedgerTransactionPostedEvent` is published by Pay, Accounting maps the Pay ledger entry to the corresponding accounting journal entry using each organization's configurable `LedgerAccountMapping` table.

---

## 2. Features

### Chart of Accounts (COA)
Organizations define their own COA — the master list of all account codes used for bookkeeping. Account types: ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE. Standard accounts are pre-seeded on org onboarding (e.g., "Cash in Hand", "Accounts Receivable", "Sales Revenue", "PAYE Tax Payable").

### General Ledger & Journal Entries
All financial transactions flow through `JournalEntry` aggregates. Each `JournalEntry` has a set of balanced `JournalLine`s (Debits = Credits). Once posted, entries cannot be edited — only **voided and reversed** (a new equal-and-opposite entry). Balance queries use the latest `BalanceSnapshot` + delta journal lines to avoid full table scans.

### Maker-Checker on Manual Journal Entries
Manual entries above ₦500,000 (configurable per org) require a secondary authorization from a different user with `accounting:journal:approve` permission before they are posted. The entry is created in PENDING_APPROVAL state and visible to approvers via WebSocket notification.

### Accounts Payable & Receivable
- **Accounts Payable (AP)**: Tracks money the organization owes suppliers. Created when a `PurchaseOrderReceivedEvent` arrives. Cleared when the supplier is paid via `pay`.
- **Accounts Receivable (AR)**: Created on credit sales in Commerce. Cleared when the customer settles their debt.

### Asset Management & Depreciation
Organizations register fixed assets (computers, vehicles, machinery). A scheduled monthly job (`RunDepreciationUseCase`) calculates straight-line depreciation for each active asset and posts:
```
DEBIT  Depreciation Expense      ₦X
CREDIT Accumulated Depreciation  ₦X
```

### Bank Reconciliation
Finance managers can upload bank statements and reconcile them against the accounting ledger. The reconciliation identifies: matched entries, unmatched ledger entries (outstanding deposits), and unmatched bank entries (outstanding payments).

### Budget Management
Organizations define `Budget`s per account per period (e.g., ₦2M for Marketing in Q3 2026). `GetBudgetVarianceQuery` compares budgeted vs actual spend by reading journal line totals for that account and period — no separate running total is maintained.

### Financial Reports (On-Demand)
All reports are computed from journal lines + balance snapshots:
- **Trial Balance**: All accounts with debit/credit totals
- **Income Statement (P&L)**: Revenue - Expenses for a period
- **Balance Sheet**: Assets = Liabilities + Equity at a point in time
- **Cash Flow Statement**: Operating, investing, and financing cash flows

---

## 3. Domain Entities & Aggregates

### `gl` (General Ledger) Submodule

**`Account` (Aggregate Root — Chart of Accounts)**
```
Account
├── id: Long
├── organizationId: Long
├── code: String                     ← e.g., "1001" — unique per org
├── name: String                     ← e.g., "Cash in Hand"
├── type: AccountType                ← ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
├── parentAccountId: Long            ← nullable — for hierarchical COA
├── isSystemAccount: Boolean         ← true for auto-seeded accounts, cannot be deleted
├── isActive: Boolean
└── createdAt: ZonedDateTime
```

**Domain Rule**: `Account` has **NO mutable balance field**. Balance is always derived from `JournalLine` entries + `BalanceSnapshot`. This deliberately eliminates lock contention on high-volume accounts (Revenue, Cash).

**Business Methods:** `deactivate()` — guards: cannot deactivate if any open journal lines reference this account

---

**`BalanceSnapshot` (Entity)**
```
BalanceSnapshot
├── id: Long
├── accountId: Long
├── snapshotDate: LocalDate
├── balance: Money
└── createdAt: ZonedDateTime
```
Periodically computed (nightly job). Balance queries use: `latest snapshot balance + sum of journal lines since snapshot date`.

---

**`JournalEntry` (Aggregate Root)**
```
JournalEntry
├── id: Long
├── organizationId: Long
├── entryNumber: String              ← unique, e.g., "JE-2026-09-00042"
├── date: LocalDate
├── reference: String                ← links to originating business event
├── description: String
├── lines: List<JournalLine>
├── status: JournalEntryStatus       ← DRAFT, PENDING_APPROVAL, POSTED, VOIDED
├── source: EntrySource              ← AUTOMATIC (event-driven) | MANUAL
├── initiatedBy: Long                ← userId, nullable for automatic entries
├── approvedBy: Long                 ← userId, nullable
├── approvedAt: ZonedDateTime        ← nullable
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `addLine(Long accountId, Money amount, EntryType type)` — accumulates debit/credit lines
- `submitForApproval(Long initiatorId)` → DRAFT → PENDING_APPROVAL (for large manual entries)
- `approve(Long approverId)` → **validates approverId ≠ initiatedBy** → PENDING_APPROVAL → POSTED → registers `JournalEntryPostedEvent`
- `post()` → validates ∑DEBIT == ∑CREDIT; if not balanced, throws `JOURNAL_UNBALANCED`; transitions → POSTED (for automatic/small entries)
- `voidEntry(Long voidedBy, String reason)` → POSTED → VOIDED; creates a reversing entry automatically

**Domain Rule (Maker-Checker)**: The `approve()` method enforces `approverId ≠ initiatedBy` at the domain level, same pattern as HR payroll.

---

**`JournalLine` (Entity — immutable after posting)**
```
JournalLine
├── id: Long
├── journalEntryId: Long
├── accountId: Long
├── amount: Money
└── type: EntryType                  ← DEBIT | CREDIT
```

---

**`LedgerAccountMapping` (Entity)**
Maps a Pay `SourceSystem` to the accounting accounts to debit and credit. This drives the `LedgerBridgeListener`.

```
LedgerAccountMapping
├── id: Long
├── organizationId: Long
├── sourceSystem: SourceSystem       ← COMMERCE_CHECKOUT, PAYROLL, etc.
├── debitAccountId: Long             ← which GL account to debit
├── creditAccountId: Long            ← which GL account to credit
└── description: String
```

---

### `ap` & `ar` Submodule

**`SupplierInvoice` (Aggregate Root — Accounts Payable)**
```
SupplierInvoice
├── id: Long
├── organizationId: Long
├── supplierId: Long
├── purchaseOrderId: Long
├── invoiceNumber: String            ← supplier's invoice number
├── amount: Money
├── dueDate: LocalDate
├── status: ApStatus                 ← UNPAID, PARTIALLY_PAID, PAID, DISPUTED
└── paidAt: ZonedDateTime
```

**`CustomerReceivable` (Aggregate Root — Accounts Receivable)**
```
CustomerReceivable
├── id: Long
├── organizationId: Long
├── customerId: Long
├── salesOrderId: Long
├── amount: Money
├── dueDate: LocalDate
└── status: ArStatus                 ← OUTSTANDING, PARTIALLY_COLLECTED, COLLECTED, WRITTEN_OFF
```

---

### `assets` Submodule

**`Asset` (Aggregate Root)**
```
Asset
├── id: Long
├── organizationId: Long
├── name: String
├── tagNumber: String
├── category: String                 ← e.g., "Computer Equipment", "Motor Vehicle"
├── purchaseDate: LocalDate
├── purchaseValue: Money
├── salvageValue: Money
├── usefulLifeMonths: Integer
├── depreciationMethod: DepreciationMethod ← STRAIGHT_LINE (MVP), REDUCING_BALANCE (future)
├── accumulatedDepreciation: Money
├── currentBookValue: Money
└── status: AssetStatus              ← ACTIVE, FULLY_DEPRECIATED, DISPOSED
```

**Business Methods:**
- `depreciate(Money amount)` → reduces `currentBookValue`, increases `accumulatedDepreciation`
- `dispose(Money saleValue)` → transitions to DISPOSED → posts disposal gain/loss journal entry

---

### `cash` Submodule

**`BankReconciliation` (Aggregate Root)**
```
BankReconciliation
├── id: Long
├── organizationId: Long
├── bankAccountCode: String          ← the GL account code for this bank account
├── statementDate: LocalDate
├── statementClosingBalance: Money
├── bookBalance: Money               ← sum of GL entries for this account
├── difference: Money
├── matchedItems: List<ReconciliationItem>
├── unmatchedBankItems: List<BankStatementLine>
├── unmatchedBookItems: List<JournalLine>
└── status: ReconciliationStatus     ← DRAFT, RECONCILED
```

**`CashEvacuation` (Aggregate Root)**
```
CashEvacuation
├── id: Long
├── organizationId: Long
├── outletId: Long
├── tillId: Long
├── amount: Money
├── evacuatedBy: Long
├── evacuatedAt: ZonedDateTime
└── status: EvacuationStatus         ← PENDING, CONFIRMED
```

---

### `budget` Submodule

**`Budget` (Aggregate Root)**
```
Budget
├── id: Long
├── organizationId: Long
├── accountId: Long
├── name: String
├── period: String                   ← e.g., "2026-Q3", "2026-09"
├── budgetedAmount: Money
├── status: BudgetStatus             ← DRAFT, APPROVED, CLOSED
├── approvedBy: Long
└── approvedAt: ZonedDateTime
```

---

## 4. Standard Journal Entry Templates

These are posted automatically by event listeners:

| Business Event | Dr | Cr |
|---|---|---|
| POS Cash Sale | Cash in Till (Asset) | Sales Revenue (Revenue) |
| POS Card Sale | Clearing / AR (Asset) | Sales Revenue (Revenue) |
| Payment Gateway Settlement | Bank Account (Asset) | Clearing Account (Asset) |
| Supplier Invoice Received | Inventory Asset (Asset) | Accounts Payable (Liability) |
| Supplier Paid via Payout | Accounts Payable (Liability) | Bank Account (Asset) |
| Customer Return Approved | Sales Revenue (Revenue) | Cash / AR (Asset) |
| Payroll Approved | Salary Expense (Expense) | Payroll Payable (Liability) |
| Payroll Disbursed | Payroll Payable (Liability) | Payroll Reserve Account (Asset) |
| PAYE Tax Accrued | PAYE Expense (Expense) | PAYE Tax Payable (Liability) |
| PAYE Remitted | PAYE Tax Payable (Liability) | Bank Account (Asset) |
| Loan Disbursed to Employee | Loan Receivable (Asset) | Bank Account (Asset) |
| Loan Repayment | Bank Account (Asset) | Loan Receivable (Asset) |
| Asset Depreciation | Depreciation Expense (Expense) | Accumulated Depreciation (Contra-Asset) |
| Inter-Outlet Stock Transfer | Inventory Asset – Dest (Asset) | Inventory Asset – Source (Asset) |
| Cash Evacuation (Till → Bank) | Bank Account (Asset) | Cash in Till (Asset) |
| Platform Subscription Paid | Platform Subscription Expense (Expense) | Bank / Clearing (Asset) |

---

## 5. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `JournalEntryPostedEvent` | Entry posted | `audit` (log), `analytics` (update financial metrics) |
| `JournalEntryPendingApprovalEvent` | Large manual entry submitted | `notifications` (WebSocket + email to approvers) |
| `AssetDepreciatedEvent` | Monthly depreciation run completes | `notifications` (monthly depreciation report) |
| `BudgetApprovedEvent` | Budget activated | `analytics` (load budget baseline for variance tracking) |
| `CashEvacuatedEvent` | Cash moved to bank | `pay` (trigger `ReconcileTillCommand`) |

---

## 6. Exceptions & Errors

**`AccountingErrorCode`**:
- `ACCOUNT_NOT_FOUND`, `DUPLICATE_ACCOUNT_CODE`, `SYSTEM_ACCOUNT_IMMUTABLE`
- `JOURNAL_UNBALANCED` — ∑Debits ≠ ∑Credits
- `INVALID_ENTRY_STATE`, `SELF_APPROVAL_NOT_ALLOWED`
- `ENTRY_AMOUNT_EXCEEDS_APPROVAL_THRESHOLD`
- `ASSET_NOT_FOUND`, `ASSET_ALREADY_DISPOSED`
- `BUDGET_NOT_FOUND`, `BUDGET_ALREADY_APPROVED`
- `RECONCILIATION_NOT_FOUND`, `INVALID_RECONCILIATION_STATE`
- `MAPPING_NOT_FOUND` — `LedgerAccountMapping` missing for a `SourceSystem`

---

## 7. Commands & Use Cases

### Chart of Accounts
- `CreateAccountCommand(orgId, code, name, type, parentAccountId)` → `CreateAccountUseCase`
- `DeactivateAccountCommand(accountId)` → `DeactivateAccountUseCase`
- `SeedDefaultChartOfAccountsCommand(orgId)` → `SeedDefaultChartOfAccountsUseCase` ← triggered on org approval

### Journal Entries
- `RecordJournalEntryCommand(orgId, date, ref, desc, lines, initiatedBy)` → `RecordJournalEntryUseCase`
  - If `amount > approvalThreshold`: creates in PENDING_APPROVAL, notifies approvers
  - Otherwise: validates balance, posts immediately
- `ApproveJournalEntryCommand(entryId, approverId)` → `ApproveJournalEntryUseCase` — validates approver ≠ initiator
- `VoidJournalEntryCommand(entryId, voidedBy, reason)` → `VoidJournalEntryUseCase` — creates reversing entry

### AP/AR
- `RecordSupplierInvoiceCommand(orgId, supplierId, poId, invoiceNumber, amount, dueDate)` → `RecordSupplierInvoiceUseCase`
- `MarkSupplierInvoicePaidCommand(invoiceId, payoutReference)` → `MarkSupplierInvoicePaidUseCase`
- `WriteOffReceivableCommand(receivableId, reason)` → `WriteOffReceivableUseCase`

### Assets
- `RegisterAssetCommand(orgId, name, tagNumber, category, purchaseDate, purchaseValue, salvageValue, usefulLifeMonths)` → `RegisterAssetUseCase`
- `RunDepreciationCommand(orgId, asOfDate)` → `RunDepreciationUseCase` ← scheduled monthly
- `DisposeAssetCommand(assetId, saleValue, disposalDate)` → `DisposeAssetUseCase`

### Bank Reconciliation
- `StartBankReconciliationCommand(orgId, bankAccountCode, statementDate, statementBalance)` → `StartBankReconciliationUseCase`
- `MatchReconciliationItemCommand(reconciliationId, bankLineId, journalLineId)` → `MatchReconciliationItemUseCase`
- `FinalizeReconciliationCommand(reconciliationId)` → `FinalizeReconciliationUseCase`

### Budgets
- `CreateBudgetCommand(orgId, accountId, name, period, amount)` → `CreateBudgetUseCase`
- `ApproveBudgetCommand(budgetId, approvedBy)` → `ApproveBudgetUseCase`

---

## 8. Queries

- `GetChartOfAccountsQuery(orgId)` → `List<AccountResult>`
- `GetAccountBalanceQuery(orgId, accountId, asOf)` → `Money`
- `GetLedgerQuery(orgId, accountId, dateFrom, dateTo)` → `List<JournalLineResult>`
- `GetTrialBalanceQuery(orgId, date)` → `TrialBalanceReport`
- `GetIncomeStatementQuery(orgId, dateFrom, dateTo)` → `IncomeStatementReport`
- `GetBalanceSheetQuery(orgId, date)` → `BalanceSheetReport`
- `GetCashFlowStatementQuery(orgId, dateFrom, dateTo)` → `CashFlowReport`
- `GetBudgetVarianceQuery(budgetId)` → `BudgetVarianceResult`
- `ListJournalEntriesQuery(orgId, status, dateFrom, dateTo)` → `Page<JournalEntryResult>`
- `ListAssetsQuery(orgId, status)` → `List<AssetResult>`
- `ListSupplierInvoicesQuery(orgId, supplierId, status)` → `List<SupplierInvoiceResult>`
- `GetOpenReceivablesQuery(orgId)` → `List<CustomerReceivableResult>`

---

## 9. Listeners

- **`PosSaleCompletedListener`**: Posts: Dr Cash/AR, Cr Sales Revenue
- **`PosSaleRefundedListener`**: Posts reversing entry for the original sale
- **`TillClosedListener`**: Posts: Dr Bank Account, Cr Cash in Till (if cash evacuation)
- **`PurchaseOrderReceivedListener`**: Posts: Dr Inventory Asset, Cr Accounts Payable
- **`StockTransferReceivedListener`**: Posts: Dr Inventory (Dest), Cr Inventory (Source)
- **`PayrollApprovedListener`**: Posts: Dr Salary Expense, Cr Payroll Payable; Posts PAYE deduction entry separately
- **`PayrollDisbursedListener`**: Posts: Dr Payroll Payable, Cr Payroll Reserve Account
- **`LoanApprovedListener`**: Posts: Dr Loan Receivable, Cr Bank Account
- **`SettlementConfirmedListener`**: Posts: Dr Bank Account, Cr Clearing Account
- **`LedgerBridgeListener`**: Listens to `LedgerTransactionPostedEvent` from Pay. Maps each Pay source system to GL accounts using `LedgerAccountMapping`. This is the **primary bridge** between the two ledgers.

---

## 10. Distributed Architecture

### Locking Strategy
- **No Lock on `Account`**: Balance is derived dynamically — eliminates hot-row lock contention on Revenue/Cash accounts
- **Pessimistic Locking**: `BalanceSnapshot` during snapshot generation — prevents concurrent snapshots writing incorrect values
- **Optimistic Locking**: `JournalEntry`, `Asset`, `Budget`, `BankReconciliation`

### Inbox & Outbox
- **Inbox (`EventDeliveryTracker`)**: **CRITICAL.** Every listener uses the Inbox pattern with `(eventId, consumerId)` as the idempotency key. A retry storm from the event bus must **never** post duplicate journal entries — duplicate entries corrupt the books permanently.
- **Outbox**: `JournalEntryPostedEvent` — published for audit and analytics consumption
