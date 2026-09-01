# Platform Catalog Module Design (`atlashub-platform:catalog`)

## 1. Domain Entities & Aggregates

**`HubProduct` (Aggregate Root)**
Represents a standalone platform product offering (e.g., "Atlas Pay", "Atlas Commerce").
- **Fields**: 
  - `id`: Long
  - `key`: String (e.g., `COMMERCE`, `PAY`, `LOGISTICS`)
  - `name`: String
  - `description`: String
  - `status`: `ProductStatus` (ACTIVE, INACTIVE, DEPRECATED)
  - `createdAt`: ZonedDateTime
  - `updatedAt`: ZonedDateTime
- **Methods**:
  - `updateDetails(String name, String description)`
  - `deactivate()`

**`ProductPricing` (Entity)**
Defines the subscription cost for a `HubProduct`.
- **Fields**:
  - `id`: Long
  - `hubProductId`: Long
  - `billingCycle`: `BillingCycle` (MONTHLY, ANNUALLY)
  - `amount`: BigDecimal
  - `currency`: String
- **Methods**:
  - `updatePrice(BigDecimal newAmount)`

## 2. Domain Events (Wrapped in `EnvelopedDomainEvent`)
- `HubProductCreatedEvent(Long productId, String key)`
- `HubProductUpdatedEvent(Long productId)`
- `HubProductDeactivatedEvent(Long productId)`

## 3. Exceptions & Errors
**`CatalogErrorCode`** (implements `ErrorCode`):
- `PRODUCT_NOT_FOUND`
- `DUPLICATE_PRODUCT_KEY`
- `INVALID_PRICING_MODEL`

## 4. Commands & Use Cases
- **Command**: `CreateHubProductCommand(String key, String name, String description)`
  - **UseCase**: `CreateHubProductUseCase`
- **Command**: `UpdateHubProductCommand(Long productId, String name, String description)`
  - **UseCase**: `UpdateHubProductUseCase`
- **Command**: `SetProductPricingCommand(Long productId, BillingCycle cycle, BigDecimal amount)`
  - **UseCase**: `SetProductPricingUseCase`

## 5. Queries
- **Query**: `ListHubProductsQuery(ProductStatus status)`
  - **Returns**: `List<HubProductResult>`
- **Query**: `GetHubProductDetailsQuery(Long productId)`
  - **Returns**: `HubProductDetailsResult`

## 6. Listeners
- None. (Catalog is the source of truth for platform products and rarely reacts to other module events).

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `HubProduct` and `ProductPricing` to prevent concurrent admin overrides of product catalogs and pricing.

### Inbox & Outbox Patterns
- **Outbox**: Used to reliably publish `HubProductUpdatedEvent` so that billing mechanisms are notified of new capabilities or pricing adjustments.
