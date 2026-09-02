# Commerce & POS Module Design (`atlashub-commerce`)

## 1. Domain Entities & Aggregates

### `catalog` Submodule
**`Product` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `code` (SKU/barcode), `name`, `description`, `categoryId`, `departmentId`, `manufacturerId`, `isTaxable`: Boolean, `isService`: Boolean, `status` (ACTIVE, INACTIVE, DELETED), `hasVariants`: Boolean
- **Methods**: `updateDetails(...)`, `markAsDeleted()`

**`ProductPrice` (Entity)**
- **Fields**: `id`, `productId`, `priceLevel` (RETAIL, WHOLESALE), `costPrice`: **`Money`**, `sellingPrice`: **`Money`**, `markup`: BigDecimal

**`CustomerPriceOverride` (Entity)**
Per-customer pricing overrides, enabling custom pricing for specific accounts (e.g., loyal clients, corporate accounts).
- **Fields**: `id`, `productId`, `customerId`, `organizationId`, `price`: **`Money`**, `effectiveFrom`: LocalDate, `effectiveTo`: LocalDate (nullable)

**`Category`, `Department`, `Manufacturer` (Entities)**
- **Fields**: `id`, `organizationId`, `name`, `description`

**`Discount` (Aggregate Root)**
Reusable discount configurations.
- **Fields**: `id`, `organizationId`, `name`, `type` (PERCENTAGE, FLAT_AMOUNT), `value`: BigDecimal, `scope` (ORDER_LEVEL, ITEM_LEVEL), `isActive`: Boolean
- **Methods**: `activate()`, `deactivate()`

**`PurchaseOrder` (Aggregate Root)**
Represents a formal stock replenishment request sent to a supplier.
- **Fields**: `id`, `organizationId`, `outletId`, `supplierId`, `status` (DRAFT, SENT, PARTIALLY_RECEIVED, RECEIVED, CANCELLED), `totalAmount`: **`Money`**, `expectedDeliveryDate`: LocalDate, `createdAt`: ZonedDateTime
- **Methods**: `addItem(PurchaseOrderItem)`, `send()`, `receive(List<ReceivedItem> items)`, `cancel()`

**`PurchaseOrderItem` (Entity)**
- **Fields**: `id`, `purchaseOrderId`, `productId`, `orderedQty`, `receivedQty`, `unitCost`: **`Money`**

**`Supplier` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `name`, `email`, `phone`, `address`, `status` (ACTIVE, INACTIVE)

### `storefront` (POS) Submodule

**`SalesOrder` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `outletId`, `customerId`, `cashierId`, `tillId`, `totalGross`: **`Money`**, `totalDiscount`: **`Money`**, `totalTax`: **`Money`**, `totalNet`: **`Money`**, `type` (RETAIL, WHOLESALE, HOSPITALITY), `status` (PENDING, PAYMENT_PENDING, COMPLETED, REFUNDED, LAYAWAY, PROFORMA, FAILED), `saleDate`: ZonedDateTime
- **Methods**: `addItem(SalesOrderItem)`, `applyDiscount(Discount discount)`, `applyItemDiscount(Long itemId, Discount discount)`, `complete()`, `refund(String reason)`, `convertToLayaway()`, `convertToProforma()`

**`SalesOrderItem` (Entity)**
- **Fields**: `id`, `salesOrderId`, `productId`, `quantity`, `unitPrice`: **`Money`**, `discountAmount`: **`Money`**, `taxAmount`: **`Money`**, `subtotal`: **`Money`**

**`CustomerDeposit` / Layaway (Aggregate Root)**
- **Fields**: `id`, `salesOrderId`, `amountPaid`: **`Money`**, `balanceRemaining`: **`Money`**, `status` (ACTIVE, RECALLED, FULFILLED)
- **Methods**: `addPayment(Money amount)`, `recall()`, `fulfil()`

