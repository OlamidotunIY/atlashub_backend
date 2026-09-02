# Billing Module Design (`atlashub-platform:billing`)

## 1. Domain Entities & Aggregates

**`OrganizationProduct` (Aggregate Root)**
Represents an active subscription to a platform product.
- **Fields**: 
  - `id`: Long
  - `organizationId`: Long
  - `productId`: Long (references `HubProduct` in catalog)
  - `status`: `SubscriptionStatus` (ACTIVE, SUSPENDED, CANCELED, PAST_DUE)
  - `billingCycle`: `BillingCycle` (MONTHLY, ANNUALLY)
  - `currentPeriodStart`: LocalDateTime
  - `currentPeriodEnd`: LocalDateTime
  - `canceledAt`: LocalDateTime
- **Methods**:
  - `activate(LocalDateTime start, LocalDateTime end)`
  - `suspend(String reason)`
  - `cancel(String reason)`
  - `renew(LocalDateTime newEnd)`
  - `isExpired()`: Boolean

**`BillingInvoice` (Entity)**
- **Fields**:
  - `id`: Long
  - `organizationProductId`: Long
  - `amount`: **`Money`** (amount + CurrencyCode matching the Organization's base currency)
  - `status`: `InvoiceStatus` (DRAFT, PAID, FAILED, VOID)
  - `dueDate`: LocalDate
  - `paidAt`: LocalDateTime
- **Methods**:
  - `markAsPaid(LocalDateTime time)`
  - `markAsFailed()`

> **Multi-Currency Note**: When an Organization subscribes, the `BillingInvoice.amount` is set in the Organization's base currency (determined from `User.country` at registration). The `billing` module resolves the correct `ProductPricing` row by matching `(productId, billingCycle, currencyCode)`.

## 2. Domain Events (Wrapped in `EnvelopedDomainEvent`)
- `ProductSubscribedEvent(Long organizationId, Long productId, Long subscriptionId)`
- `SubscriptionRenewedEvent(Long subscriptionId, LocalDateTime newEnd)`
- `SubscriptionSuspendedEvent(Long subscriptionId, String reason)`
- `SubscriptionCanceledEvent(Long subscriptionId, String reason)`
- `BillingInvoiceGeneratedEvent(Long invoiceId, Money amount)`
- `BillingInvoicePaidEvent(Long invoiceId, Long subscriptionId)`

## 3. Exceptions & Errors
**`BillingErrorCode`** (implements `ErrorCode`):
- `SUBSCRIPTION_ALREADY_EXISTS`
- `SUBSCRIPTION_NOT_FOUND`
- `INVALID_SUBSCRIPTION_STATE`
- `INVOICE_NOT_FOUND`
- `INVOICE_ALREADY_PAID`

## 4. Commands & Use Cases
- **Command**: `SubscribeToProductCommand(Long organizationId, Long productId, BillingCycle cycle)`
  - **UseCase**: `SubscribeToProductUseCase` (Validates catalog product, creates `OrganizationProduct`, generates initial `BillingInvoice`, publishes `ProductSubscribedEvent`).
- **Command**: `CancelSubscriptionCommand(Long subscriptionId, String reason)`
  - **UseCase**: `CancelSubscriptionUseCase` (Transitions status to CANCELED, publishes `SubscriptionCanceledEvent`).
- **Command**: `SuspendSubscriptionCommand(Long subscriptionId, String reason)`
  - **UseCase**: `SuspendSubscriptionUseCase`
- **Command**: `PayBillingInvoiceCommand(Long invoiceId, String paymentReference)`
  - **UseCase**: `PayBillingInvoiceUseCase` (Marks invoice as PAID, triggers `SubscriptionRenewedEvent`).

## 5. Queries
- **Query**: `GetOrganizationContextQuery(Long organizationId)`
  - **Returns**: `OrganizationContextResult(List<String> activeProductKeys, Map<String, Object> featureFlags)`
- **Query**: `ListSubscriptionsQuery(Long organizationId)`
  - **Returns**: `List<SubscriptionResult>`
- **Query**: `ListBillingInvoicesQuery(Long organizationId, Long subscriptionId)`
  - **Returns**: `List<InvoiceResult>`

## 6. Listeners
- `PaymentSuccessfulListener`: Listens to `PaymentSuccessfulEvent` (from Pay module) matching a billing reference. Dispatches `PayBillingInvoiceCommand` to automate renewals.

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Optimistic Locking (@Version)**: Applied to OrganizationProduct to prevent race conditions during concurrent renewals or cancellations.

### Inbox & Outbox Patterns
- **Outbox**: Used to reliably publish SubscriptionRenewedEvent.
- **Inbox (EventDeliveryTracker)**: Checks incoming PaymentSuccessfulEvents to ensure a subscription is only renewed once per payment, guaranteeing Exactly-Once processing.
