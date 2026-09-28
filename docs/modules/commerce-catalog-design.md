# Commerce Catalog Design (`atlashub-commerce` / `com.atlashub.commerce.catalog`)

## Role & Purpose

The `catalog` subpackage manages the organization's product catalog: products, variants, pricing, discounts, suppliers, purchase orders, and marketplace vendors. This is distinct from the platform-level `atlashub-platform:catalog` (which manages AtlasHub's own subscription products). Every organization manages its own catalog here.

Gradle module: `atlashub-commerce`  
Package: `com.atlashub.commerce.catalog`

---

## Domain Layer

### `Product` (Aggregate Root)

**Package:** `com.atlashub.commerce.catalog.domain.entities`

```
Product
├── id             : Long
├── organizationId : Long
├── vendorId       : Long              ← nullable; set for marketplace vendor products
├── code           : String            ← SKU or barcode
├── name           : String
├── description    : String
├── categoryId     : Long
├── departmentId   : Long              ← nullable
├── manufacturerId : Long              ← nullable
├── isTaxable      : Boolean
├── isService      : Boolean           ← services have no inventory
├── hasVariants    : Boolean
├── status         : ProductStatus     ← ACTIVE | INACTIVE | PENDING_APPROVAL | DELETED
├── type           : ProductType       ← PHYSICAL | DIGITAL | SERVICE
└── createdAt      : ZonedDateTime
```

**Business methods (on entity):**

| Method | Guard | Events | Exceptions |
|---|---|---|---|
| `approve()` | status == PENDING_APPROVAL | `ProductApprovedEvent` | `InvalidProductStateException` |
| `reject(reason)` | status == PENDING_APPROVAL | — | `InvalidProductStateException` |
| `deactivate()` | status == ACTIVE | — | `InvalidProductStateException` |
| `markAsDeleted()` | any | — | — |

**Why on entity:** All transitions are pure state changes on a single aggregate's own fields.

---

### `ProductVariant` (Entity)

```
ProductVariant
├── id         : Long
├── productId  : Long
├── sku        : String
├── attributes : Map<String, String>   ← e.g., {size: "XL", color: "Red"}
└── isActive   : Boolean
```

---

### `ProductPrice` (Entity)

```
ProductPrice
├── id           : Long
├── productId    : Long
├── variantId    : Long                ← nullable; null = base product price
├── priceLevel   : PriceLevel          ← RETAIL | WHOLESALE
├── costPrice    : Money
├── sellingPrice : Money
└── markup       : BigDecimal
```

---

### `CustomerPriceOverride` (Entity)

Per-customer pricing for corporate accounts.

```
CustomerPriceOverride
├── id           : Long
├── productId    : Long
├── customerId   : Long
├── priceLevel   : PriceLevel
└── price        : Money
```

---

### `Discount` (Aggregate Root)

```
Discount
├── id              : Long
├── organizationId  : Long
├── name            : String
├── type            : DiscountType     ← PERCENTAGE | FLAT_AMOUNT
├── value           : BigDecimal
├── scope           : DiscountScope    ← ORDER_LEVEL | ITEM_LEVEL
├── minOrderAmount  : Money
├── maxUses         : Integer          ← nullable; null = unlimited
├── usedCount       : Integer
├── validFrom       : LocalDate
├── validTo         : LocalDate
└── isActive        : Boolean
```

**Business methods:**
- `applyTo(Money orderTotal)` — validates `isActive`, within date range, `usedCount < maxUses`, `orderTotal >= minOrderAmount`; increments `usedCount`; throws `DiscountExpiredException`, `DiscountMaxUsesReachedException`, `DiscountMinimumNotMetException`

---

### `Supplier` (Aggregate Root)

```
Supplier
├── id             : Long
├── organizationId : Long
├── name           : String
├── email          : EmailAddress
├── phone          : PhoneNumber
├── address        : String
└── status         : SupplierStatus   ← ACTIVE | INACTIVE
```

**Business methods:** `deactivate()`, `reactivate()`

---

### `PurchaseOrder` (Aggregate Root)

```
PurchaseOrder
├── id                   : Long
├── organizationId       : Long
├── outletId             : Long
├── supplierId           : Long
├── status               : PurchaseOrderStatus ← DRAFT | SENT | PARTIALLY_RECEIVED | RECEIVED | CANCELLED
├── totalAmount          : Money
├── expectedDeliveryDate : LocalDate
└── items                : List<PurchaseOrderItem>
```

**Business methods (on entity):**

