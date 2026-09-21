# Catalog Module Design (`atlashub-platform:catalog`)

## Role & Purpose

The `catalog` module defines **what products AtlasHub sells** and **how much they cost**. This is the platform's internal product catalogue — completely separate from the product catalogues that organizations manage in `atlashub-commerce` (which is their own store inventory).

AtlasHub's products are things like:
- **Atlas Pay** — transaction-based pricing, tiered by volume
- **Atlas Commerce** — monthly subscription
- **Atlas Logistics** — monthly subscription
- **Atlas HR** — free
- **Atlas Accounting** — monthly subscription
- **Atlas Hotel** — future, premium subscription

The `catalog` module is the source of truth that the `billing` module reads when generating invoices and calculating what an organization owes.

---

## 1. Features

### Platform Products
AtlasHub admins define `Product` records — each representing a subscribable service. Each product has:
- A `pricingModel` (`SUBSCRIPTION_FLAT`, `SUBSCRIPTION_TIERED`, `TRANSACTION_FEE`, `FREE`)
- One or more `PricingPlan`s (e.g., "Starter", "Growth", "Enterprise")
- Currency-specific pricing (NGN, KES, USD)

### Transaction-Fee Pricing (Atlas Pay)
Atlas Pay uses `TRANSACTION_FEE` pricing. The fee structure is tiered — as monthly transaction volume increases, the per-transaction rate decreases:

```
Tier 1: Volume ₦0–₦1M/month       → 1.5% per transaction (capped at ₦2,000)
Tier 2: Volume ₦1M–₦10M/month     → 1.2% per transaction (capped at ₦1,500)
Tier 3: Volume ₦10M–₦100M/month   → 0.9% per transaction (capped at ₦1,000)
Tier 4: Volume > ₦100M/month       → 0.6% per transaction (capped at ₦500)
```

### Subscription Pricing (Commerce, Logistics, Accounting)
Subscription products have a fixed monthly fee per plan, billed in advance.

### Free Products (HR)
Products with `pricingModel = FREE` are accessible without generating any invoice.

### Multi-Currency Pricing
Each `PricingPlan` stores prices in multiple currencies. When a Nigerian org subscribes, they pay in NGN; a Kenyan org pays in KES.

---

## 2. Domain Entities & Aggregates

### `Product` (Aggregate Root)

```
Product
├── id: Long
├── code: String               ← unique slug: "ATLAS_PAY", "ATLAS_COMMERCE", "ATLAS_LOGISTICS", etc.
├── name: String
├── description: String
├── pricingModel: PricingModel ← FREE, SUBSCRIPTION_FLAT, SUBSCRIPTION_TIERED, TRANSACTION_FEE
├── isActive: Boolean
├── plans: List<PricingPlan>   ← only for SUBSCRIPTION models
├── feeStructure: FeeStructure ← only for TRANSACTION_FEE model
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `addPlan(PricingPlan plan)`
- `deactivatePlan(Long planId)`
- `updateFeeStructure(FeeStructure structure)`
- `deactivate()`

**Product Codes (Built-In):**
```
ATLAS_PAY        → pricingModel: TRANSACTION_FEE
ATLAS_COMMERCE   → pricingModel: SUBSCRIPTION_FLAT
ATLAS_LOGISTICS  → pricingModel: SUBSCRIPTION_FLAT
ATLAS_HR         → pricingModel: FREE
ATLAS_ACCOUNTING → pricingModel: SUBSCRIPTION_FLAT
ATLAS_HOTEL      → pricingModel: SUBSCRIPTION_FLAT (future)
```

---

### `PricingPlan` (Entity)

```
PricingPlan
├── id: Long
├── productId: Long
├── name: String                ← "Starter", "Growth", "Enterprise"
├── billingCycle: BillingCycle  ← MONTHLY, ANNUAL
├── prices: Map<Currency, Money> ← { NGN: ₦15,000, KES: KES 3,000, USD: $30 }
├── features: List<String>      ← human-readable feature list for marketing
├── isActive: Boolean
└── displayOrder: Integer
```

---

### `FeeStructure` (Value Object)

Used only for TRANSACTION_FEE products.

```
FeeStructure
├── tiers: List<FeeTier>
└── currency: Currency           ← base currency for tier thresholds

FeeTier
├── monthlyVolumeFrom: Money
├── monthlyVolumeTo: Money       ← nullable for last tier (unbounded)
├── feePercentage: BigDecimal    ← e.g., 1.5 (means 1.5%)
└── feeCap: Money                ← maximum fee per transaction
```

---

### `PricingModel` (Enum)
`FREE`, `SUBSCRIPTION_FLAT`, `SUBSCRIPTION_TIERED`, `TRANSACTION_FEE`

### `BillingCycle` (Enum)
`MONTHLY`, `ANNUAL`

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `ProductCreatedEvent` | New platform product added | `billing` (make available for subscription) |
| `PricingPlanUpdatedEvent` | Plan price changed | `billing` (apply to upcoming renewal invoices) |
| `ProductDeactivatedEvent` | Product disabled | `billing` (prevent new subscriptions) |

---

## 4. Outbound Port (Open Host Service)

```java
// In atlashub-shared
public interface CatalogQueryPort {
    Optional<ProductDto> findByCode(String productCode);
    Optional<PricingPlanDto> findPlanById(Long planId);
    FeeStructureDto getPayFeeStructure();       // used by billing for invoice calculation
    List<ProductDto> listActiveProducts();
}
```

---

## 5. Exceptions & Errors

**`CatalogErrorCode`**:
- `PRODUCT_NOT_FOUND`, `PLAN_NOT_FOUND`
- `PRODUCT_ALREADY_EXISTS`, `PLAN_ALREADY_EXISTS`
- `INVALID_FEE_STRUCTURE` — tiers are not contiguous or have gaps
- `PRODUCT_INACTIVE`

---

## 6. Commands & Use Cases (Admin-Only)

All catalog management is restricted to AtlasHub admin staff.

- `CreateProductCommand(code, name, description, pricingModel)` → `CreateProductUseCase`
- `AddPricingPlanCommand(productId, name, billingCycle, prices, features)` → `AddPricingPlanUseCase`
- `UpdatePricingPlanCommand(planId, prices)` → `UpdatePricingPlanUseCase`
- `DeactivatePricingPlanCommand(planId)` → `DeactivatePricingPlanUseCase`
- `UpdateFeeStructureCommand(productId, tiers)` → `UpdateFeeStructureUseCase`
- `DeactivateProductCommand(productId)` → `DeactivateProductUseCase`

---

## 7. Queries

- `ListActiveProductsQuery()` → `List<ProductResult>` — for the subscription signup page
- `GetProductByCodeQuery(code)` → `ProductResult`
- `ListPricingPlansQuery(productId, currency)` → `List<PricingPlanResult>` — currency-filtered
- `GetFeeStructureQuery(productCode, currency)` → `FeeStructureResult`
