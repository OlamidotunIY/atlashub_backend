# Payment Module Design (`atlashub-pay`)

## Role & Purpose

The Pay module is the **financial rails** of the AtlasHub platform. Every movement of money — whether a customer paying for a POS sale, a payroll disbursement to employees, an inter-outlet cash transfer, or an invoice payment to the platform — flows through this module's infrastructure. No other module holds, moves, or accounts for real money independently; they all delegate money movement to `atlashub-pay`.

The Pay module does **not** decide the business purpose of a money movement. When Commerce completes a sale, it does not call Pay and say "handle this payment". It publishes an event and Pay responds with its own internal logic. Pay's job is to ensure money is correctly credited, debited, tracked in the ledger, and reported — regardless of where the instruction came from.

Internally, Pay is split into specialized submodules:
- **`accounts`** — Virtual bank accounts (NUBANs) issued per organization via external providers (Anchor)
- **`ledger`** — Double-entry accounting ledger for all internal money movements
- **`charges`** — Inbound payments from customers (card, bank transfer, USSD, POS terminal) via Paystack/Moniepoint
- **`transfers`** — Outbound payouts to bank accounts (employee salaries, supplier payments, refunds)
- **`splits`** — Revenue sharing rules applied to incoming payments
- **`subscriptions`** — Payment mandates (recurring card charge authorizations from end-customers)
- **`settlement`** — Tracking of settlement batches from payment processors to the organization's bank
- **`transactions-query`** — Unified read model for querying the full transaction history

---

## 1. How External Platforms Integrate

AtlasHub does not hold money directly. It works through licensed financial infrastructure providers. Here is how each integrates:

### Anchor (Virtual Account Issuance)
Anchor is a Banking-as-a-Service provider that issues real NUBAN (Nigerian Uniform Bank Account Number) accounts. When an organization's KYC is approved:
1. `OrganizationComplianceApprovedListener` in `pay:accounts` calls `IssueVirtualAccountUseCase`.
2. This calls Anchor's API via `AnchorVirtualAccountAdapter` to create a virtual account linked to AtlasHub's pool account.
3. Anchor issues a NUBAN and sends a webhook confirming activation.
4. `ActivateVirtualAccountUseCase` is called by the inbound webhook adapter, assigns the NUBAN, activates the `VirtualAccount`, and publishes `VirtualAccountActivatedEvent`.

When someone makes a bank transfer to that NUBAN:
- Anchor sends a `collection` webhook to AtlasHub.
- An inbound webhook adapter processes it and calls `FundWalletCommand` → credits the org's **Operating Account** in the ledger → publishes `WalletFundedEvent`.

### Paystack / Moniepoint (Payment Collection)
For card payments, USSD, and POS terminal transactions:
1. AtlasHub calls Paystack's or Moniepoint's API via the `charges` module adapter to initialize a charge.
2. Paystack/Moniepoint redirects the customer through their payment flow.
3. On success/failure, a webhook is sent to AtlasHub.
4. The inbound webhook adapter in `charges` calls `ProcessWebhookPaymentUseCase`:
   - Validates the webhook signature.
   - Updates the `PaymentTransaction` aggregate status.
   - On success, publishes `PaymentSuccessfulEvent` (consumed by Commerce, Billing, and the ledger).
   - On failure, publishes `PaymentFailedEvent` (consumed by Commerce to release reserved stock).

### Moniepoint (Physical POS Terminal)
For Moniepoint POS hardware terminal transactions:
- Moniepoint sends a terminal transaction webhook.
- Same path as above through the `charges` webhook adapter.

### The Shadow Ledger Principle
AtlasHub's internal pay ledger is a **shadow of real money held at Anchor/Paystack**. Every time a real money event occurs (Anchor confirms a deposit, Paystack confirms a collection, a payout settles), AtlasHub posts a corresponding `LedgerTransaction` to its internal double-entry ledger. This gives AtlasHub a real-time, independently auditable record of every organization's balance — without relying solely on external provider APIs.