| Method | Guard | Events | Exceptions |
|---|---|---|---|
| `addItem(productId, qty, unitCost)` | status == DRAFT | — | `InvalidPurchaseOrderStateException` |
| `send()` | status == DRAFT, items > 0 | `PurchaseOrderSentEvent` | `InvalidPurchaseOrderStateException` |
| `receiveItems(receivedItems)` | status == SENT or PARTIALLY_RECEIVED | `PurchaseOrderReceivedEvent` (if fully received) | `InvalidPurchaseOrderStateException` |
| `cancel()` | status == DRAFT or SENT | — | `InvalidPurchaseOrderStateException` |

### `PurchaseOrderItem` (Entity)

```
PurchaseOrderItem
├── id              : Long
├── purchaseOrderId : Long
├── productId       : Long
├── quantityOrdered : Integer
├── quantityReceived: Integer
└── unitCost        : Money
```

---

### `Vendor` (Aggregate Root) — Marketplace mode

```
Vendor
├── id                       : Long
├── organizationId           : Long   ← marketplace that owns this vendor
├── userId                   : Long   ← vendor's platform user account
├── businessName             : String
├── email                    : EmailAddress
├── phone                    : PhoneNumber
├── settlementBankCode       : String
├── settlementAccountNumber  : String
├── settlementAccountName    : String
├── commissionRate           : BigDecimal
├── disbursementSchedule     : DisbursementSchedule ← DAILY | WEEKLY | ON_DEMAND
├── status                   : VendorStatus          ← PENDING | ACTIVE | SUSPENDED | TERMINATED
└── createdAt                : ZonedDateTime
```

**Business methods (on entity):**
- `approve()` → PENDING → ACTIVE; registers `VendorApprovedEvent`
- `suspend(reason)` → ACTIVE → SUSPENDED; registers `VendorSuspendedEvent`
- `terminate()` → any → TERMINATED; registers `VendorTerminatedEvent`

---

### Domain Events — `com.atlashub.commerce.catalog.domain.events`

| Event | Published When | Consumed By |
|---|---|---|
| `ProductCreatedEvent` | Product added to catalog | `inventory` (init stock records per outlet) |
| `ProductApprovedEvent` | Marketplace vendor product approved | `notifications` (notify vendor) |
| `PurchaseOrderSentEvent` | PO sent to supplier | `notifications` (email supplier), `logistics` (inbound shipment) |
| `PurchaseOrderReceivedEvent` | PO fully received | `accounting` (Dr Inventory, Cr Accounts Payable) |
| `VendorApprovedEvent` | Vendor application approved | `notifications`, `pay` (create split rule for vendor) |
| `VendorSuspendedEvent` | Vendor suspended | `notifications` |
| `VendorTerminatedEvent` | Vendor terminated | `notifications` |

---

### Domain Exceptions — `com.atlashub.commerce.catalog.domain.exceptions`

```java
public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(Long id) { super("Product not found: " + id); }
}
public class ProductCodeAlreadyExistsException extends ConflictException {
    public ProductCodeAlreadyExistsException(String code) { super("Product code already exists: " + code); }
}
public class InvalidProductStateException extends BusinessRuleException {
    public InvalidProductStateException(String message) { super(message); }
}
public class ProductPendingApprovalException extends BusinessRuleException {
    public ProductPendingApprovalException() { super("Product is pending approval and cannot be sold"); }
}
public class SupplierNotFoundException extends NotFoundException {
    public SupplierNotFoundException(Long id) { super("Supplier not found: " + id); }
}
public class VendorNotFoundException extends NotFoundException {
    public VendorNotFoundException(Long id) { super("Vendor not found: " + id); }
}
public class VendorSuspendedException extends BusinessRuleException {
    public VendorSuspendedException() { super("Vendor is suspended and cannot list products"); }
}
public class PurchaseOrderNotFoundException extends NotFoundException {
    public PurchaseOrderNotFoundException(Long id) { super("Purchase order not found: " + id); }
}
public class InvalidPurchaseOrderStateException extends BusinessRuleException {
    public InvalidPurchaseOrderStateException(String message) { super(message); }
}
public class DiscountNotFoundException extends NotFoundException {
    public DiscountNotFoundException(Long id) { super("Discount not found: " + id); }
}
public class DiscountExpiredException extends BusinessRuleException {
    public DiscountExpiredException() { super("This discount has expired"); }
}
public class DiscountMaxUsesReachedException extends BusinessRuleException {
    public DiscountMaxUsesReachedException() { super("This discount has reached its maximum usage limit"); }
}
public class DiscountMinimumNotMetException extends BusinessRuleException {
    public DiscountMinimumNotMetException(Money minimum) { super("Order does not meet the minimum amount of " + minimum); }
}
```

