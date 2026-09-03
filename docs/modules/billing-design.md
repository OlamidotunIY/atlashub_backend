# Platform Billing Module Design (`atlashub-platform:billing`)

## Role & Purpose

The Billing module is the **subscription engine** of the AtlasHub platform. It manages the commercial relationship between AtlasHub and the organizations that use it. Every time an organization subscribes to a platform product (e.g., Atlas Pay), renews their subscription, or falls behind on payment, this module is the one tracking it.

It answers questions like: *Is this organization currently allowed to use Atlas Commerce? When does their Atlas Pay subscription expire? How much do they owe this billing cycle?*

The Billing module is the **gatekeeper** for access control at the organizational level. When a subscription lapses or is suspended, this module publishes the events that cause other modules (Identity, Pay) to restrict or cut off the organization's access. It does not handle the actual payment processing — that is delegated to the `pay` module. Instead, it handles invoices, subscription state, and the business rules around renewals.

---

## 1. How It Fits Into the Platform

### The Subscription Lifecycle
When an organization subscribes to a product:
1. `billing` reads the applicable `ProductPricing` from `catalog` (matched by `productId`, `billingCycle`, and the org's base currency from `identity`).
2. Creates an `OrganizationProduct` (the active subscription record).
3. Generates the first `BillingInvoice` with the resolved amount and a due date.
4. Publishes `ProductSubscribedEvent` and `BillingInvoiceGeneratedEvent`.

When payment is made (triggered automatically via a webhook from `pay`):
1. `PaymentSuccessfulListener` receives a `PaymentSuccessfulEvent` from `atlashub-pay`.
2. Matches the payment reference to the open invoice.
3. Calls `PayBillingInvoiceUseCase` → marks invoice PAID, renews the subscription period, publishes `SubscriptionRenewedEvent`.

When payment fails or the subscription period ends without payment:
1. The subscription transitions to `PAST_DUE`.
2. After a grace period, `SuspendSubscriptionUseCase` transitions it to `SUSPENDED`.
3. `SubscriptionSuspendedEvent` is published → Identity restricts org access, Pay may freeze wallet operations.

### Access Control Gate
The `GetOrganizationContextQuery` is a special read that returns which products an organization actively has access to. Other modules call this (or listen to subscription events) to determine feature availability. For example, the Commerce module only allows POS sales if the org has an active Commerce subscription.

---

## 2. Domain Entities & Aggregates

**`OrganizationProduct` (Aggregate Root)**
Represents an active subscription to a platform product.
- **Fields**:
  - `id`: Long
  - `organizationId`: Long
  - `productId`: Long (references `HubProduct` in `catalog`)
  - `status`: `SubscriptionStatus` (ACTIVE, SUSPENDED, CANCELED, PAST_DUE)
  - `cycle`: `BillingCycle` (MONTHLY, ANNUALLY)
  - `currentPeriodStart`: ZonedDateTime
  - `currentPeriodEnd`: ZonedDateTime
  - `canceledAt`: ZonedDateTime (nullable)
- **Methods**:
  - `create(...)` — static factory, publishes `ProductSubscribedEvent`
  - `activate(ZonedDateTime start, ZonedDateTime end)` — sets the billing period
  - `suspend(String reason)` — publishes `SubscriptionSuspendedEvent`
  - `cancel(String reason)` — publishes `SubscriptionCanceledEvent`
  - `renew(ZonedDateTime newEnd)` — extends the current period, publishes `SubscriptionRenewedEvent`
  - `isExpired(): Boolean` — checks if `currentPeriodEnd` is in the past

**`BillingInvoice` (Aggregate Root)**
- **Fields**:
  - `id`: Long
  - `organizationProductId`: Long
  - `amount`: `Money` (in the org's base currency, matched from `ProductPricing`)
  - `status`: `InvoiceStatus` (DRAFT, PAID, FAILED, VOID)
  - `dueDate`: ZonedDateTime
  - `paidAt`: ZonedDateTime (nullable)
- **Methods**:
  - `markAsPaid(ZonedDateTime time)` — publishes `BillingInvoicePaidEvent`
  - `markAsFailed()` — transitions to FAILED for retry or escalation

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `ProductSubscribedEvent` | Organization subscribes to a product | `pay:accounts` (ensure wallet is funded), `notifications` (welcome email) |
| `BillingInvoiceGeneratedEvent` | New invoice created | `notifications` (send invoice email), `accounting` (record receivable) |
| `BillingInvoicePaidEvent` | Invoice marked paid | `accounting` (record payment), subscription is auto-renewed |
| `SubscriptionRenewedEvent` | Subscription period extended after payment | `identity` (restore access if was suspended), `notifications` |
| `SubscriptionSuspendedEvent` | Subscription suspended due to non-payment | `identity` (restrict org access), `pay` (optionally freeze wallet) |
| `SubscriptionCanceledEvent` | Subscription canceled by org | `pay:accounts` (begin offboarding), `accounting` (record cancellation) |

---

## 4. Exceptions & Errors

**`BillingErrorCode`** (implements `ErrorCode`):
- `SUBSCRIPTION_ALREADY_EXISTS`
- `SUBSCRIPTION_NOT_FOUND`
- `INVALID_SUBSCRIPTION_STATE`
- `INVOICE_NOT_FOUND`
- `INVOICE_ALREADY_PAID`

---

## 5. Commands & Use Cases

- `SubscribeToProductCommand(Long organizationId, Long productId, BillingCycle cycle)` → `SubscribeToProductUseCase`
  Validates that the product is ACTIVE in `catalog`, checks no existing active subscription, creates `OrganizationProduct` and initial `BillingInvoice`, publishes `ProductSubscribedEvent`.
- `CancelSubscriptionCommand(Long subscriptionId, String reason)` → `CancelSubscriptionUseCase`
  Transitions status to CANCELED, publishes `SubscriptionCanceledEvent`.
- `SuspendSubscriptionCommand(Long subscriptionId, String reason)` → `SuspendSubscriptionUseCase`
- `PayBillingInvoiceCommand(Long invoiceId, String paymentReference)` → `PayBillingInvoiceUseCase`
  Marks invoice as PAID, calls `subscription.renew(newEnd)` to extend the period, publishes `BillingInvoicePaidEvent` and `SubscriptionRenewedEvent`.

---

## 6. Queries

- `GetOrganizationContextQuery(Long organizationId)` → `OrganizationContextResult(List<String> activeProductKeys, Map<String, Object> featureFlags)`
  Critical read used by other modules to gate feature access. Returns which product keys the org currently has ACTIVE subscriptions for.
- `ListSubscriptionsQuery(Long organizationId)` → `List<SubscriptionResult>`
- `ListBillingInvoicesQuery(Long organizationId, Long subscriptionId)` → `List<InvoiceResult>`

---

## 7. Listeners

- **`PaymentSuccessfulListener`**: Listens to `PaymentSuccessfulEvent` (from `atlashub-pay`). Matches the `paymentReference` to an open `BillingInvoice`. On match, dispatches `PayBillingInvoiceCommand` to automatically mark the invoice paid and renew the subscription. Uses Inbox pattern to guarantee exactly-once processing — a duplicate `PaymentSuccessfulEvent` will not double-renew the subscription.

---

## 8. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `OrganizationProduct` to prevent race conditions during concurrent renewals or cancellations (e.g., a scheduled renewal job and a manual cancellation request arriving simultaneously).

### Inbox & Outbox Patterns
- **Outbox**: Reliably publishes `SubscriptionRenewedEvent` so Identity restores org access after payment, even if the event bus is temporarily down.
- **Inbox (`EventDeliveryTracker`)**: Checks incoming `PaymentSuccessfulEvent`s by `(eventId, consumerId)` composite key to guarantee a subscription is renewed **exactly once** per payment, preventing double-renewals from duplicate webhook deliveries.