**`CustomerCredit` (Aggregate Root)**
Tracks customer accounts sold to on credit (outstanding debt).
- **Fields**: `id`, `organizationId`, `customerId`, `totalDebt`: **`Money`**, `creditLimit`: **`Money`**, `status` (WITHIN_LIMIT, OVER_LIMIT, SETTLED)
- **Methods**: `addDebt(Money amount)`, `recordPayment(Money amount)`, `isWithinLimit(Money newDebt)`: Boolean

**`Till` (Aggregate Root)**
Represents a physical cash drawer at a POS terminal.
- **Fields**: `id`, `organizationId`, `outletId`, `name`, `openingFloat`: **`Money`**, `expectedClosingBalance`: **`Money`**, `actualClosingBalance`: **`Money`**, `status` (OPEN, CLOSED), `openedAt`: ZonedDateTime, `closedAt`: ZonedDateTime, `openedBy`: Long, `closedBy`: Long
- **Methods**: `open(Long userId, Money float)`, `close(Long userId, Money actualBalance)`, `recordVariance()`

**`HospitalityTable` (Aggregate Root)**
Represents a dine-in table for restaurant/bar operations.
- **Fields**: `id`, `organizationId`, `outletId`, `tableNumber`: String, `covers`: Integer, `status` (AVAILABLE, OCCUPIED, RESERVED, BILL_REQUESTED)
- **Methods**: `occupy(Integer covers, Long waiterId)`, `free()`, `requestBill()`

**`KitchenOrderTicket` (Aggregate Root)**
Represents a kitchen print ticket generated when food items are ordered.
- **Fields**: `id`, `salesOrderId`, `tableId`, `items`: `List<KotItem>`, `status` (PENDING, IN_PROGRESS, READY, SERVED), `sentAt`: ZonedDateTime
- **Methods**: `markInProgress()`, `markReady()`, `markServed()`

### `inventory` Submodule

**`Inventory` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `outletId`, `productId`, `quantity`, `reorderLevel`, `safeStock`
- **Methods**: `addStock(Integer qty)`, `deductStock(Integer qty)`, `reserveStock(Integer qty)`, `releaseReservedStock(Integer qty)`

**`StockAdjustment` (Aggregate Root)**
- **Fields**: `id`, `inventoryId`, `adjustedBy`, `previousQty`, `newQty`, `reason` (DAMAGE, EXPIRY, THEFT, CORRECTION)

**`StockCount` (Aggregate Root)**
- **Fields**: `id`, `outletId`, `status` (OPEN, RECONCILED), `countedItems`: `List<StockCountItem>`, `startedAt`: ZonedDateTime, `reconciledAt`: ZonedDateTime

**`SupplyRefund` / Return Outwards (Aggregate Root)**
- **Fields**: `id`, `supplierId`, `outletId`, `productId`, `quantity`, `refundValue`: **`Money`**, `status` (PENDING, DISPATCHED, CONFIRMED, FAILED)

**`CustomerReturn` (Aggregate Root)**
Represents a customer returning a purchased item to the store (distinct from supplier return).
- **Fields**: `id`, `salesOrderId`, `organizationId`, `outletId`, `customerId`, `productId`, `quantity`, `refundAmount`: **`Money`**, `reason`: String, `refundMethod` (CASH, CREDIT_NOTE, WALLET), `status` (PENDING, APPROVED, REFUNDED)
- **Methods**: `approve()`, `processRefund()`

