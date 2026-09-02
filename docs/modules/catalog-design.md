# Platform Catalog Module Design (`atlashub-platform:catalog`)

## 1. Domain Entities & Aggregates

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
  - `amount`: **`Money`** (carries both `BigDecimal` amount and `CurrencyCode`)
- **Methods**:
  - `updatePrice(Money newAmount)`

> **Multi-Currency Note**: `ProductPricing` may have multiple entries per `HubProduct` — one per (`BillingCycle`, `CurrencyCode`) combination. This allows the same product to be priced in NGN for Nigerian orgs and KES for Kenyan orgs. The Organization's base currency (derived from the `country` field on `User` at registration) determines which pricing row is selected at subscription time.

## 2. Domain Events (Wrapped in `EnvelopedDomainEvent`)
- `HubProductCreatedEvent(String productId, ProductKey key)`
- `HubProductUpdatedEvent(String productId)`
- `HubProductDeactivatedEvent(String productId)`

## 3. Exceptions & Errors
**`CatalogErrorCode`** (implements `ErrorCode`):
- `PRODUCT_NOT_FOUND`
- `DUPLICATE_PRODUCT_KEY`
- `INVALID_PRICING_MODEL`
- `PRODUCT_NOT_ACTIVE`

## 4. Commands & Use Cases
- **Command**: `CreateHubProductCommand(Long adminId, ProductKey key, String name, String description)`
  - **UseCase**: `CreateHubProductUseCase`
  - **Security**: `@PreAuthorize("@adminAuth.hasPermission(#command.adminId, 'CATALOG_MANAGE')")`
- **Command**: `UpdateHubProductCommand(Long adminId, Long productId, String name, String description)`
  - **UseCase**: `UpdateHubProductUseCase`
- **Command**: `SetProductPricingCommand(Long productId, BillingCycle cycle, Money amount)`
  - **UseCase**: `SetProductPricingUseCase`

## 5. Queries
- **Query**: `ListHubProductsQuery(ProductStatus status)` → `List<HubProductResult>`
- **Query**: `GetHubProductDetailsQuery(Long productId)` → `HubProductDetailsResult`

## 6. Listeners
None. Catalog is the source of truth for platform products and does not react to other module events.

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `HubProduct` and `ProductPricing` to prevent concurrent admin overrides.

### Inbox & Outbox Patterns
- **Outbox**: Reliably publishes `HubProductUpdatedEvent` so that billing mechanisms are notified of new pricing adjustments.
