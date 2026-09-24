# Pay Module Design (`atlashub-pay`)

## Role & Purpose

The Pay module is the **financial rails** of the AtlasHub platform. Every movement of money — whether a customer paying for a POS sale, a payroll disbursement to employees, an inter-outlet float transfer, an organization paying their AtlasHub subscription, or a vendor receiving their marketplace split — flows through this module's infrastructure.

No other module holds, moves, or accounts for real money independently. They all delegate money movement to `atlashub-pay`.

Pay is also a **B2B2C module** — organizations that subscribe to Atlas Pay can expose the payment infrastructure to their own end-customers. A merchant's e-commerce website can call the AtlasHub Pay API to collect payments from shoppers, using the merchant's API key and HMAC authentication.

Internally, Pay is split into specialized submodules:
- **`accounts`** — Virtual bank accounts (NUBANs) issued per organization via Anchor. Also per-customer dedicated NUBANs for B2B2C collection.
- **`ledger`** — Double-entry accounting ledger tracking all internal money movements in real time
- **`charges`** — Inbound payments via Paystack (card, bank transfer, USSD) and Moniepoint (physical POS terminals)
- **`transfers`** — Outbound payouts to bank accounts (salaries, supplier payments, refunds, marketplace vendor disbursements)
- **`splits`** — Configurable rules for distributing incoming payments among multiple recipients (AtlasHub fee + business share + vendor share)
- **`mandates`** — Recurring payment authorizations from end-customers (for subscription services offered BY organizations)
- **`settlement`** — Tracking settlement batches from Paystack/Moniepoint back to the organization's bank
- **`tx-query`** — Unified read model for querying the full transaction history across all submodules
- **`webhooks`** — Outbound webhook delivery to merchant servers when payment events occur

---

## 1. How External Platforms Integrate

### Anchor (Virtual Account Issuance)
When an organization's KYC is approved:
1. `OrganizationComplianceApprovedListener` calls `IssueVirtualAccountUseCase`
2. Calls Anchor's API via `AnchorVirtualAccountAdapter` to create a virtual account linked to AtlasHub's pool account
3. Anchor issues a NUBAN and sends a webhook confirming activation
4. `ActivateVirtualAccountUseCase` assigns the NUBAN, activates the `VirtualAccount`, publishes `VirtualAccountActivatedEvent`

When someone makes a bank transfer to that NUBAN:
- Anchor sends a `collection` webhook to AtlasHub
- Webhook adapter calls `CreditWalletFromTransferUseCase` → credits the org's **Operating Account** in the ledger → publishes `WalletFundedEvent`

### Per-Customer Virtual Accounts (B2B2C)
For organizations that need dedicated collection accounts per customer (e.g., a platform that wants each of its users to have their own NUBAN for credit top-ups):
- Organization calls `IssueCustomerVirtualAccountUseCase` with their customer's details
- Anchor issues a customer-specific NUBAN
- Collections to this NUBAN are automatically credited to the organization's ledger with the customer reference attached

### Paystack (Card, Bank Transfer, USSD)
1. Merchant or internal module calls `InitializeChargeUseCase`
2. Pay calls Paystack API → gets `checkoutUrl` or `ussdCode`
3. Customer completes payment externally
4. Paystack sends webhook to AtlasHub's webhook endpoint
5. `ProcessPaystackWebhookUseCase` validates HMAC signature, finds the `Charge`, calls `markSuccessful()` or `markFailed()`
6. On success: posts ledger entry + publishes `ChargeSuccessfulEvent` → consumed by Commerce, Billing, and the org's outbound webhook

### Moniepoint (Physical POS Terminals)
Same path as Paystack, via `ProcessMoniepointWebhookUseCase`. Moniepoint terminal transactions arrive via webhook.

### The Shadow Ledger Principle
AtlasHub's internal pay ledger is a **shadow of real money held at Anchor/Paystack**. Every real money event (Anchor confirms a deposit, Paystack confirms a collection, a payout settles) produces a corresponding `LedgerTransaction` in the internal double-entry ledger. This gives AtlasHub a real-time, independently auditable record of every organization's balance — without relying solely on external provider APIs.

---

## 2. Internal Account Structure

Every organization gets these ledger accounts bootstrapped when their compliance is approved:

| Account | Type | Purpose |
|---|---|---|
| **Operating Account** | Asset | Day-to-day inflows and outflows |
| **Payroll Reserve Account** | Asset | Funds locked for upcoming payroll disbursement |
| **Tax Holding Account** | Liability | VAT, PAYE collected but not yet remitted |
| **Escrow Account** | Asset | Funds held pending delivery confirmation (marketplace transactions) |
| **Suspense Account** | Asset | Inter-outlet transfer clearing (in-transit) |
| **Till Accounts** (per outlet) | Asset | Cash at each POS till |
| **Split Holding Account** | Asset | Funds received that are pending split distribution to vendors |

---

## 3. The Double-Entry Ledger (`pay:ledger`)

The ledger enforces double-entry bookkeeping: every `LedgerTransaction` must contain at least one DEBIT entry and one CREDIT entry, and the sum of all DEBITs must equal the sum of all CREDITs. If they don't balance, the transaction is rejected at domain construction time.

### Key Domain Models

**`LedgerAccount` (Aggregate Root)**
```
LedgerAccount
├── id: Long
├── organizationId: Long
├── accountType: LedgerAccountType    ← OPERATING, PAYROLL_RESERVE, TAX_HOLDING, ESCROW, SUSPENSE, TILL, SPLIT_HOLDING
├── outletId: Long                    ← nullable — only for TILL accounts
├── currency: Currency
├── status: LedgerAccountStatus       ← ACTIVE, FROZEN, CLOSED
└── createdAt: ZonedDateTime
```

**`LedgerTransaction` (Aggregate Root)**
```
LedgerTransaction
├── id: Long
├── organizationId: Long
├── entries: List<LedgerEntry>        ← at least 2: one DEBIT, one CREDIT
├── sourceSystem: SourceSystem        ← COMMERCE_CHECKOUT, PAYROLL, PLATFORM_BILLING, etc.
├── sourceReferenceId: String         ← ID of the originating business entity
├── description: String
├── currency: Currency
├── postedAt: ZonedDateTime
└── reference: String                 ← unique idempotency reference
```

**Domain Rule**: At construction, `validateBalance()` is called. If ∑DEBIT ≠ ∑CREDIT, `UNBALANCED_LEDGER_TRANSACTION` exception is thrown. The ledger will never contain an unbalanced transaction.

**`LedgerEntry` (Entity — immutable)**
```
LedgerEntry
├── id: Long
├── transactionId: Long
├── accountId: Long
├── type: EntryType              ← DEBIT | CREDIT
├── amount: Money
└── runningBalance: Money        ← balance of this account AFTER this entry
```

**`BalanceSnapshot` (Entity)**
Periodically computed balance per account. Balance queries use `latest snapshot + entries since snapshot` to avoid full table scans.

**`SourceSystem` (Enum)**
```
COMMERCE_CHECKOUT       ← POS sale completed
COMMERCE_REFUND         ← Customer refund
PLATFORM_BILLING        ← Organization pays their AtlasHub invoice
PAYROLL                 ← Payroll disbursement
LOAN_DISBURSEMENT       ← Employee loan approved
INTER_OUTLET_TRANSFER   ← Stock/cash between outlets
CASH_BANKING            ← Till cash deposited to bank
EXTERNAL_COLLECTION     ← Bank transfer to NUBAN (Anchor)
CARD_CHARGE             ← Card/USSD/POS payment (Paystack/Moniepoint)
PAYOUT                  ← Outbound bank transfer
SETTLEMENT              ← Paystack/Moniepoint settles to org bank
SPLIT                   ← Revenue split to vendor/sub-merchant
ESCROW_RELEASE          ← Escrow released on delivery confirmation
ESCROW_REFUND           ← Escrow refunded on delivery failure
MANUAL                  ← Admin-initiated manual entry
SYSTEM                  ← Auto-generated (fee calculation, etc.)
```

### Ledger Transaction Examples

**POS Card Sale**:
```
DEBIT  Escrow Account              ₦25,000   [payment held during delivery]
CREDIT Operating Account           ₦25,000   [net revenue after fee]
SourceSystem: COMMERCE_CHECKOUT | ref: order-1042
```

**Marketplace Split (AtlasHub fee 1.5%, Business 70%, Vendor 28.5%)**:
```
Transaction: ₦100,000 received
DEBIT  Operating Account (Org)     ₦70,000   [business share]
DEBIT  Split Holding Account       ₦28,500   [vendor share, pending disbursement]
DEBIT  AtlasHub Fee (Platform)     ₦1,500    [AtlasHub's transaction fee]
CREDIT Escrow Account              ₦100,000  [cleared from escrow on delivery]
SourceSystem: SPLIT | ref: order-1042
```

**Payroll Disbursement**:
```
DEBIT  Payroll Reserve Account     ₦2,500,000
CREDIT Payout Clearing Account     ₦2,500,000
SourceSystem: PAYROLL | ref: payrollRun-23
```