---

## 2. Internal Account Structure

Every organization on AtlasHub has a set of internal **logical accounts** created at onboarding (bootstrapped on `OrganizationRegistered`). These are ledger accounts — not bank accounts. They exist in the Pay ledger to track money segregated by purpose:

| Account Name | Type | Purpose |
|---|---|---|
| **Operating Account** | Asset | Day-to-day inflows and outflows (sales revenue, supplier payments, withdrawals) |
| **Payroll Reserve Account** | Asset | Funds explicitly set aside before payroll disbursement; locked until payroll is approved |
| **Tax Holding Account** | Liability | VAT, PAYE, and other statutory deductions collected but not yet remitted to FIRS/LIRS |
| **Escrow Account** | Asset | Funds held during a commerce transaction pending delivery confirmation (released on delivery or refunded on failure) |
| **Suspense Account** | Asset | Temporary holding account for inter-outlet transfers and unresolved entries while in transit |
| **Till Accounts** (per outlet) | Asset | One account per POS till. Cash collected at the till is tracked here until reconciled to the Operating Account |

Every `LedgerEntry` references one of these account IDs. This means every money movement has a clear source and destination.

---

## 3. The Double-Entry Ledger (`pay:ledger`)

The ledger submodule is the **core financial engine** of the Pay module. It enforces double-entry bookkeeping: every `LedgerTransaction` must contain at least one DEBIT entry and one CREDIT entry, and the sum of all DEBITs must equal the sum of all CREDITs. If they don't balance, the transaction is rejected.

### Key Domain Models
- **`LedgerTransaction`**: The top-level aggregate. Groups the balanced set of `LedgerEntry` records. Validated on construction — imbalanced transactions cannot be persisted.
- **`LedgerEntry`**: A single line in a ledger transaction: which account, DEBIT or CREDIT, amount, and running balance after this entry.
- **`BalanceSnapshot`**: Periodically stored balance for a given account, used to avoid re-scanning all entries from the beginning of time on each balance query. Balance queries use the latest snapshot + any entries after it.
- **`TransactionReference`**: Links a `LedgerTransaction` back to its originating business event via `(transactionId, SourceSystem)`. The `SourceSystem` enum is critical for traceability.

### Extended `SourceSystem` Values
Every ledger transaction knows where it came from:

| SourceSystem | Originating Event |
|---|---|
| `COMMERCE_CHECKOUT` | POS sale completed in Commerce |
| `PLATFORM_BILLING` | Organization subscription invoice paid |
| `PAYROLL` | Payroll disbursement from HR |
| `LOAN_DISBURSEMENT` | Employee loan approved in HR |
| `INTER_OUTLET_TRANSFER` | Cash or stock value transferred between outlets |
| `CASH_BANKING` | Till cash deposited to organization's bank |
| `EXTERNAL_COLLECTION` | Customer bank transfer to virtual NUBAN (from Anchor) |
| `CARD_CHARGE` | Card/USSD/POS payment processed via Paystack/Moniepoint |
| `PAYOUT` | Outbound bank transfer (salary, supplier, refund) |
| `REFUND` | Customer refund posted |
| `SPLIT` | Revenue share distributed to split account |
| `MANUAL` | Admin-initiated manual entry |
| `SYSTEM` | Auto-generated system entry (e.g., fee calculation) |

### Example Ledger Transactions

**Commerce POS Sale (Cash Payment)**:
```
DEBIT  Till Account (Outlet A, Till 1)   ₦25,000   [cash received in till]
CREDIT Operating Account                 ₦25,000   [revenue recognized]
SourceSystem: COMMERCE_CHECKOUT | ref: salesOrder-1042
```

**Platform Billing Invoice Paid**:
```
DEBIT  Operating Account                 ₦50,000   [payment for subscription]
CREDIT Billing Payable (Clearing)        ₦50,000   [clears the outstanding invoice]
SourceSystem: PLATFORM_BILLING | ref: invoice-891
```

