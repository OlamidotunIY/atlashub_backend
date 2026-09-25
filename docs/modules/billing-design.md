# Billing Module Design (`atlashub-platform:billing`)

## Role & Purpose

The `billing` module manages the **commercial relationship between AtlasHub and its business customers**. It handles subscriptions, invoices, entitlements, and access revocation. It is the revenue engine of the AtlasHub platform itself.

The billing module answers:
- *Is organization X subscribed to Atlas Pay?* → enforced as an entitlement check
- *How much does organization X owe AtlasHub this month?* → calculated and invoiced
- *Has organization X paid their invoice?* → tracked by payment events from `pay`

The billing module does **not** process payments itself — it delegates payment collection to `atlashub-pay` (the organization pays their AtlasHub invoice through the same payment infrastructure they use for their business).

---

## 1. Business Model Summary

| Product | Pricing Model | How Invoice Generated |
|---|---|---|
| Atlas Pay | Transaction fee (tiered by volume) | Monthly invoice calculated from actual transaction volume |
| Atlas Commerce | Flat monthly subscription | Invoice generated at the start of each billing period |
| Atlas Logistics | Flat monthly subscription | Invoice generated at the start of each billing period |
| Atlas HR | Free | No invoice generated |
| Atlas Accounting | Flat monthly subscription | Invoice generated at the start of each billing period |

---

## 2. Features

### Subscription Management
Organizations subscribe to one or more AtlasHub products. Each subscription tracks:
- Which product + pricing plan they are on
- The current billing period (start + end dates)
- The subscription status

### Invoice Generation
At the start of each billing period (for subscriptions) or end of period (for transaction-fee products), `GenerateInvoiceUseCase` creates a `BillingInvoice`. The invoice is sent to the organization via `notifications`.

### Payment Collection
Organizations pay their AtlasHub invoices by:
1. Receiving the invoice (via email)
2. Clicking the payment link → redirected to a Paystack checkout initialized by `pay`
3. On payment success, `PaymentSuccessfulEvent` is received → `MarkInvoicePaidUseCase` closes the invoice

### Entitlement Checking
Other modules verify that an org has an active subscription before granting access. They call `EntitlementQueryPort.hasActiveSubscription(orgId, "ATLAS_COMMERCE")`. This is a synchronous check — no Kafka involved.

### Access Suspension & Restoration
- If an invoice is overdue beyond the grace period (7 days), `SuspendSubscriptionUseCase` suspends the subscription → `SubscriptionSuspendedEvent` → `iam` suspends non-owner member access
- When the overdue invoice is paid, `RestoreSubscriptionUseCase` restores access → `SubscriptionRestoredEvent`

### Maker-Checker for Large Invoices
If a transaction-fee invoice exceeds a configured threshold (₦1M), it requires a secondary AtlasHub admin approval before being dispatched to the organization.

---

## 3. Domain Entities & Aggregates

### `Subscription` (Aggregate Root)

```
Subscription
├── id: Long
├── organizationId: Long
├── productCode: String              ← "ATLAS_PAY", "ATLAS_COMMERCE", etc.
├── pricingPlanId: Long              ← nullable for FREE and TRANSACTION_FEE products
├── status: SubscriptionStatus       ← PENDING_PAYMENT, ACTIVE, SUSPENDED, CANCELLED, EXPIRED
├── currentPeriodStart: LocalDate
├── currentPeriodEnd: LocalDate
├── nextBillingDate: LocalDate
├── billingCurrency: Currency        ← determined from org's baseCurrency
├── trialEndsAt: LocalDate           ← nullable
├── cancelledAt: ZonedDateTime       ← nullable
├── cancelReason: String             ← nullable
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `activate(LocalDate periodStart, LocalDate periodEnd)` → sets ACTIVE, sets billing dates → registers `SubscriptionActivatedEvent`
- `suspend(String reason)` → sets SUSPENDED → registers `SubscriptionSuspendedEvent`
- `restore()` → sets ACTIVE → registers `SubscriptionRestoredEvent`
- `cancel(String reason, ZonedDateTime when)` → sets CANCELLED → registers `SubscriptionCancelledEvent`
- `renew(LocalDate newPeriodStart, LocalDate newPeriodEnd)` → advances billing period → registers `SubscriptionRenewedEvent`

**Domain Rules:**
- A FREE product (Atlas HR) is immediately ACTIVE on creation, never generates an invoice
- A subscription can only be suspended if it has at least one overdue unpaid invoice
- Cancellation takes effect at the end of the current billing period — the org retains access until `currentPeriodEnd`

---

### `BillingInvoice` (Aggregate Root)

```
BillingInvoice
├── id: Long
├── organizationId: Long
├── subscriptionId: Long
├── invoiceNumber: String          ← unique, e.g., "INV-2026-09-00123"
├── productCode: String
├── periodStart: LocalDate
├── periodEnd: LocalDate
├── lineItems: List<InvoiceLineItem>
├── subtotal: Money
├── tax: Money                     ← VAT where applicable
├── totalAmount: Money
├── currency: Currency
├── status: InvoiceStatus          ← DRAFT, ISSUED, PAID, OVERDUE, VOID
├── dueDate: LocalDate             ← 7 days from issue for subscription invoices
├── issuedAt: ZonedDateTime        ← nullable
├── paidAt: ZonedDateTime          ← nullable
├── paymentReference: String       ← pay module's payment reference, nullable
├── paymentCheckoutUrl: String     ← Paystack checkout URL, nullable
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `issue()` → transitions DRAFT → ISSUED → registers `InvoiceIssuedEvent`
- `markPaid(String paymentRef, ZonedDateTime paidAt)` → transitions → PAID → registers `InvoicePaidEvent`
- `markOverdue()` → called by scheduler → registers `InvoiceOverdueEvent`
- `void(String reason)` → for corrections/cancellations → registers `InvoiceVoidedEvent`