---

## Application Layer

### Commands — `com.atlashub.commerce.catalog.application.commands`

#### `CreateProductCommand`
```java
record CreateProductCommand(Long organizationId, Long vendorId, String code, String name,
                            String description, Long categoryId, Long departmentId,
                            boolean isTaxable, boolean isService, boolean hasVariants, ProductType type)
```
**Handler:** `CreateProductHandler` | **Response:** `CreateProductResponse(Long productId)`  
**RBAC:** `@PreAuthorize("hasAuthority('commerce:products:create')")`  
**Flow:** Check unique code per org → `ProductCodeAlreadyExistsException`; create `Product` (status = ACTIVE unless vendor product → PENDING_APPROVAL); `repository.save()` → publishes `ProductCreatedEvent`

---

#### `ApproveVendorProductCommand`
```java
record ApproveVendorProductCommand(Long productId, Long approvedByUserId)
```
**Handler:** `ApproveVendorProductHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('commerce:vendors:approve')")`

---

#### `SetProductPriceCommand`
```java
record SetProductPriceCommand(Long productId, Long variantId, PriceLevel priceLevel,
                              Money costPrice, Money sellingPrice)
```
**Handler:** `SetProductPriceHandler` | **Response:** `void`

---

#### `CreateDiscountCommand`
```java
record CreateDiscountCommand(Long organizationId, String name, DiscountType type,
                             BigDecimal value, DiscountScope scope, Money minOrderAmount,
                             Integer maxUses, LocalDate validFrom, LocalDate validTo)
```
**Handler:** `CreateDiscountHandler` | **Response:** `CreateDiscountResponse(Long discountId)`

---

#### `CreateSupplierCommand`
```java
record CreateSupplierCommand(Long organizationId, String name, String email, String phone, String address)
```
**Handler:** `CreateSupplierHandler` | **Response:** `CreateSupplierResponse(Long supplierId)`

---

#### `CreateVendorCommand`
```java
record CreateVendorCommand(Long organizationId, Long userId, String businessName, String email,
                           String phone, String settlementBankCode, String settlementAccountNumber,
                           BigDecimal commissionRate, DisbursementSchedule disbursementSchedule)
```
**Handler:** `CreateVendorHandler` | **Response:** `CreateVendorResponse(Long vendorId)`

---

#### `ApproveVendorCommand`
```java
record ApproveVendorCommand(Long vendorId)
```
**Handler:** `ApproveVendorHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('commerce:vendors:approve')")`

---

#### `SuspendVendorCommand`
```java
record SuspendVendorCommand(Long vendorId, String reason)
```
**Handler:** `SuspendVendorHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('commerce:vendors:approve')")`

---

#### `CreatePurchaseOrderCommand`
```java
record CreatePurchaseOrderCommand(Long organizationId, Long outletId, Long supplierId,
                                  LocalDate expectedDeliveryDate, List<PurchaseOrderItemDto> items)
```
**Handler:** `CreatePurchaseOrderHandler` | **Response:** `CreatePurchaseOrderResponse(Long poId)`

---

#### `SendPurchaseOrderCommand`
```java
record SendPurchaseOrderCommand(Long purchaseOrderId)
```
**Handler:** `SendPurchaseOrderHandler` | **Response:** `void`  
**Flow:** Load `PurchaseOrder` → `po.send()` → `repository.save()` → `PurchaseOrderSentEvent` published

---

#### `ReceivePurchaseOrderCommand`
```java
record ReceivePurchaseOrderCommand(Long purchaseOrderId, List<ReceivedItemDto> receivedItems)
```
**Handler:** `ReceivePurchaseOrderHandler` | **Response:** `void`  
**Flow:** Load `PurchaseOrder` → `po.receiveItems(items)` → `repository.save()` → if fully received, `PurchaseOrderReceivedEvent` published → inventory.addStock() called via domain event

---

### Queries — `com.atlashub.commerce.catalog.application.queries`

#### `ListProductsQuery`
```java
record ListProductsQuery(Long organizationId, Long vendorId, Long categoryId,
                         ProductStatus status, String search, int page, int size)
```
**Handler:** `ListProductsHandler` | **Result:** `PageResult<ProductResult>`  
**Justification:** Large, searchable — organizations can have thousands of products.

---

#### `SearchProductByBarcodeQuery`
```java
record SearchProductByBarcodeQuery(Long organizationId, String barcode)
```
**Handler:** `SearchProductByBarcodeHandler` | **Result:** `ProductResult` (single)  
**Justification:** Point-of-use barcode scan — returns single match.