---

## 4. Virtual Accounts (`pay:accounts`)

### `VirtualAccount` (Aggregate Root)

```
VirtualAccount
├── id: Long
├── organizationId: Long
├── ownerType: OwnerType           ← ORGANIZATION | CUSTOMER
├── customerId: String             ← nullable, only for CUSTOMER-owned accounts
├── accountName: String
├── bankName: String
├── nuban: String                  ← actual bank account number, nullable until activated
├── bankCode: String
├── anchorAccountId: String        ← Anchor's internal reference
├── currency: Currency
├── status: VirtualAccountStatus   ← PENDING_ISSUANCE, ACTIVE, SUSPENDED, CLOSED
├── createdAt: ZonedDateTime
└── activatedAt: ZonedDateTime     ← nullable
```

**Business Methods:**
- `activate(String nuban, String bankName)` → registers `VirtualAccountActivatedEvent`
- `suspend()` → registers `VirtualAccountSuspendedEvent`
- `close()` → registers `VirtualAccountClosedEvent`

---

## 5. Inbound Payments (`pay:charges`)

### `Charge` (Aggregate Root)

```
Charge
├── id: Long
├── organizationId: Long
├── customerId: String             ← nullable (set for B2B2C payments)
├── amount: Money
├── channel: PaymentChannel        ← CARD, BANK_TRANSFER, USSD, POS_TERMINAL
├── status: ChargeStatus           ← PENDING, SUCCESSFUL, FAILED, REFUNDED
├── reference: String              ← unique, used to correlate gateway webhook
├── provider: PaymentProvider      ← PAYSTACK, MONIEPOINT
├── checkoutUrl: String            ← nullable (for redirect-based payments)
├── gatewayReference: String       ← provider's own transaction reference
├── gatewayResponse: String        ← raw provider response, nullable
├── splitId: Long                  ← nullable — which split rule to apply
├── sourceSystem: SourceSystem
├── sourceReferenceId: String      ← e.g., salesOrderId, billingInvoiceId
├── metadata: Map<String, String>  ← arbitrary key-value for merchant use
├── createdAt: ZonedDateTime
└── completedAt: ZonedDateTime     ← nullable
```

**Business Methods:**
- `markSuccessful(String gatewayRef, String gatewayResponse)` → registers `ChargeSuccessfulEvent`
- `markFailed(String reason)` → registers `ChargeFailedEvent`
- `refund(Money amount)` → validates amount ≤ original, registers `ChargeRefundInitiatedEvent`

---

## 6. Outbound Transfers (`pay:transfers`)

### `Payout` (Aggregate Root)

Every outbound bank transfer is a `Payout`. The `sourceSystem` and `sourceReferenceId` fields link every payout to its originating business event.

```
Payout
├── id: Long
├── organizationId: Long
├── amount: Money
├── recipientBankCode: String
├── recipientAccountNumber: String
├── recipientAccountName: String
├── narration: String
├── status: PayoutStatus           ← PENDING_APPROVAL, APPROVED, PROCESSING, SUCCESSFUL, FAILED
├── reference: String
├── provider: PaymentProvider      ← PAYSTACK, MONIEPOINT
├── providerReference: String      ← nullable
├── sourceSystem: SourceSystem     ← PAYROLL, REFUND, SUPPLIER_PAYMENT, MANUAL, LOAN_DISBURSEMENT, VENDOR_DISBURSEMENT
├── sourceReferenceId: String
├── initiatedBy: Long              ← userId (maker)
├── approvedBy: Long               ← userId (checker), nullable
├── approvedAt: ZonedDateTime      ← nullable
├── createdAt: ZonedDateTime
└── completedAt: ZonedDateTime     ← nullable
```

**Business Methods:**
- `approve(Long approverId)` → transitions PENDING_APPROVAL → APPROVED → registers `PayoutApprovedEvent`
- `markProcessing()` → APPROVED → PROCESSING
- `complete(String providerRef)` → PROCESSING → SUCCESSFUL → registers `PayoutCompletedEvent`
- `fail(String reason)` → registers `PayoutFailedEvent`

**Maker-Checker Rule:**
All payouts above ₦100,000 (configurable per org) require a second authorized user with `pay:transfers:approve` permission to approve before execution.

---

## 7. Revenue Splits (`pay:splits`)

### `SplitRule` (Aggregate Root)

Defines how incoming payments are distributed.