## 2. Domain Events
- `ProductCreatedEvent(Long productId, String code, Long orgId)`
- `ProductPriceUpdatedEvent(Long productId, String priceLevel, Money sellingPrice)`
- `PurchaseOrderSentEvent(Long purchaseOrderId, Long supplierId)`
- `PurchaseOrderReceivedEvent(Long purchaseOrderId)`
- `PosSaleCompletedEvent(Long salesOrderId, Long outletId, Money totalNet)`
- `PosSaleRefundedEvent(Long salesOrderId)`
- `CustomerReturnApprovedEvent(Long returnId, Long salesOrderId, Money refundAmount)`
- `LayawayCreatedEvent(Long depositId, Money amount)`
- `StockAdjustedEvent(Long inventoryId, Integer difference, String reason)`
- `StockCountReconciledEvent(Long countId)`
- `SupplyRefundProcessedEvent(Long refundId)`
- `TillOpenedEvent(Long tillId, Long userId, Money openingFloat)`
- `TillClosedEvent(Long tillId, Long userId, Money actualBalance, Money variance)`
- `CustomerCreditUpdatedEvent(Long creditId, Long customerId, Money totalDebt)`

## 3. Exceptions & Errors
**`CommerceErrorCode`**:
- `PRODUCT_NOT_FOUND`, `CATEGORY_NOT_FOUND`, `SUPPLIER_NOT_FOUND`
- `INSUFFICIENT_STOCK`, `INVENTORY_NOT_FOUND`
- `ORDER_ALREADY_COMPLETED`, `INVALID_ORDER_STATE`
- `DEPOSIT_EXCEEDS_BALANCE`
- `CREDIT_LIMIT_EXCEEDED`
- `TILL_ALREADY_OPEN`, `TILL_NOT_OPEN`, `INVALID_TILL_STATE`
- `TABLE_ALREADY_OCCUPIED`, `TABLE_NOT_FOUND`
- `PURCHASE_ORDER_NOT_FOUND`, `INVALID_PURCHASE_ORDER_STATE`
- `CUSTOMER_RETURN_NOT_FOUND`, `INVALID_RETURN_STATE`

## 4. Commands & Use Cases

### Product & Catalog
- `CreateProductCommand(orgId, code, name, categoryId, isTaxable, isService)` → `CreateProductUseCase`
- `UpdateProductCommand(...)` → `UpdateProductUseCase`
- `DeleteProductCommand(productId)` → `DeleteProductUseCase`
- `SetProductPriceCommand(productId, priceLevel, costPrice, sellingPrice)` → `SetProductPriceUseCase`
- `SetCustomerPriceOverrideCommand(productId, customerId, price, effectiveFrom, effectiveTo)` → `SetCustomerPriceOverrideUseCase`
- `CreateDiscountCommand(orgId, name, type, value, scope)` → `CreateDiscountUseCase`
- `CreateSupplierCommand(orgId, name, email, phone)` → `CreateSupplierUseCase`

### Purchase Orders
- `CreatePurchaseOrderCommand(orgId, outletId, supplierId, items)` → `CreatePurchaseOrderUseCase`
- `SendPurchaseOrderCommand(purchaseOrderId)` → `SendPurchaseOrderUseCase`
- `ReceivePurchaseOrderCommand(purchaseOrderId, receivedItems)` → `ReceivePurchaseOrderUseCase` (Triggers inventory `addStock()` for each received item.)

### POS / Storefront
- `ProcessPosCheckoutCommand(outletId, tillId, customerId, type, items, discountId, paymentMethod)` → `ProcessPosCheckoutUseCase` (Reserves stock → processes payment → completes order.)
- `ProcessCreditSaleCommand(outletId, tillId, customerId, items)` → `ProcessCreditSaleUseCase` (Checks `CustomerCredit.isWithinLimit()`, creates order, adds debt.)
- `RefundPosSaleCommand(salesOrderId, reason, refundMethod)` → `RefundPosSaleUseCase`
- `ApproveCustomerReturnCommand(returnId)` → `ApproveCustomerReturnUseCase`
- `CreateLayawayCommand(salesOrderId, depositAmount)` → `CreateLayawayUseCase`
- `CreateProformaCommand(outletId, customerId, items)` → `CreateProformaUseCase`
- `OpenTillCommand(outletId, userId, openingFloat)` → `OpenTillUseCase`
- `CloseTillCommand(tillId, userId, actualBalance)` → `CloseTillUseCase`
- `OccupyTableCommand(tableId, covers, waiterId)` → `OccupyTableUseCase`
- `SendKitchenOrderCommand(salesOrderId, tableId, items)` → `SendKitchenOrderUseCase` (Creates KOT, publishes to kitchen display.)
- `MarkKotReadyCommand(kotId)` → `MarkKotReadyUseCase`