---

### `InvoiceLineItem` (Entity)

```
InvoiceLineItem
├── id: Long
├── invoiceId: Long
├── description: String            ← e.g., "Atlas Commerce — Starter Plan (Sep 2026)"
├── quantity: Integer              ← 1 for subscriptions, transaction count for fee invoices
├── unitPrice: Money
├── totalAmount: Money
└── lineItemType: LineItemType     ← SUBSCRIPTION, TRANSACTION_FEE, TAX, DISCOUNT, CREDIT
```

---

### `TransactionVolumeLedger` (Aggregate Root)

For `ATLAS_PAY`, billing needs to know the total monthly transaction volume to determine the fee tier. This is a billing-owned read model, updated by consuming `PaymentSuccessfulEvent` from `pay`.

```
TransactionVolumeLedger
├── id: Long
├── organizationId: Long
├── month: YearMonth               ← e.g., "2026-09"
├── totalVolume: Money             ← cumulative transaction volume in the org's billing currency
├── totalTransactionCount: Long
└── lastUpdatedAt: ZonedDateTime
```

**Domain Logic:**
- `recordTransaction(Money amount)` → increments `totalVolume` and `totalTransactionCount`
- `deriveTier(FeeStructure feeStructure): FeeTier` → returns the applicable tier for the current volume

---

## 4. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `SubscriptionCreatedEvent` | Org subscribes to a product | `notifications` (confirmation email) |
| `SubscriptionActivatedEvent` | Payment received, subscription goes ACTIVE | `iam` (grant product entitlements), `notifications` |
| `SubscriptionSuspendedEvent` | Invoice overdue past grace period | `iam` (suspend non-owner member access), `notifications` |
| `SubscriptionRestoredEvent` | Overdue invoice paid | `iam` (restore member access), `notifications` |
| `SubscriptionRenewedEvent` | Billing period renewed | `notifications` (renewal confirmation) |
| `SubscriptionCancelledEvent` | Org cancels subscription | `iam` (schedule access removal at period end), `notifications` |
| `InvoiceIssuedEvent` | Invoice sent to org | `notifications` (email with payment link), `pay` (initialize Paystack checkout) |
| `InvoicePaidEvent` | Invoice marked paid | `subscription` (activate/renew subscription) |
| `InvoiceOverdueEvent` | Due date passed without payment | `notifications` (reminder), triggers suspension after grace period |

---

## 5. Outbound Port (Open Host Service)

```java
// In atlashub-shared
public interface EntitlementQueryPort {
    boolean hasActiveSubscription(Long orgId, String productCode);
    SubscriptionStatus getSubscriptionStatus(Long orgId, String productCode);
    boolean isSubscriptionSuspended(Long orgId);
}
```

---

## 6. Exceptions & Errors