```
SplitRule
├── id: Long
├── organizationId: Long
├── name: String
├── type: SplitType                ← PERCENTAGE | FLAT
├── platformFeePercentage: BigDecimal  ← AtlasHub's cut (always applied first)
├── subaccounts: List<SplitSubaccount>
└── isActive: Boolean
```

**`SplitSubaccount` (Entity)**
```
SplitSubaccount
├── id: Long
├── splitRuleId: Long
├── recipientType: RecipientType   ← ORGANIZATION | VENDOR | EXTERNAL_BANK_ACCOUNT
├── recipientId: String            ← orgId, vendorId, or bank account reference
├── share: BigDecimal              ← percentage or flat amount
└── description: String
```

**How Splits Work at Payment Time:**

When `InitializeChargeUseCase` is called with a `splitRuleId`:
1. AtlasHub fee is deducted first (e.g., 1.5%)
2. Remaining amount is split per the `SplitRule`
3. For each subaccount recipient, a `LedgerTransaction` is posted crediting the appropriate account
4. Vendor disbursements are queued as `Payout` records (scheduled or immediate, per org configuration)

---

## 8. Recurring Payment Mandates (`pay:mandates`)

> **Note**: This submodule manages recurring card authorizations from END-CUSTOMERS of organizations. Example: a fitness studio that uses AtlasHub to charge its gym members every month. This is NOT related to billing subscriptions (which is an org paying AtlasHub).

### `PaymentMandate` (Aggregate Root)