---

#### `ListSuppliersQuery`
```java
record ListSuppliersQuery(Long organizationId, SupplierStatus status)
```
**Handler:** `ListSuppliersHandler` | **Result:** `List<SupplierResult>`  
**Justification:** Bounded per org.

---

#### `ListVendorsQuery`
```java
record ListVendorsQuery(Long organizationId, VendorStatus status)
```
**Handler:** `ListVendorsHandler` | **Result:** `List<VendorResult>`

---

#### `ListPurchaseOrdersQuery`
```java
record ListPurchaseOrdersQuery(Long organizationId, PurchaseOrderStatus status, int page, int size)
```
**Handler:** `ListPurchaseOrdersHandler` | **Result:** `PageResult<PurchaseOrderResult>`

---

## Infrastructure Layer

### Persistence

**JPA Entities + Tables:**

| Entity | Table | Locking |
|---|---|---|
| `ProductJpaEntity` | `commerce_products` | `@Version` optimistic |
| `ProductVariantJpaEntity` | `commerce_product_variants` | — |
| `ProductPriceJpaEntity` | `commerce_product_prices` | — |
| `DiscountJpaEntity` | `commerce_discounts` | `@Version` optimistic |
| `SupplierJpaEntity` | `commerce_suppliers` | — |
| `PurchaseOrderJpaEntity` | `commerce_purchase_orders` | `@Version` optimistic |
| `PurchaseOrderItemJpaEntity` | `commerce_purchase_order_items` | — |
| `VendorJpaEntity` | `commerce_vendors` | `@Version` optimistic |

**Key Spring Data methods:**
```
ProductJpaRepository
  + findByOrganizationIdAndCode(Long orgId, String code): Optional<ProductJpaEntity>
  + findByOrganizationIdAndStatus(Long orgId, ProductStatus status, Pageable p): Page<ProductJpaEntity>

VendorJpaRepository
  + findByOrganizationIdAndStatus(Long orgId, VendorStatus status): List<VendorJpaEntity>
```

**Repository Adapters + Sequences:**
- `ProductRepositoryAdapter` → `commerce_product_seq`
- `DiscountRepositoryAdapter` → `commerce_discount_seq`
- `SupplierRepositoryAdapter` → `commerce_supplier_seq`
- `PurchaseOrderRepositoryAdapter` → `commerce_purchase_order_seq`
- `VendorRepositoryAdapter` → `commerce_vendor_seq`

---

## Presentation Layer

### Controller: `CommerceCatalogController`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/products` | `commerce:products:create` | `CreateProductRequest` | `CreateProductResponse` |
| `GET` | `/products` | — | `?orgId&vendorId&status&search&page&size` | `PageResult<ProductResult>` |
| `GET` | `/products/barcode` | — | `?orgId&barcode` | `ProductResult` |
| `POST` | `/products/{id}/approve` | `commerce:vendors:approve` | — | `void` |
| `POST` | `/products/{id}/price` | `commerce:products:create` | `SetProductPriceRequest` | `void` |
| `POST` | `/discounts` | `commerce:products:create` | `CreateDiscountRequest` | `CreateDiscountResponse` |
| `POST` | `/suppliers` | `commerce:products:create` | `CreateSupplierRequest` | `CreateSupplierResponse` |
| `GET` | `/suppliers` | — | `?orgId&status` | `List<SupplierResult>` |
| `POST` | `/purchase-orders` | — | `CreatePurchaseOrderRequest` | `CreatePurchaseOrderResponse` |
| `POST` | `/purchase-orders/{id}/send` | — | — | `void` |
| `POST` | `/purchase-orders/{id}/receive` | — | `ReceivePurchaseOrderRequest` | `void` |
| `GET` | `/purchase-orders` | — | `?orgId&status&page&size` | `PageResult<PurchaseOrderResult>` |
| `POST` | `/vendors` | — | `CreateVendorRequest` | `CreateVendorResponse` |
| `POST` | `/vendors/{id}/approve` | `commerce:vendors:approve` | — | `void` |
| `POST` | `/vendors/{id}/suspend` | `commerce:vendors:approve` | `SuspendVendorRequest` | `void` |
| `GET` | `/vendors` | — | `?orgId&status` | `List<VendorResult>` |

---

## RBAC Table

| Permission | Commands |
|---|---|
| `commerce:products:create` | `CreateProductHandler`, `SetProductPriceHandler`, `CreateDiscountHandler`, `CreateSupplierHandler` |
| `commerce:vendors:approve` | `ApproveVendorProductHandler`, `ApproveVendorHandler`, `SuspendVendorHandler` |