**Payroll Disbursement**:
```
DEBIT  Payroll Reserve Account           ₦2,500,000  [deduct from reserve]
CREDIT Payout Clearing Account           ₦2,500,000  [in-transit to bank accounts]
SourceSystem: PAYROLL | ref: payrollRun-23
```

**Inter-Outlet Cash Transfer (Outlet A → Outlet B)**:
```
DEBIT  Suspense Account                  ₦100,000   [funds leave source]
CREDIT Till Account (Outlet A, Till 1)  ₦100,000   [debit the source till]

DEBIT  Till Account (Outlet B, Till 2)  ₦100,000   [credit destination till]
CREDIT Suspense Account                  ₦100,000   [clears suspense on confirmation]
SourceSystem: INTER_OUTLET_TRANSFER | ref: transfer-78
```

---

## 4. Virtual Accounts (`pay:accounts`)

**`VirtualAccount` (Aggregate Root)**
Represents a real bank account (NUBAN) issued by Anchor, linked to an organization.
- **Fields**: `id`, `integration` (org's integration ID), `UserCode`, `accountName`, `bankName`, `idempotencyKey`, `currency`, `status` (PENDING_ISSUANCE, ACTIVE, CLOSURE_REQUESTED, CLOSED), `nuban`
- **Methods**: `activate(NUBAN nuban)`, `requestClosure()`, `close()`
- **Owner Types**: Accounts can be owned by an `ORGANIZATION` (the main operating account) or a `CUSTOMER` (a customer's dedicated virtual account for collections, used in split payment flows)

### Account Issuance Flow
1. `OrganizationComplianceApproved` event received.
2. `IssueVirtualAccountUseCase` calls Anchor API.
3. Anchor creates the account asynchronously and sends a webhook.
4. `ActivateVirtualAccountUseCase` assigns the NUBAN and activates the record.
5. `VirtualAccountActivatedEvent` is published — the org's operating account in the ledger is now live.

---

## 5. Inbound Payments (`pay:charges`)

**`PaymentTransaction` (Aggregate Root)**
Tracks a single customer payment attempt via an external gateway.
- **Fields**: `id`, `organizationId`, `amount`: `Money`, `channel` (CARD, BANK_TRANSFER, USSD, POS_TERMINAL), `status` (PENDING, SUCCESS, FAILED, REFUNDED), `reference`, `gatewayResponse`, `sourceSystem`
- **Methods**: `markSuccessful(String gatewayResponse)`, `markFailed(String reason)`, `refund()`

### Payment Flow
1. Commerce calls `InitializePaymentUseCase` with amount, currency, and optionally a `splitId`.
2. Pay calls Paystack/Moniepoint API, gets a `checkoutUrl` or terminal charge reference.
3. Customer pays externally.
4. Gateway sends a webhook to AtlasHub's inbound webhook endpoint.
5. `ProcessWebhookPaymentUseCase` validates the signature (HMAC), finds the `PaymentTransaction` by reference, calls `markSuccessful()` or `markFailed()`.
6. On success: posts a `LedgerTransaction` crediting the Operating Account, publishes `PaymentSuccessfulEvent`.
7. If a `splitId` was provided, the split rules are evaluated and additional ledger entries are posted to split recipients.

**Webhook Idempotency**: All gateway webhooks are processed through the Inbox pattern using `(webhookId, consumerId)` as the idempotency key. Duplicate webhook deliveries (which are common with Paystack) will be silently discarded after the first successful processing.

---

## 6. Outbound Transfers (`pay:transfers`)

**`Payout` (Aggregate Root)**
Represents an outbound bank transfer to an external account.
- **Fields**: `id`, `organizationId`, `amount`: `Money`, `destinationBankCode`, `destinationAccountNumber`, `destinationAccountName`, `status` (PENDING, PROCESSING, SUCCESS, FAILED), `reference`, `sourceSystem`, `sourceReferenceId`
- **Methods**: `process()`, `complete()`, `fail(String reason)`
- The `sourceSystem` + `sourceReferenceId` fields link every payout back to the originating business event (payroll run ID, refund ID, supplier invoice ID, etc.)

### Who Creates Payouts
| Originator | SourceSystem | Example |
|---|---|---|
| HR (payroll) | `PAYROLL` | Employee salaries after `PayrollApprovedEvent` |
| Commerce (refund) | `REFUND` | Customer refund after `CustomerReturnApprovedEvent` |
| Commerce (supplier) | `SUPPLIER_PAYMENT` | Supplier payment after PO receipt |
| Organization (manual withdrawal) | `MANUAL` | Owner withdrawing from operating wallet |
| HR (loan) | `LOAN_DISBURSEMENT` | Employee loan approved and disbursed |

---

## 7. Intra-Organization Transfers (`pay:ledger`)

Movements of money **within** an organization (between their own accounts) do not require external API calls. They are pure ledger transactions. Key examples:

**Funding Payroll Reserve**: Before running payroll, the org must fund their Payroll Reserve Account.
```
DEBIT  Operating Account          ₦5,000,000
CREDIT Payroll Reserve Account    ₦5,000,000
Command: FundPayrollReserveCommand(orgId, amount)
```

**Till Cash Reconciliation (End of Day)**: At day close, a till's balance is swept to the Operating Account.
```
DEBIT  Till Account (Till 1)      ₦180,000
CREDIT Operating Account          ₦180,000
Command: ReconcileTillCommand(tillId, userId, actualBalance)
```

**Inter-Outlet Float Transfer** (Outlet A sends float to Outlet B):
```
Phase 1 (dispatch):
  DEBIT  Suspense Account           ₦50,000
  CREDIT Till Account (Outlet A)    ₦50,000

Phase 2 (receipt confirmation):
  DEBIT  Till Account (Outlet B)    ₦50,000
  CREDIT Suspense Account           ₦50,000
Command: TransferInterOutletFloatCommand(sourceOutletId, destOutletId, amount)
```

---

## 8. Revenue Splits (`pay:splits`)

**`PaymentSplit` (Aggregate Root)**
Defines how incoming payments should be distributed among multiple recipients (e.g., a marketplace splitting revenue between the platform and a vendor).
- **Fields**: `id`, `organizationId`, `name`, `type` (PERCENTAGE, FLAT), `subaccounts`: `List<SplitAccount>` (accountId, share)

When an `InitializePaymentCommand` includes a `splitId`, the `charges` module evaluates the split rules at payment success and posts additional ledger entries crediting each split recipient's account.

---

## 9. Payment Mandates (`pay:subscriptions`)

> **Note on Naming**: The `subscriptions` submodule in `pay` is NOT related to platform billing subscriptions (those live in `atlashub-platform:billing`). This submodule manages **recurring payment mandates** — authorizations from end-customers to charge their cards on a recurring basis. Example: a customer paying for a subscription service offered **by the organization**, not AtlasHub.

**`SubscriptionMandate` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `customerId`, `planId`, `authorizationCode` (from Paystack), `status` (ACTIVE, REVOKED), `nextChargeDate`

---

## 10. Settlement (`pay:settlement`)

**`Settlement` (Aggregate Root)**
Tracks settlement batches from payment processors (Paystack settling collected funds to the organization's linked bank account).
- **Fields**: `id`, `organizationId`, `processorReference`, `amount`: `Money`, `settledAt`: ZonedDateTime, `status` (PENDING, CONFIRMED, DISPUTED)
- **Methods**: `confirm()`, `dispute(String reason)`

When Paystack settles a batch to the organization's bank:
1. A settlement webhook arrives.
2. A `Settlement` record is created and confirmed.
3. A `LedgerTransaction` is posted:
   ```
   DEBIT  Bank Account (External)     ₦X
   CREDIT Operating Account           ₦X
   SourceSystem: SETTLEMENT
   ```
4. `SettlementConfirmedEvent` is published for accounting to record.

---

## 11. Transaction History (`pay:transactions-query`)

A dedicated **read model** that aggregates entries from the ledger, charges, payouts, and splits into a unified, queryable transaction history. No writes happen here — it is populated by consuming events from other Pay submodules.

**Queries**:
- `ListTransactionsQuery(orgId, type, status, dateFrom, dateTo, channel)` → `List<TransactionResult>`
- `GetTransactionDetailsQuery(transactionId)` → `TransactionDetailsResult`
- `GetWalletBalanceQuery(orgId, accountType)` → `Money`
- `ListPayoutsQuery(orgId, status, sourceSystem)` → `List<PayoutResult>`
- `GetPaymentStatusQuery(reference)` → `PaymentStatusResult`

---

## 12. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `VirtualAccountCreatedEvent` | NUBAN issued | `notifications` (inform org) |
| `VirtualAccountActivatedEvent` | NUBAN activated by Anchor webhook | `notifications`, `accounting` (open ledger accounts) |
| `PaymentSuccessfulEvent` | Customer payment confirmed by gateway | `commerce` (complete sale), `billing` (mark invoice paid), `ledger` (credit operating account) |
| `PaymentFailedEvent` | Payment failed or declined | `commerce` (release reserved stock), `notifications` |
| `PayoutCompletedEvent` | Bank transfer confirmed successful | `hr` (mark salary disbursed), `accounting`, `notifications` |
| `PayoutFailedEvent` | Bank transfer failed | `hr` (revert payroll run), `notifications` |
| `WalletFundedEvent` | Operating Account credited | `accounting` (journal entry), `notifications` |
| `WalletDebitedEvent` | Operating Account debited | `accounting` (journal entry) |
| `SettlementConfirmedEvent` | Paystack/Moniepoint settles to bank | `accounting` (record settlement) |
| `BulkPayoutCompletedEvent` | All payroll payouts processed | `hr` (mark payroll DISBURSED) |
| `BulkPayoutFailedEvent` | One or more payroll payouts failed | `hr` (revert payroll to APPROVED for retry) |

---

## 13. Exceptions & Errors

**`PayErrorCode`**:
- `INSUFFICIENT_FUNDS`, `WALLET_NOT_FOUND`, `ACCOUNT_NOT_FOUND`
- `TRANSACTION_FAILED`, `TRANSACTION_NOT_FOUND`, `DUPLICATE_REFERENCE`
- `PAYOUT_FAILED`, `INVALID_BANK_DETAILS`
- `UNBALANCED_LEDGER_TRANSACTION`, `CURRENCY_MISMATCH`
- `WEBHOOK_SIGNATURE_INVALID`, `WEBHOOK_ALREADY_PROCESSED`
- `ACCOUNT_SUSPENDED`, `SETTLEMENT_NOT_FOUND`

---

## 14. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Pessimistic Locking (`@Lock(PESSIMISTIC_WRITE)`)**: **CRITICAL** for `BalanceSnapshot` during ledger posting. When posting a `LedgerTransaction`, all affected accounts are locked in a consistent sort order (ascending accountId) to prevent deadlocks while guaranteeing no two concurrent transactions corrupt the same account balance.
- **Pessimistic Locking**: Applied to `VirtualAccount` during closure requests.

### Idempotency & Inbox/Outbox
- **API Idempotency (Idempotency-Key header)**: All fund movement commands (`InitiatePayoutCommand`, `FundWalletCommand`) require a client-supplied Idempotency Key stored server-side. Duplicate requests with the same key return the original response without re-executing.
- **Webhook Idempotency (Inbox pattern)**: All inbound webhooks (Anchor, Paystack, Moniepoint) are tracked by `(webhookId, consumerId)` via `EventDeliveryTracker`. A webhook that has already been processed is silently discarded.
- **Outbox**: Publishes `PaymentSuccessfulEvent`, `PayoutCompletedEvent`, `BulkPayoutCompletedEvent`. These are written to the Outbox within the same DB transaction as the ledger posting, guaranteeing atomicity.
