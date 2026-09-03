# Commerce & POS Module Design (`atlashub-commerce`)

## Role & Purpose

The Commerce module is the **retail and sales engine** of the AtlasHub platform. It enables organizations to run physical stores, hospitality venues (restaurants, bars), and wholesale operations. Everything that happens between a product arriving in a warehouse and money arriving in the organization's account is orchestrated here.

This module is not an e-commerce platform — it is a **Point of Sale (POS) and inventory management system** designed for brick-and-mortar businesses operating multiple outlets. An organization running five stores across Lagos will use Commerce to manage stock at each outlet, process sales at the till, track credit customers, manage kitchen orders for their restaurant, and generate daily sales reports.

Commerce is deeply connected to the rest of the platform:
- It reads from `pay` to confirm payment before completing a sale.
- It publishes events that `logistics` acts on (dispatch a shipment after an online order).
- It pushes sales events to `accounting` for automatic journal entries.
- It checks the organization's subscription status in `billing` before allowing POS access.

---

## 1. Features

### Product Catalog (Commerce-Specific)
Organizations manage their own product catalog (distinct from AtlasHub's platform product catalog in `atlashub-platform:catalog`). Each commerce `Product` has a SKU/barcode, pricing levels (RETAIL, WHOLESALE), and can have variants. Products are organized by categories, departments, and manufacturers.

### Multi-Outlet Inventory Management
Stock is tracked per outlet via `Inventory` aggregates. Each `(outletId, productId)` pair has a quantity, a reorder level, and a safe stock level. The system automatically flags products that fall below reorder levels. Inventory changes happen through:
- **POS Sales**: Stock reserved at checkout, deducted on payment confirmation.
- **Purchase Order Receipt**: Stock added when a supplier delivery is received.
- **Stock Adjustments**: Manual corrections for damage, theft, expiry.
- **Stock Counts**: Periodic physical counts reconciled against system records.
- **Incoming Stock Transfers**: From logistics (inter-outlet transfers arriving).

### Point of Sale (POS)
The storefront submodule handles the full POS transaction lifecycle:
1. Cashier opens the till (`OpenTillUseCase` — creates a `Till` record with opening float, posts a ledger entry).
2. Items are added to a `SalesOrder`.
3. Discounts are applied (order-level or item-level).
4. Payment is collected — cash, card, credit, or layaway.
5. On card/transfer payment: `InitializePaymentUseCase` (in `pay`) is called synchronously. The sale waits for `PaymentSuccessfulEvent` before completing.
6. Stock is deducted from inventory.
7. `PosSaleCompletedEvent` is published — consumed by accounting for journal entry and by notifications for receipt dispatch.
8. Till is closed at end of day (`CloseTillUseCase`) — balances the till, posts `TillClosedEvent`, swept to operating account via `ReconcileTillCommand` in pay.

### Hospitality (Restaurant/Bar)
Organizations running hospitality venues use table management alongside POS:
- **Tables**: `HospitalityTable` tracks covers, status (AVAILABLE, OCCUPIED, RESERVED, BILL_REQUESTED).
- **Kitchen Order Tickets (KOT)**: When food/drink items are ordered, a `KitchenOrderTicket` is created and sent to a kitchen display (via `SendKitchenOrderUseCase`). The kitchen marks items READY when prepared.

### Credit Sales & Layaway
- **Credit Sales**: Organizations can sell to customers on credit. `CustomerCredit` tracks their total outstanding debt against a credit limit. `isWithinLimit()` is checked before completing a credit sale.
- **Layaway**: A customer can make a partial deposit on a large purchase. `CustomerDeposit` tracks the deposit, remaining balance, and can be recalled or fulfilled.

### Supplier & Purchase Orders
Organizations raise `PurchaseOrder`s to suppliers to replenish stock. The order lifecycle: DRAFT → SENT → PARTIALLY_RECEIVED → RECEIVED. When received, stock is automatically added to inventory via `Inventory.addStock()`.

### Returns
- **Customer Returns**: A customer returning a product triggers `CustomerReturn`. On approval, a refund is issued via `pay` (CASH, CREDIT_NOTE, or WALLET credit) and stock is restored.
- **Supplier Returns (Return Outwards)**: Defective goods sent back to supplier tracked via `SupplyRefund`.

### Stock Transfers
When goods need to move between outlets, Commerce raises a `StockTransfer` request. The `logistics` module handles the physical shipment. On delivery confirmation, `StockTransferReceivedEvent` triggers stock adjustment at both source and destination outlets, and `accounting` posts the inventory movement journal entry.

---

## 2. Domain Entities & Aggregates

### `catalog` Submodule

**`Product` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `code` (SKU/barcode), `name`, `description`, `categoryId`, `departmentId`, `manufacturerId`, `isTaxable`: Boolean, `isService`: Boolean, `status` (ACTIVE, INACTIVE, DELETED), `hasVariants`: Boolean
- **Methods**: `updateDetails(...)`, `markAsDeleted()`

**`ProductPrice` (Entity)**
- **Fields**: `id`, `productId`, `priceLevel` (RETAIL, WHOLESALE), `costPrice`: `Money`, `sellingPrice`: `Money`, `markup`: BigDecimal

**`CustomerPriceOverride` (Entity)**
Per-customer pricing overrides for specific accounts (loyal clients, corporate accounts).
- **Fields**: `id`, `productId`, `customerId`, `organizationId`, `price`: `Money`, `effectiveFrom`: LocalDate, `effectiveTo`: LocalDate (nullable)

**`Discount` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `name`, `type` (PERCENTAGE, FLAT_AMOUNT), `value`: BigDecimal, `scope` (ORDER_LEVEL, ITEM_LEVEL), `isActive`: Boolean

**`Supplier` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `name`, `email`, `phone`, `address`, `status` (ACTIVE, INACTIVE)

**`PurchaseOrder` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `outletId`, `supplierId`, `status` (DRAFT, SENT, PARTIALLY_RECEIVED, RECEIVED, CANCELLED), `totalAmount`: `Money`, `expectedDeliveryDate`: LocalDate
- **Methods**: `addItem(PurchaseOrderItem)`, `send()`, `receive(List<ReceivedItem>)`, `cancel()`

### `storefront` Submodule

**`SalesOrder` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `outletId`, `customerId`, `cashierId`, `tillId`, `totalGross`: `Money`, `totalDiscount`: `Money`, `totalTax`: `Money`, `totalNet`: `Money`, `type` (RETAIL, WHOLESALE, HOSPITALITY), `status` (PENDING, PAYMENT_PENDING, COMPLETED, REFUNDED, LAYAWAY, PROFORMA, FAILED), `saleDate`: ZonedDateTime
- **Methods**: `addItem(SalesOrderItem)`, `applyDiscount(Discount)`, `applyItemDiscount(Long itemId, Discount)`, `complete()`, `refund(String reason)`, `convertToLayaway()`, `convertToProforma()`

**`CustomerDeposit` / Layaway (Aggregate Root)**
- **Fields**: `id`, `salesOrderId`, `amountPaid`: `Money`, `balanceRemaining`: `Money`, `status` (ACTIVE, RECALLED, FULFILLED)
- **Methods**: `addPayment(Money amount)`, `recall()`, `fulfil()`

**`CustomerCredit` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `customerId`, `totalDebt`: `Money`, `creditLimit`: `Money`, `status` (WITHIN_LIMIT, OVER_LIMIT, SETTLED)
- **Methods**: `addDebt(Money amount)`, `recordPayment(Money amount)`, `isWithinLimit(Money newDebt): Boolean`

**`Till` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `outletId`, `name`, `openingFloat`: `Money`, `expectedClosingBalance`: `Money`, `actualClosingBalance`: `Money`, `status` (OPEN, CLOSED), `openedAt`, `closedAt`, `openedBy`, `closedBy`
- **Methods**: `open(Long userId, Money float)`, `close(Long userId, Money actualBalance)`, `recordVariance()`

**`HospitalityTable` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `outletId`, `tableNumber`: String, `covers`: Integer, `status` (AVAILABLE, OCCUPIED, RESERVED, BILL_REQUESTED)
- **Methods**: `occupy(Integer covers, Long waiterId)`, `free()`, `requestBill()`

**`KitchenOrderTicket` (Aggregate Root)**
- **Fields**: `id`, `salesOrderId`, `tableId`, `items`: `List<KotItem>`, `status` (PENDING, IN_PROGRESS, READY, SERVED), `sentAt`: ZonedDateTime
- **Methods**: `markInProgress()`, `markReady()`, `markServed()`

### `inventory` Submodule

**`Inventory` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `outletId`, `productId`, `quantity`, `reorderLevel`, `safeStock`
- **Methods**: `addStock(Integer qty)`, `deductStock(Integer qty)`, `reserveStock(Integer qty)`, `releaseReservedStock(Integer qty)`

**`StockAdjustment` (Aggregate Root)**
- **Fields**: `id`, `inventoryId`, `adjustedBy`, `previousQty`, `newQty`, `reason` (DAMAGE, EXPIRY, THEFT, CORRECTION)

**`StockCount` (Aggregate Root)**
- **Fields**: `id`, `outletId`, `status` (OPEN, RECONCILED), `countedItems`: `List<StockCountItem>`, `startedAt`, `reconciledAt`

**`StockTransfer` (Aggregate Root)**
Requests to move stock between outlets. The logistics module handles the physical movement.
- **Fields**: `id`, `organizationId`, `sourceOutletId`, `destinationOutletId`, `status` (REQUESTED, APPROVED, DISPATCHED, RECEIVED, CANCELLED), `requestedAt`, `receivedAt`
- **Methods**: `addTransferItem(...)`, `approve()`, `dispatch()`, `receive(Long receiverId, List<ReceivedItem>)`, `cancel()`
- **On receipt**: `addStock()` is called for the destination outlet inventory, `deductStock()` for the source. An `accounting` journal entry posts:
  ```
  DEBIT  Inventory Asset (Outlet B)   ₦X (cost value)
  CREDIT Inventory Asset (Outlet A)   ₦X
  ```

**`CustomerReturn` (Aggregate Root)**
- **Fields**: `id`, `salesOrderId`, `organizationId`, `outletId`, `customerId`, `productId`, `quantity`, `refundAmount`: `Money`, `reason`: String, `refundMethod` (CASH, CREDIT_NOTE, WALLET), `status` (PENDING, APPROVED, REFUNDED)
- **Methods**: `approve()`, `processRefund()`

**`SupplyRefund` / Return Outwards (Aggregate Root)**
- **Fields**: `id`, `supplierId`, `outletId`, `productId`, `quantity`, `refundValue`: `Money`, `status` (PENDING, DISPATCHED, CONFIRMED, FAILED)

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `ProductCreatedEvent` | New product added | `inventory` (initialize stock records) |
| `PurchaseOrderSentEvent` | PO sent to supplier | `notifications` (email supplier), `logistics` (optionally schedule delivery) |
| `PurchaseOrderReceivedEvent` | Goods received from supplier | `inventory` (add stock), `accounting` (Debit Inventory, Credit AP) |
| `PosSaleCompletedEvent` | Sale completed successfully | `accounting` (Debit Cash/Receivable, Credit Revenue), `notifications` (receipt) |
| `PosSaleRefundedEvent` | Sale refunded | `accounting` (reverse entry), `pay` (initiate refund payout) |
| `TillOpenedEvent` | Till opened for the day | `accounting`, `pay` (post opening float ledger entry) |
| `TillClosedEvent` | Till closed at end of day | `accounting`, `pay` (post TillClosed → reconcile to operating account) |
| `StockAdjustedEvent` | Stock manually adjusted | `accounting` (Debit Loss/Gain, Credit/Debit Inventory) |
| `StockTransferRequestedEvent` | Transfer initiated | `logistics` (create shipment for delivery if inter-city) |
| `StockTransferReceivedEvent` | Transfer received at destination | `inventory` (update stock levels), `accounting` (post inventory movement entry), `pay` (post inter-outlet ledger transaction) |
| `CustomerReturnApprovedEvent` | Return approved | `pay` (initiate refund), `inventory` (restore stock), `accounting` (reverse sale entry) |
| `LayawayCreatedEvent` | Layaway deposit taken | `accounting` (Debit Cash, Credit Customer Deposit Liability) |

---

## 4. Exceptions & Errors

**`CommerceErrorCode`**:
- `PRODUCT_NOT_FOUND`, `CATEGORY_NOT_FOUND`, `SUPPLIER_NOT_FOUND`
- `INSUFFICIENT_STOCK`, `INVENTORY_NOT_FOUND`
- `ORDER_ALREADY_COMPLETED`, `INVALID_ORDER_STATE`
- `DEPOSIT_EXCEEDS_BALANCE`, `CREDIT_LIMIT_EXCEEDED`
- `TILL_ALREADY_OPEN`, `TILL_NOT_OPEN`, `INVALID_TILL_STATE`
- `TABLE_ALREADY_OCCUPIED`, `TABLE_NOT_FOUND`
- `PURCHASE_ORDER_NOT_FOUND`, `INVALID_PURCHASE_ORDER_STATE`
- `CUSTOMER_RETURN_NOT_FOUND`, `INVALID_RETURN_STATE`

---

## 5. Commands & Use Cases

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
- `ReceivePurchaseOrderCommand(purchaseOrderId, receivedItems)` → `ReceivePurchaseOrderUseCase`

### POS / Storefront
- `ProcessPosCheckoutCommand(outletId, tillId, customerId, type, items, discountId, paymentMethod)` → `ProcessPosCheckoutUseCase`
  Flow: Reserve stock → Call `InitializePaymentUseCase` if card/transfer → await `PaymentSuccessfulEvent` → complete sale → deduct stock → `PosSaleCompletedEvent`.
- `ProcessCreditSaleCommand(outletId, tillId, customerId, items)` → `ProcessCreditSaleUseCase`
  Checks `CustomerCredit.isWithinLimit()`, creates order, adds to customer's outstanding debt.
- `RefundPosSaleCommand(salesOrderId, reason, refundMethod)` → `RefundPosSaleUseCase`
- `ApproveCustomerReturnCommand(returnId)` → `ApproveCustomerReturnUseCase`
- `CreateLayawayCommand(salesOrderId, depositAmount)` → `CreateLayawayUseCase`
- `CreateProformaCommand(outletId, customerId, items)` → `CreateProformaUseCase`
- `OpenTillCommand(outletId, userId, openingFloat)` → `OpenTillUseCase`
- `CloseTillCommand(tillId, userId, actualBalance)` → `CloseTillUseCase`
  Posts `TillClosedEvent` → triggers `ReconcileTillCommand` in `pay` to sweep till balance to operating account.
- `OccupyTableCommand(tableId, covers, waiterId)` → `OccupyTableUseCase`
- `SendKitchenOrderCommand(salesOrderId, tableId, items)` → `SendKitchenOrderUseCase`
- `MarkKotReadyCommand(kotId)` → `MarkKotReadyUseCase`

### Inventory
- `AdjustStockCommand(inventoryId, newQty, reason)` → `AdjustStockUseCase`
- `InitiateStockCountCommand(outletId)` → `InitiateStockCountUseCase`
- `ReconcileStockCountCommand(countId, List<CountedItem>)` → `ReconcileStockCountUseCase`
- `InitiateStockTransferCommand(orgId, sourceId, destId, items)` → `InitiateStockTransferUseCase`
- `ReceiveStockTransferCommand(transferId, receivedItems)` → `ReceiveStockTransferUseCase`
  Calls `addStock()` for destination, `deductStock()` for source, publishes `StockTransferReceivedEvent`.

---

## 6. Queries

- `ListProductsQuery(orgId, filters)`, `GetProductDetailsQuery(productId)`
- `SearchProductByBarcodeQuery(orgId, barcode)` — barcode/QR scanner at POS
- `ListCategoriesQuery(orgId)`, `ListSuppliersQuery(orgId)`
- `GetInventoryLevelQuery(outletId, productId)`
- `GetStockSummaryQuery(outletId)` — stock value, low-stock alerts, reorder suggestions
- `ListLowStockProductsQuery(orgId, outletId)` — products at or below reorderLevel
- `ListPosTransactionsQuery(outletId, dateFrom, dateTo, tillId)`
- `GetSalesSummaryReportQuery(orgId, dateFrom, dateTo, groupBy)` — by cashier, outlet, product, category
- `GetTillSummaryQuery(tillId)` — expected vs actual balance
- `ListCustomerCreditQuery(orgId, customerId)`
- `ListPurchaseOrdersQuery(orgId, status, supplierId)`
- `ListActiveTablesQuery(outletId)`
- `ListStockTransfersQuery(orgId, direction, status)`

---

## 7. Listeners

- **`ShipmentDeliveredListener`**: Listens to `ShipmentDeliveredEvent` (from `logistics`). Calls `ReceiveStockTransferUseCase` to add stock at the destination outlet.
- **`PaymentSuccessfulListener`**: Listens to `PaymentSuccessfulEvent` (from `pay`). Marks `SalesOrder` as COMPLETED, deducts stock from reserved to sold, publishes `PosSaleCompletedEvent`.
- **`PaymentFailedListener`**: Listens to `PaymentFailedEvent` (from `pay`). Calls `Inventory.releaseReservedStock()` compensation and marks the order FAILED.
- **`CustomerReturnShipmentReceivedListener`**: Listens to `CustomerReturnShipmentReceivedEvent` (from `logistics`). Triggers `ApproveCustomerReturnUseCase` to process the refund and restore stock.

---

## 8. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Pessimistic Locking (`@Lock(PESSIMISTIC_WRITE)`)**: **CRITICAL** for `Inventory` during `reserveStock()`, `deductStock()`, and `addStock()` to prevent overselling or double-counting in concurrent scenarios.
- **Pessimistic Locking**: Applied to `CustomerCredit` during credit sales to prevent exceeding credit limits.
- **Optimistic Locking (`@Version`)**: Applied to `Product`, `SalesOrder`, `Till`, and `HospitalityTable`.

### Checkout Saga (Stock Reservation)
1. Create `SalesOrder` (PENDING).
2. `Inventory.reserveStock()` (Pessimistic Lock) — stock is held.
3. Publish `OrderPaymentRequestedEvent` → `atlashub-pay` initializes payment.
4. **Success path**: `PaymentSuccessfulEvent` → `deductStock()` → `SalesOrder.complete()` → `PosSaleCompletedEvent`.
5. **Failure path**: `PaymentFailedEvent` → `Inventory.releaseReservedStock()` → `SalesOrder.fail()`.

### Inbox & Outbox
- **Outbox**: Reliably publishes `PosSaleCompletedEvent` and `PurchaseOrderSentEvent`.
- **Inbox**: Idempotent processing of `PaymentSuccessfulEvent` and `ShipmentDeliveredEvent` to prevent double stock movements from duplicate event deliveries.
