# Accounting & ERP Module Design (`atlashub-accounting`)

## 1. Domain Entities & Aggregates

### `gl` (General Ledger) Submodule
**`Account` (Aggregate Root - Chart of Accounts)**
- **Fields**: `id`, `organizationId`, `code` (e.g., "1001"), `name` (e.g., "Cash in Hand"), `type`: `AccountType` (ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE), `isActive`: Boolean
- **Note**: `Account` does **NOT** have a mutable `balance` field or `credit()`/`debit()` methods. This deliberately avoids lock contention on high-volume accounts. The balance is always derived from `JournalLine` entries.
- **Methods**: `deactivate()`

**`BalanceSnapshot` (Entity)**
Periodically computed and stored for fast balance queries, avoiding full `JournalLine` aggregation on every read.
- **Fields**: `id`, `accountId`, `snapshotDate`: LocalDate, `balance`: **`Money`**, `createdAt`: ZonedDateTime
- A balance query adds any `JournalLine`s created after the latest `BalanceSnapshot.createdAt` to produce the current balance.

**`JournalEntry` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `date`: LocalDate, `reference`: String, `description`: String, `lines`: `List<JournalLine>`, `status` (DRAFT, POSTED, VOIDED)
- **Methods**: `addLine(Long accountId, Money amount, EntryType type)`, `post()` (validates sum(DEBIT) == sum(CREDIT)), `voidEntry()`

**`JournalLine` (Entity)**
- **Fields**: `id`, `journalEntryId`, `accountId`, `amount`: **`Money`**, `type`: `EntryType` (DEBIT, CREDIT)

### `ap` & `ar` (Payables & Receivables) Submodule
**`Expense` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `categoryId`, `amount`: **`Money`**, `vendorName`, `date`, `description`, `status` (PENDING, APPROVED, PAID)
- **Methods**: `approve()`, `markPaid()`

### `assets` Submodule
**`Asset` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `name`, `tagNumber`, `purchaseDate`, `purchaseValue`: **`Money`**, `salvageValue`: **`Money`**, `usefulLifeMonths`, `accumulatedDepreciation`: **`Money`**, `currentValue`: **`Money`**, `status` (ACTIVE, DISPOSED)
- **Methods**: `depreciate(Money amount)`, `dispose(Money saleValue)`

### `cash` Submodule
**`BankReconciliation` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `bankAccountId`, `statementDate`, `statementBalance`: **`Money`**, `bookBalance`: **`Money`**, `difference`: **`Money`**, `status` (DRAFT, RECONCILED)
**`CashEvacuation` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `outletId`, `amount`: **`Money`**, `date`, `evacuatedBy`, `status` (PENDING, CONFIRMED)

## 2. Domain Events
- `AccountCreatedEvent(Long accountId, String code)`
- `JournalEntryPostedEvent(Long entryId, String reference)`
- `ExpenseRecordedEvent(Long expenseId, BigDecimal amount)`
- `ExpenseApprovedEvent(Long expenseId)`
- `AssetRegisteredEvent(Long assetId)`
- `AssetDepreciatedEvent(Long assetId, BigDecimal amount)`
- `CashEvacuatedEvent(Long evacuationId, BigDecimal amount)`

## 3. Exceptions & Errors
**`AccountingErrorCode`**:
- `ACCOUNT_NOT_FOUND`, `DUPLICATE_ACCOUNT_CODE`
- `JOURNAL_UNBALANCED` (Debits do not equal Credits)
- `INVALID_ENTRY_STATE`, `ASSET_NOT_FOUND`
- `EXPENSE_NOT_FOUND`, `INVALID_RECONCILIATION`

## 4. Commands & Use Cases
- `CreateAccountCommand(orgId, code, name, type)` -> `CreateAccountUseCase`
- `RecordJournalEntryCommand(orgId, date, ref, desc, lines)` -> `RecordJournalEntryUseCase` (Creates entry, calls `post()`, updates `Account` balances).
- `RecordExpenseCommand(orgId, categoryId, amount, vendor, date, desc)` -> `RecordExpenseUseCase`
- `ApproveExpenseCommand(expenseId)` -> `ApproveExpenseUseCase`
- `RegisterAssetCommand(name, tag, purchaseDate, value, salvage, usefulLife)` -> `RegisterAssetUseCase`
- `RunDepreciationCommand(orgId, date)` -> `RunDepreciationUseCase` (Calculates monthly depreciation for all active assets, records Journal Entry).
- `ReconcileBankStatementCommand(...)` -> `ReconcileBankStatementUseCase`
- `EvacuateCashCommand(outletId, amount)` -> `EvacuateCashUseCase`

## 5. Queries
- `GetChartOfAccountsQuery(orgId)` -> `List<AccountResult>`
- `GetLedgerQuery(orgId, accountId, dateFrom, dateTo)` -> `List<JournalLineResult>`
- `GetTrialBalanceQuery(orgId, date)` -> `TrialBalanceReport` (Lists all accounts and their debit/credit balances).
- `GetIncomeStatementQuery(orgId, dateFrom, dateTo)` -> `IncomeStatementReport` (Revenue - Expenses).
- `GetBalanceSheetQuery(orgId, date)` -> `BalanceSheetReport` (Assets = Liabilities + Equity).
- `GetCashFlowStatementQuery(orgId, dateFrom, dateTo)` -> `CashFlowReport`
- `ListExpensesQuery(orgId, status, dateFrom, dateTo)`
- `ListAssetsQuery(orgId, status)`

## 6. Listeners
- `PosSaleCompletedListener`: Listens to Commerce module. Triggers `RecordJournalEntryCommand` (Debit Cash/Receivable, Credit Sales Revenue).
- `PosSaleRefundedListener`: Reverses POS sale entry.
- `PayrollApprovedListener`: Listens to HR module. Triggers `RecordJournalEntryCommand` (Debit Salary Expense, Credit Payroll Payable).
- `PayrollDisbursedListener`: Triggers `RecordJournalEntryCommand` (Debit Payroll Payable, Credit Bank Account).
- `StockReceivedListener`: Listens to Commerce/Logistics. Triggers entry (Debit Inventory Asset, Credit Accounts Payable).
- `PaymentSuccessfulListener`: Listens to Pay module. Triggers entry (Debit Clearing Account, Credit Accounts Receivable/Wallet Liability).

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Balance Snapshots (No Locks on raw Account)**: The raw Account entity does *not* have a mutable balance field to prevent massive lock contention on high-volume accounts (like Revenue/Cash). The balance is derived dynamically from JournalLines.
- **Pessimistic Locking (@Lock(PESSIMISTIC_WRITE))**: Applied only to the periodic BalanceSnapshot generation tables.

### Inbox & Outbox Patterns
- **Inbox (EventDeliveryTracker)**: **CRITICAL**. The entire Accounting module relies on consuming events (PosSaleCompletedEvent, PayrollDisbursedEvent, PaymentSuccessfulEvent). The Inbox pattern uses the composite key (eventId, consumerId) to guarantee that a JournalEntry is **never posted twice** for the same operational event.
- **Outbox**: Publishes JournalEntryPostedEvent.