```
PaymentMandate
├── id: Long
├── organizationId: Long
├── customerId: String
├── email: EmailAddress
├── amount: Money
├── frequency: MandateFrequency    ← DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUALLY
├── authorizationCode: String      ← Paystack's tokenized card authorization
├── status: MandateStatus          ← ACTIVE, PAUSED, REVOKED, EXPIRED
├── nextChargeDate: LocalDate
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `pause()`, `resume()`, `revoke()`, `advanceNextChargeDate()`

---

## 9. Settlement (`pay:settlement`)

### `Settlement` (Aggregate Root)

Tracks batches of funds settled by Paystack/Moniepoint to the organization's linked bank account.

```
Settlement
├── id: Long
├── organizationId: Long
├── provider: PaymentProvider
├── providerSettlementId: String
├── amount: Money
├── settledAt: ZonedDateTime
├── status: SettlementStatus       ← PENDING, CONFIRMED, DISPUTED
└── description: String
```

**Business Methods:**
- `confirm()` → posts ledger entry: DEBIT Bank Account, CREDIT Operating Account → registers `SettlementConfirmedEvent`
- `dispute(String reason)` → registers `SettlementDisputedEvent`

---

## 10. Outbound Webhooks (`pay:webhooks`)

When payment events occur, AtlasHub notifies the organization's registered webhook endpoint.

### `WebhookSubscription` (Aggregate Root)

```
WebhookSubscription
├── id: Long
├── organizationId: Long
├── url: String                    ← HTTPS endpoint on the merchant's server
├── events: Set<WebhookEventType>  ← which events to receive
├── secretKey: String              ← merchant stores this to verify incoming webhooks
├── status: WebhookStatus          ← ACTIVE, DISABLED
├── createdAt: ZonedDateTime
└── lastDeliveryAt: ZonedDateTime  ← nullable
```

### `WebhookDelivery` (Entity)

```
WebhookDelivery
├── id: Long
├── subscriptionId: Long
├── eventType: WebhookEventType
├── payload: String                ← JSON payload
├── signature: String              ← HMAC-SHA256 of payload using merchant's secretKey
├── httpStatus: Integer            ← nullable, set after delivery attempt
├── status: DeliveryStatus         ← PENDING, DELIVERED, FAILED, RETRYING
├── attemptCount: Integer
├── nextRetryAt: ZonedDateTime     ← nullable, exponential backoff
└── createdAt: ZonedDateTime
```

**Webhook Events:**
```
charge.successful
charge.failed
charge.refunded
payout.completed
payout.failed
mandate.charged
mandate.revoked
settlement.confirmed
```

**Delivery Guarantees:**
- Minimum once delivery with exponential backoff (1s, 2s, 4s, 8s, 16s, max 5 retries)
- After 5 failed attempts, delivery is marked FAILED and the org is notified
- Merchant verifies authenticity by checking `X-AtlasHub-Signature` header (HMAC-SHA256 of the payload body)

---

## 11. Transaction History (`pay:tx-query`)

A dedicated **read model** aggregating entries from all Pay submodules into a unified, queryable history. No writes happen here — it is populated by consuming events.

**Queries:**
- `ListTransactionsQuery(orgId, type, status, dateFrom, dateTo, channel, page)` → `Page<TransactionResult>`
- `GetTransactionDetailsQuery(transactionId)` → `TransactionDetailsResult`
- `GetWalletBalanceQuery(orgId, accountType)` → `Money`
- `GetWalletBalancesQuery(orgId)` → `Map<LedgerAccountType, Money>` ← dashboard wallet overview
- `ListPayoutsQuery(orgId, status, sourceSystem, page)` → `Page<PayoutResult>`
- `GetChargeByReferenceQuery(reference)` → `ChargeResult`
- `GetTransactionVolumeQuery(orgId, month)` → `TransactionVolumeResult` ← for billing fee tier calculation

---

## 12. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `VirtualAccountActivatedEvent` | NUBAN activated | `notifications`, `accounting` |
| `ChargeSuccessfulEvent` | Customer payment confirmed | `commerce` (complete sale), `billing` (mark invoice paid), `ledger` (post entry), `webhooks` (notify merchant) |
| `ChargeFailedEvent` | Payment failed/declined | `commerce` (release reserved stock), `notifications`, `webhooks` |
| `ChargeRefundInitiatedEvent` | Refund initiated | `transfers` (create payout), `accounting` |
| `PayoutCompletedEvent` | Bank transfer confirmed | `hr` (mark salary disbursed), `accounting`, `notifications`, `webhooks` |
| `PayoutFailedEvent` | Bank transfer failed | `hr` (revert payroll), `notifications`, `webhooks` |
| `WalletFundedEvent` | Operating account credited (NUBAN transfer) | `accounting`, `notifications` |
| `SettlementConfirmedEvent` | Paystack/Moniepoint settles funds | `accounting` |
| `BulkPayoutCompletedEvent` | All payroll payouts processed | `hr` (mark payroll DISBURSED) |
| `BulkPayoutFailedEvent` | One or more payroll payouts failed | `hr` (revert payroll run to APPROVED) |
| `LedgerTransactionPostedEvent` | Any ledger entry posted | `accounting` (bridge listener posts GL journal entry) |

---

## 13. Exceptions & Errors

**`PayErrorCode`**:
- `INSUFFICIENT_FUNDS`, `WALLET_NOT_FOUND`, `ACCOUNT_NOT_FOUND`, `ACCOUNT_FROZEN`
- `CHARGE_NOT_FOUND`, `DUPLICATE_REFERENCE`
- `PAYOUT_NOT_FOUND`, `INVALID_BANK_DETAILS`, `PAYOUT_APPROVAL_REQUIRED`
- `UNBALANCED_LEDGER_TRANSACTION`, `CURRENCY_MISMATCH`
- `WEBHOOK_SIGNATURE_INVALID`, `WEBHOOK_ALREADY_PROCESSED`
- `SPLIT_RULE_NOT_FOUND`, `INVALID_SPLIT_PERCENTAGES` — percentages don't sum to 100
- `SETTLEMENT_NOT_FOUND`
- `MANDATE_NOT_FOUND`, `MANDATE_REVOKED`
- `VIRTUAL_ACCOUNT_NOT_FOUND`, `VIRTUAL_ACCOUNT_INACTIVE`

---

## 14. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Pessimistic Locking (`PESSIMISTIC_WRITE`)**: Applied to `LedgerAccount` during `PostLedgerTransactionUseCase`. All affected accounts are locked in ascending `accountId` order to prevent deadlocks. This is non-negotiable — concurrent ledger posts without this lock will corrupt balance snapshots.
- **Pessimistic Locking**: Applied to `Payout` during `ApprovePayout` to prevent double approval.
- **Optimistic Locking**: Applied to `Charge`, `VirtualAccount`, `SplitRule`, `PaymentMandate`.

### Idempotency & Outbox/Inbox
- **API Idempotency (Idempotency-Key header)**: All mutation commands (charge, payout, ledger post) require a client-supplied Idempotency Key. Duplicate requests with the same key return the original result without re-executing.
- **Webhook Idempotency (Inbox pattern)**: All inbound webhooks (Anchor, Paystack, Moniepoint) are tracked by `(webhookId, consumerId)` using `EventDeliveryTracker`. Duplicate deliveries are silently discarded.
- **Outbox**: `ChargeSuccessfulEvent`, `PayoutCompletedEvent`, `BulkPayoutCompletedEvent`, `LedgerTransactionPostedEvent` — written to the outbox in the same transaction as the state change. These events drive financial movements downstream and must never be lost.