---

## Complete File List

```
atlashub-commerce/src/main/java/com/atlashub/commerce/catalog/
├── domain/
│   ├── entities/
│   │   ├── Product.java
│   │   ├── ProductVariant.java
│   │   ├── ProductPrice.java
│   │   ├── CustomerPriceOverride.java
│   │   ├── Discount.java
│   │   ├── Supplier.java
│   │   ├── PurchaseOrder.java
│   │   ├── PurchaseOrderItem.java
│   │   └── Vendor.java
│   ├── events/
│   │   ├── ProductCreatedEvent.java
│   │   ├── ProductApprovedEvent.java
│   │   ├── PurchaseOrderSentEvent.java
│   │   ├── PurchaseOrderReceivedEvent.java
│   │   ├── VendorApprovedEvent.java
│   │   ├── VendorSuspendedEvent.java
│   │   └── VendorTerminatedEvent.java
│   ├── exceptions/
│   │   ├── ProductNotFoundException.java
│   │   ├── ProductCodeAlreadyExistsException.java
│   │   ├── InvalidProductStateException.java
│   │   ├── ProductPendingApprovalException.java
│   │   ├── SupplierNotFoundException.java
│   │   ├── VendorNotFoundException.java
│   │   ├── VendorSuspendedException.java
│   │   ├── PurchaseOrderNotFoundException.java
│   │   ├── InvalidPurchaseOrderStateException.java
│   │   ├── DiscountNotFoundException.java
│   │   ├── DiscountExpiredException.java
│   │   ├── DiscountMaxUsesReachedException.java
│   │   └── DiscountMinimumNotMetException.java
│   ├── repositories/
│   │   ├── ProductRepository.java
│   │   ├── DiscountRepository.java
│   │   ├── SupplierRepository.java
│   │   ├── PurchaseOrderRepository.java
│   │   └── VendorRepository.java
│   └── valueobject/
│       ├── ProductStatus.java
│       ├── ProductType.java
│       ├── PriceLevel.java
│       ├── DiscountType.java
│       ├── DiscountScope.java
│       ├── SupplierStatus.java
│       ├── PurchaseOrderStatus.java
│       ├── VendorStatus.java
│       └── DisbursementSchedule.java
├── application/
│   ├── commands/
│   │   ├── CreateProduct/ ...
│   │   ├── ApproveVendorProduct/ ...
│   │   ├── SetProductPrice/ ...
│   │   ├── CreateDiscount/ ...
│   │   ├── CreateSupplier/ ...
│   │   ├── CreateVendor/ ...
│   │   ├── ApproveVendor/ ...
│   │   ├── SuspendVendor/ ...
│   │   ├── CreatePurchaseOrder/ ...
│   │   ├── SendPurchaseOrder/ ...
│   │   └── ReceivePurchaseOrder/ ...
│   └── queries/
│       ├── ListProducts/ ...
│       ├── SearchProductByBarcode/ ...
│       ├── ListSuppliers/ ...
│       ├── ListVendors/ ...
│       └── ListPurchaseOrders/ ...
├── infrastructure/
│   ├── persistence/
│   │   ├── adapters/ [ProductRepositoryAdapter, DiscountRepositoryAdapter, SupplierRepositoryAdapter, PurchaseOrderRepositoryAdapter, VendorRepositoryAdapter]
│   │   ├── entities/ [ProductJpaEntity, ProductVariantJpaEntity, ProductPriceJpaEntity, DiscountJpaEntity, SupplierJpaEntity, PurchaseOrderJpaEntity, PurchaseOrderItemJpaEntity, VendorJpaEntity]
│   │   ├── mappers/ [ProductMapper, DiscountMapper, SupplierMapper, PurchaseOrderMapper, VendorMapper]
│   │   └── repositories/ [ProductJpaRepository, DiscountJpaRepository, SupplierJpaRepository, PurchaseOrderJpaRepository, VendorJpaRepository]
└── presentation/
    ├── dto/ [CreateProductRequest, CreateProductResponse, SetProductPriceRequest, CreateDiscountRequest, CreateDiscountResponse, CreateSupplierRequest, CreateSupplierResponse, CreateVendorRequest, CreateVendorResponse, CreatePurchaseOrderRequest, CreatePurchaseOrderResponse, ReceivePurchaseOrderRequest, SuspendVendorRequest, ProductResult, SupplierResult, VendorResult, PurchaseOrderResult]
    └── rest/
        └── CommerceCatalogController.java
```
