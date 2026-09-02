# Platform Catalog Module Design (`atlashub-platform:catalog`)

## Role & Purpose

The Catalog module is AtlasHub's **internal product menu**. It defines the platform products that organizations can subscribe to — Atlas Pay, Atlas Commerce, Atlas Logistics, Atlas HR, Atlas Accounting — and the pricing for each. It is exclusively managed by AtlasHub administrators and serves as the authoritative source of truth for what is available on the platform and at what cost.

When an organization wants to start using Atlas Pay, they are subscribing to a `HubProduct` defined in this catalog. When an admin wants to change the monthly price for Atlas Commerce, they do it here. The `billing` module reads from the catalog at subscription time to determine the correct invoice amount for an organization based on their country (currency) and chosen billing cycle.

This module has **no awareness of individual organizations** — it only knows about products and their pricing tiers. It is intentionally simple and stable.

---

## 1. Features

### Platform Product Management
AtlasHub staff (via the `admin` module) define each platform product as a `HubProduct` with a unique `ProductKey` (e.g., `PAY`, `COMMERCE`, `LOGISTICS`, `HR`, `ACCOUNTING`). Products have a lifecycle status (ACTIVE, INACTIVE, DEPRECATED). Deprecated products are no longer available for new subscriptions but remain accessible to existing subscribers.

### Multi-Currency, Multi-Cycle Pricing
Each `HubProduct` can have multiple `ProductPricing` entries — one per `(BillingCycle, CurrencyCode)` combination. This means:
- A Nigerian organization (NGN) subscribing monthly sees: ₦50,000/month
- A Kenyan organization (KES) subscribing annually sees: KES 480,000/year
- The same product can be priced differently per market.

At subscription time, the `billing` module resolves the correct pricing row by matching `(productId, billingCycle, currencyCode)` against the organization's base currency.

---

## 2. Domain Entities & Aggregates

**`HubProduct` (Aggregate Root)**
Represents a standalone platform product offering (e.g., "Atlas Pay", "Atlas Commerce").
- **Fields**:
  - `id`: Long
  - `key`: `ProductKey` enum (`PAY`, `COMMERCE`, `LOGISTICS`, `HR`, `ACCOUNTING`)
  - `name`: String
  - `description`: String
  - `status`: `ProductStatus` (ACTIVE, INACTIVE, DEPRECATED)
  - `createdAt`: ZonedDateTime
  - `updatedAt`: ZonedDateTime
- **Methods**:
  - `create(Long id, ProductKey key, String name, String description)` — static factory, raises `HubProductCreatedEvent`
  - `updateDetails(String name, String description)` — raises `HubProductUpdatedEvent`
  - `deactivate()` — guards against non-ACTIVE status, raises `HubProductDeactivatedEvent`

**`ProductPricing` (Entity)**
Defines the subscription cost for a `HubProduct` per billing cycle and currency.
- **Fields**:
  - `id`: Long
  - `hubProductId`: Long
  - `billingCycle`: `BillingCycle` (MONTHLY, ANNUALLY)
  - `amount`: `Money` (carries both `BigDecimal` amount and `CurrencyCode`)
- **Unique Constraint**: `(hubProductId, billingCycle, currencyCode)` — only one price per cycle+currency combo
- **Methods**:
  - `updatePrice(Money newAmount)`

> **Multi-Currency Note**: The Organization's base currency is derived from `User.country` at registration. When subscribing, the `billing` module queries `ProductPricing` for the matching `(productId, billingCycle, currencyCode)` row to generate the invoice amount.

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `HubProductCreatedEvent` | New product created | `billing` (may auto-create skeleton subscriptions), `notifications` |
| `HubProductUpdatedEvent` | Product name/description updated | `billing` (informational) |
| `HubProductDeactivatedEvent` | Product deactivated | `billing` (cancel new subscriptions for this product) |

---

## 4. Exceptions & Errors

**`CatalogErrorCode`** (implements `ErrorCode`):
- `PRODUCT_NOT_FOUND`
- `DUPLICATE_PRODUCT_KEY`
- `INVALID_PRICING_MODEL`
- `PRODUCT_NOT_ACTIVE`

---

## 5. Commands & Use Cases

- `CreateHubProductCommand(ProductKey key, String name, String description)` → `CreateHubProductUseCase`
- `UpdateHubProductCommand(Long productId, String name, String description)` → `UpdateHubProductUseCase`
- `SetProductPricingCommand(Long productId, BillingCycle cycle, Money amount)` → `SetProductPricingUseCase`
  Upserts a `ProductPricing` row keyed by `(productId, billingCycle, currencyCode)`.

---

## 6. Queries

- `ListHubProductsQuery(ProductStatus status)` → `List<HubProductResult>`
  Used by the `billing` module and the organization dashboard to show available products.
- `GetHubProductDetailsQuery(Long productId)` → `HubProductDetailsResult`
  Returns the product with all its configured pricing tiers.

---

## 7. Listeners

None. Catalog is the **source of truth** for platform products and does not react to other module events. It is only mutated by admin commands.

---

## 8. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `HubProduct` and `ProductPricing` to prevent concurrent admin overrides (e.g., two admins updating the same product price simultaneously).

### Inbox & Outbox Patterns
- **Outbox**: Reliably publishes `HubProductUpdatedEvent` so that billing is notified when pricing changes. A billing module can use this to proactively recalculate upcoming invoice amounts for organizations on affected plans.