**`BillingErrorCode`**:
- `SUBSCRIPTION_NOT_FOUND`, `INVOICE_NOT_FOUND`
- `SUBSCRIPTION_ALREADY_ACTIVE`, `SUBSCRIPTION_SUSPENDED`, `SUBSCRIPTION_CANCELLED`
- `INVOICE_ALREADY_PAID`, `INVOICE_VOIDED`
- `PRODUCT_NOT_AVAILABLE` — product is deactivated in catalog
- `INVALID_INVOICE_STATE`

---

## 7. Commands & Use Cases

### Subscriptions
- `SubscribeCommand(orgId, productCode, pricingPlanId)` → `SubscribeUseCase`
  - Creates subscription in PENDING_PAYMENT state (or ACTIVE immediately for FREE products)
  - For paid products, triggers invoice generation
- `ActivateSubscriptionCommand(subscriptionId)` → `ActivateSubscriptionUseCase` ← called when invoice paid
- `SuspendSubscriptionCommand(subscriptionId, reason)` → `SuspendSubscriptionUseCase` ← called by overdue scheduler
- `RestoreSubscriptionCommand(subscriptionId)` → `RestoreSubscriptionUseCase` ← called when overdue invoice paid
- `CancelSubscriptionCommand(subscriptionId, reason)` → `CancelSubscriptionUseCase`
- `RenewSubscriptionCommand(subscriptionId)` → `RenewSubscriptionUseCase` ← scheduled job, called at billing period end

### Invoices
- `GenerateSubscriptionInvoiceCommand(subscriptionId, period)` → `GenerateSubscriptionInvoiceUseCase`
- `GenerateTransactionFeeInvoiceCommand(orgId, month)` → `GenerateTransactionFeeInvoiceUseCase`
  - Reads `TransactionVolumeLedger` → determines tier → calculates fee → creates invoice
- `MarkInvoicePaidCommand(invoiceId, paymentReference, paidAt)` → `MarkInvoicePaidUseCase`
- `VoidInvoiceCommand(invoiceId, reason)` → `VoidInvoiceUseCase` ← admin only
- `MarkInvoiceOverdueCommand(invoiceId)` → `MarkInvoiceOverdueUseCase` ← scheduler

---

## 8. Queries

- `GetSubscriptionQuery(orgId, productCode)` → `SubscriptionResult`
- `ListSubscriptionsQuery(orgId)` → `List<SubscriptionResult>`
- `ListInvoicesQuery(orgId, status, dateFrom, dateTo)` → `List<InvoiceSummaryResult>`
- `GetInvoiceDetailsQuery(invoiceId)` → `InvoiceDetailsResult`
- `GetCurrentBillingPeriodQuery(subscriptionId)` → `BillingPeriodResult`
- `GetTransactionVolumeQuery(orgId, month)` → `TransactionVolumeResult`

---

## 9. Listeners

- **`OrganizationComplianceApprovedListener`**: Listens to `OrganizationComplianceApprovedEvent` from `compliance`. Activates any PENDING_PAYMENT subscriptions that were blocked on compliance approval.
- **`PaymentSuccessfulListener`**: Listens to `PaymentSuccessfulEvent` from `pay`. If the payment reference matches a billing invoice, calls `MarkInvoicePaidUseCase`. Also calls `TransactionVolumeLedger.recordTransaction()` for Pay fee calculation.
- **`SubscriptionCancelledListener`** (internal): Schedules member access removal for the end of the current billing period.

---

## 10. Distributed Architecture

### Scheduled Jobs
- **Invoice Generation Job** (runs on 1st of every month, 00:05 AM): For all ACTIVE subscriptions with `nextBillingDate = today`, generates invoices.
- **Overdue Check Job** (runs daily, 08:00 AM): For all ISSUED invoices with `dueDate < today`, calls `MarkInvoiceOverdueUseCase`.
- **Suspension Job** (runs daily, 08:05 AM): For all OVERDUE invoices older than 7 days (grace period), calls `SuspendSubscriptionUseCase`.

### Locking
- **Optimistic Locking**: `Subscription`, `BillingInvoice`
- **Pessimistic Locking**: `TransactionVolumeLedger` during `recordTransaction()` — prevents concurrent updates corrupting the running total

### Outbox & Inbox
- **Outbox**: `SubscriptionSuspendedEvent`, `InvoiceIssuedEvent` — both trigger downstream actions that must be delivered reliably
- **Inbox**: `PaymentSuccessfulEvent` — idempotent. A duplicate delivery of this event must not mark the same invoice paid twice, which would attempt to activate an already-active subscription.