### Inventory
- `AdjustStockCommand(inventoryId, newQty, reason)` → `AdjustStockUseCase`
- `InitiateStockCountCommand(outletId)` → `InitiateStockCountUseCase`
- `ReconcileStockCountCommand(countId, List<CountedItem>)` → `ReconcileStockCountUseCase`
- `ProcessSupplyRefundCommand(supplierId, outletId, productId, qty)` → `ProcessSupplyRefundUseCase`

## 5. Queries
- `ListProductsQuery(orgId, filters)`, `GetProductDetailsQuery(productId)`
- `SearchProductByBarcodeQuery(orgId, barcode)` → Looks up `Product` by `code` field. Used for barcode/QR scanning at POS.
- `ListCategoriesQuery(orgId)`, `ListDepartmentsQuery(orgId)`, `ListSuppliersQuery(orgId)`
- `GetInventoryLevelQuery(outletId, productId)`
- `GetStockSummaryQuery(outletId)` (Returns overall stock value, low-stock alerts, reorder suggestions.)
- `ListLowStockProductsQuery(orgId, outletId)` → Products at or below `reorderLevel`.
- `ListPosTransactionsQuery(outletId, dateFrom, dateTo, tillId)`
- `GetSalesSummaryReportQuery(orgId, dateFrom, dateTo, groupBy)` (Cashier, outlet, product, category.)
- `GetTransactionDetailsQuery(salesOrderId)`
- `GetTillSummaryQuery(tillId)` (Cash movements, expected vs actual balance.)
- `ListCustomerCreditQuery(orgId, customerId)`
- `ListPurchaseOrdersQuery(orgId, status, supplierId)`
- `ListActiveTablesQuery(outletId)`

## 6. Listeners
- `ShipmentDeliveredListener`: Listens to Logistics `ShipmentDeliveredEvent` to automatically call `addStock()` in `Inventory`.
- `PurchaseOrderReceivedListener`: Listens internally to `PurchaseOrderReceivedEvent` to update `Inventory` stock levels.
- `PaymentSuccessfulListener`: Listens to `PaymentSuccessfulEvent` from `atlashub-pay` to mark `SalesOrder` as COMPLETED.
- `PaymentFailedListener`: Listens to `PaymentFailedEvent` to trigger `releaseReservedStock()` compensation.

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Pessimistic Locking (`@Lock(PESSIMISTIC_WRITE)`)**: **CRITICAL** for `Inventory` during `reserveStock()`, `deductStock()`, and `addStock()` to prevent overselling or double-counting.
- **Pessimistic Locking**: Also applied to `CustomerCredit` during credit sales to prevent exceeding credit limits under concurrent requests.
- **Optimistic Locking (`@Version`)**: Applied to `Product`, `SalesOrder`, `Till`, and `HospitalityTable`.

### Sagas & Compensation (Stock Reservation)
- **Checkout Saga**:
  1. Create `SalesOrder` (PENDING).
  2. Reserve stock via `Inventory.reserveStock()` (Pessimistic Lock).
  3. Publish `OrderPaymentRequestedEvent` → `atlashub-pay`.
  4. **Compensation**: If `PaymentFailedEvent` received, call `Inventory.releaseReservedStock()` and mark order FAILED.

### Inbox & Outbox Patterns
- **Outbox**: Reliably publishes `PosSaleCompletedEvent` and `PurchaseOrderSentEvent`.
- **Inbox (`EventDeliveryTracker`)**: Idempotent processing of `PaymentSuccessfulEvent` and `ShipmentDeliveredEvent`.
