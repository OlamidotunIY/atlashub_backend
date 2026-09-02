# Commerce & POS Module Design (`atlashub-commerce`)

## 1. Domain Entities & Aggregates

### `catalog` Submodule
**`Product` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `code`, `name`, `description`, `categoryId`, `departmentId`, `manufacturerId`, `isTaxable`, `isService`, `status`
- **Methods**: `updateDetails(...)`, `markAsDeleted()`
**`ProductPrice` (Entity)**
- **Fields**: `id`, `productId`, `priceLevel` (RETAIL, WHOLESALE), `costPrice`, `sellingPrice`, `markup`
**`Category`, `Department`, `Manufacturer` (Entities)**
- **Fields**: `id`, `organizationId`, `name`, `description`

### `storefront` (POS) Submodule
**`SalesOrder` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `outletId`, `customerId`, `totalGross`: **`Money`**, `totalDiscount`: **`Money`**, `totalTax`: **`Money`**, `totalNet`: **`Money`**, `type` (RETAIL, WHOLESALE, HOSPITALITY), `status` (PENDING, PAYMENT_PENDING, COMPLETED, REFUNDED, LAYAWAY, PROFORMA, FAILED)
- **Methods**: `addItem(SalesOrderItem)`, `applyDiscount(Money amount)`, `complete()`, `refund()`, `convertToLayaway()`
**`SalesOrderItem` (Entity)**
- **Fields**: `id`, `salesOrderId`, `productId`, `quantity`, `unitPrice`: **`Money`**, `taxAmount`: **`Money`**, `subtotal`: **`Money`**
**`CustomerDeposit` / Layaway (Aggregate Root)**
- **Fields**: `id`, `salesOrderId`, `amountPaid`: **`Money`**, `balanceRemaining`: **`Money`**, `status` (ACTIVE, RECALLED, FULFILLED)

### `inventory` Submodule
**`Inventory` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `outletId`, `productId`, `quantity`, `reorderLevel`, `safeStock`
- **Methods**: `addStock(Integer qty)`, `deductStock(Integer qty)`, `reserveStock(Integer qty)`, `releaseReservedStock(Integer qty)`
**`StockAdjustment` (Aggregate Root)**
- **Fields**: `id`, `inventoryId`, `adjustedBy`, `previousQty`, `newQty`, `reason`
**`StockCount` (Aggregate Root)**
- **Fields**: `id`, `outletId`, `status` (OPEN, RECONCILED), `countedItems` (List of count sheets)
**`SupplyRefund` / Return Outwards (Aggregate Root)**
- **Fields**: `id`, `supplierId`, `outletId`, `productId`, `quantity`, `refundValue`: **`Money`**, `status`

## 2. Domain Events
- `ProductCreatedEvent`, `ProductPriceUpdatedEvent`
- `PosSaleCompletedEvent(Long salesOrderId, Long outletId, Money totalNet)`
- `PosSaleRefundedEvent(Long salesOrderId)`
- `LayawayCreatedEvent(Long depositId, Money amount)`
- `StockAdjustedEvent(Long inventoryId, Integer difference, String reason)`
- `StockCountReconciledEvent(Long countId)`
- `SupplyRefundProcessedEvent(Long refundId)`

## 3. Exceptions & Errors
**`CommerceErrorCode`**:
- `PRODUCT_NOT_FOUND`, `CATEGORY_NOT_FOUND`
- `INSUFFICIENT_STOCK`, `INVENTORY_NOT_FOUND`
- `ORDER_ALREADY_COMPLETED`, `INVALID_ORDER_STATE`
- `DEPOSIT_EXCEEDS_BALANCE`

## 4. Commands & Use Cases
- `CreateProductCommand`, `UpdateProductCommand`, `DeleteProductCommand` -> `...UseCase`
- `SetProductPriceCommand(productId, priceLevel, cost, selling)`
- `ProcessPosCheckoutCommand(outletId, customerId, type, items, paymentMethod)` -> `ProcessPosCheckoutUseCase` (Deducts stock, creates SalesOrder, marks completed).
- `RefundPosSaleCommand(salesOrderId, reason)`
- `CreateLayawayCommand(salesOrderId, depositAmount)` -> `CreateLayawayUseCase`
- `AdjustStockCommand(inventoryId, newQty, reason)` -> `AdjustStockUseCase`
- `InitiateStockCountCommand(outletId)` -> `InitiateStockCountUseCase`
- `ReconcileStockCountCommand(countId, List<CountedItem>)`
- `ProcessSupplyRefundCommand(supplierId, outletId, productId, qty)`

## 5. Queries
- `ListProductsQuery(orgId, filters)`, `GetProductDetailsQuery`
- `ListCategoriesQuery`, `ListDepartmentsQuery`
- `GetInventoryLevelQuery(outletId, productId)`
- `GetStockSummaryQuery(outletId)` (Returns overall stock value, low stock alerts)
- `ListPosTransactionsQuery(outletId, dateFrom, dateTo)`
- `GetSalesSummaryReportQuery(orgId, dateFrom, dateTo, groupBy)`
- `GetTransactionDetailsQuery(salesOrderId)`

## 6. Listeners
- `ShipmentDeliveredListener`: Listens to Logistics to automatically update `Inventory` (addStock).

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Pessimistic Locking (@Lock(PESSIMISTIC_WRITE))**: **CRITICAL** for Inventory. When djustStock is called during a checkout, the DB row is locked to prevent overselling.
- **Optimistic Locking (@Version)**: Applied to Product and SalesOrder.

### Sagas & Compensation (Stock Reservation)
- **Checkout Saga**: 
  1. Create SalesOrder (PENDING).
  2. Reserve stock in Inventory (Requires Pessimistic Lock).
  3. Call tlashub-pay.
  4. **Compensation**: If payment fails, release the reserved stock and mark the order as FAILED.

### Inbox & Outbox Patterns
- **Outbox**: Reliably publishes PosSaleCompletedEvent.
- **Inbox (EventDeliveryTracker)**: Idempotent processing of external PaymentSuccessfulEvent.
