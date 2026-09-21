# Commerce Module Design (`atlashub-commerce`)

## Role & Purpose

The Commerce module is the **retail and marketplace engine** of AtlasHub. It enables organizations to run physical stores, hospitality venues (restaurants, bars), and multi-vendor marketplaces.

Commerce operates in two distinct modes:

1. **Standalone POS Mode** — A business manages its own stores, inventory, and sales. This is the core use case for a retailer running multiple outlets.
2. **Marketplace Mode** — A business operates as a platform connecting buyers to multiple vendors. Vendors list their products; the marketplace owner earns a commission on each sale.

Both modes share the same underlying domain model. The `Storefront` configuration on an organization determines which mode is active.

Commerce is deeply connected to the rest of the platform:
- It delegates all money movement to `atlashub-pay`
- It publishes events that `atlashub-logistics` acts on (dispatch a delivery after an online order)
- It pushes events to `atlashub-accounting` for automatic journal entries
- It checks entitlement via `billing:EntitlementQueryPort` before allowing access

---

## 1. Features

### Product Catalog (Commerce-Owned)
Organizations manage their own product catalog (completely separate from AtlasHub's platform catalog in `atlashub-platform:catalog`). Each `Product` has a SKU/barcode, pricing levels (RETAIL, WHOLESALE), optional variants (size, color), and tax configuration.

### Multi-Outlet Inventory
Stock is tracked per outlet via `Inventory` aggregates. Each `(outletId, productId)` pair tracks quantity, reorder level, and safe stock level. Low-stock alerts are generated automatically. Inventory changes occur through:
- **POS Sales**: Stock reserved at checkout, deducted on payment confirmation
- **Purchase Order Receipt**: Stock added when a supplier delivery arrives
- **Stock Adjustments**: Manual corrections (damage, theft, expiry)
- **Stock Counts**: Periodic physical counts
- **Incoming Stock Transfers**: From logistics

### Point of Sale (POS)
Full POS transaction lifecycle:
1. Cashier opens the till (`OpenTillUseCase` — creates a `Till`, posts opening float ledger entry via Pay)
2. Items added to `SalesOrder`
3. Discounts applied (order-level or item-level)
4. Payment collected: cash (recorded immediately), card/transfer (triggers Pay checkout → awaits `ChargeSuccessfulEvent`)
5. Stock deducted from inventory
6. `PosSaleCompletedEvent` published → accounting journal entry + receipt notification
7. End of day: till closed → `TillClosedEvent` → Pay sweeps till balance to Operating Account

### Hospitality (Restaurant/Bar)
- **Tables**: `HospitalityTable` tracks covers and status (AVAILABLE, OCCUPIED, RESERVED, BILL_REQUESTED)
- **Kitchen Order Tickets (KOT)**: When items are ordered, a `KitchenOrderTicket` is created and pushed to the kitchen display in real time via WebSocket. Kitchen marks items READY when prepared.

### Marketplace & Vendors
In marketplace mode:
- **Vendors** register on the platform, have their own product listings and sub-inventory
- **Vendor Products**: Products are linked to a `vendorId` — the marketplace owner can approve/reject vendor product listings
- **Fund Routing**: On a marketplace sale, `pay` applies the configured `SplitRule` — AtlasHub fee deducted first, then business (marketplace) commission, then vendor share credited to Split Holding Account for disbursement
- **Vendor Payout**: Vendors receive their earnings via `Payout` records in `pay`, on a configurable disbursement schedule (daily, weekly, on-demand)

### Online Storefront
Organizations can configure an online store that is accessible to end-customers outside the organization:
- Publicly accessible product catalogue (hosted on a subdomain or embedded via API)
- Online order creation
- Payment via AtlasHub Pay (card/bank transfer)
- Integration with Logistics for delivery

### Credit Sales & Layaway
- **Credit Sales**: Sell to trusted customers on account. `CustomerCredit` enforces a credit limit.
- **Layaway**: Partial deposit on a large purchase. Balance collected over time. Fulfilled when fully paid.

### Supplier & Purchase Orders
Raise `PurchaseOrder`s to suppliers. Lifecycle: DRAFT → SENT → PARTIALLY_RECEIVED → RECEIVED. On receipt, stock is automatically added via `Inventory.addStock()`.

### Returns
- **Customer Returns**: Triggers refund via `pay` and stock restoration
- **Supplier Returns (Return Outwards)**: Defective goods sent back to supplier

---

## 2. Domain Entities & Aggregates

### `catalog` Submodule

**`Product` (Aggregate Root)**
```
Product
├── id: Long
├── organizationId: Long
├── vendorId: Long                    ← nullable — set for marketplace vendor products
├── code: String                      ← SKU or barcode
├── name: String
├── description: String
├── categoryId: Long
├── departmentId: Long                ← nullable
├── manufacturerId: Long              ← nullable
├── isTaxable: Boolean
├── isService: Boolean                ← services don't have inventory
├── hasVariants: Boolean
├── status: ProductStatus             ← ACTIVE, INACTIVE, PENDING_APPROVAL, DELETED
└── type: ProductType                 ← PHYSICAL, DIGITAL, SERVICE
```

**Business Methods:**
- `approve()` → marketplace admin approves vendor product listing
- `reject(String reason)` → marketplace admin rejects vendor product
- `markAsDeleted()`

**`ProductVariant` (Entity)**: `id`, `productId`, `sku`, `attributes: Map<String, String>` (e.g., `{size: "XL", color: "Red"}`), `isActive`

**`ProductPrice` (Entity)**
```
ProductPrice
├── id: Long
├── productId: Long
├── variantId: Long                   ← nullable, null means base product price
├── priceLevel: PriceLevel            ← RETAIL, WHOLESALE
├── costPrice: Money
├── sellingPrice: Money
└── markup: BigDecimal
```

**`CustomerPriceOverride` (Entity)**: Per-customer pricing for corporate accounts.

**`Discount` (Aggregate Root)**: `id`, `organizationId`, `name`, `type` (PERCENTAGE, FLAT_AMOUNT), `value`, `scope` (ORDER_LEVEL, ITEM_LEVEL), `minOrderAmount: Money`, `maxUses: Integer`, `usedCount: Integer`, `validFrom: LocalDate`, `validTo: LocalDate`, `isActive: Boolean`

**`Supplier` (Aggregate Root)**: `id`, `organizationId`, `name`, `email`, `phone`, `address`, `status` (ACTIVE, INACTIVE)

**`PurchaseOrder` (Aggregate Root)**
```
PurchaseOrder
├── id: Long
├── organizationId: Long
├── outletId: Long
├── supplierId: Long
├── status: PurchaseOrderStatus       ← DRAFT, SENT, PARTIALLY_RECEIVED, RECEIVED, CANCELLED
├── totalAmount: Money
├── expectedDeliveryDate: LocalDate
└── items: List<PurchaseOrderItem>
```

**Business Methods:** `addItem()`, `send()` → registers `PurchaseOrderSentEvent`, `receiveItems(List<ReceivedItem>)`, `cancel()`

**`Vendor` (Aggregate Root)** — marketplace mode
```
Vendor
├── id: Long
├── organizationId: Long              ← the marketplace that owns this vendor
├── userId: Long                      ← the vendor's platform user account
├── businessName: String
├── email: EmailAddress
├── phone: PhoneNumber
├── settlementBankCode: String
├── settlementAccountNumber: String
├── settlementAccountName: String
├── commissionRate: BigDecimal        ← the marketplace's cut percentage
├── disbursementSchedule: DisbursementSchedule ← DAILY, WEEKLY, ON_DEMAND
├── status: VendorStatus              ← PENDING, ACTIVE, SUSPENDED, TERMINATED
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `approve()` → registers `VendorApprovedEvent`
- `suspend(String reason)` → registers `VendorSuspendedEvent`
- `terminate()` → registers `VendorTerminatedEvent`

---

### `storefront` Submodule

**`SalesOrder` (Aggregate Root)**
```
SalesOrder
├── id: Long
├── organizationId: Long
├── outletId: Long
├── vendorId: Long                    ← nullable, set for marketplace orders
├── customerId: Long                  ← nullable
├── cashierId: Long
├── tillId: Long                      ← nullable for online orders
├── type: OrderType                   ← POS_RETAIL, POS_WHOLESALE, POS_HOSPITALITY, ONLINE, LAYAWAY, PROFORMA
├── status: OrderStatus               ← PENDING, PAYMENT_PENDING, COMPLETED, REFUNDED, FAILED, LAYAWAY, PROFORMA
├── items: List<SalesOrderItem>
├── discountId: Long                  ← nullable
├── totalGross: Money
├── totalDiscount: Money
├── totalTax: Money
├── totalNet: Money
├── paymentMethod: PaymentMethod      ← CASH, CARD, BANK_TRANSFER, USSD, CREDIT, LAYAWAY
├── chargeReference: String           ← pay module's charge reference, nullable
├── saleDate: ZonedDateTime
└── deliveryAddress: String           ← nullable, for online orders
```

**Business Methods:**
- `addItem(productId, variantId, qty, unitPrice)` — updates totals
- `applyDiscount(Discount discount)` — validates discount rules
- `completePayment()` → registers `PosSaleCompletedEvent`
- `failPayment(String reason)` → registers `PosSaleFailedEvent`
- `refund(String reason)` → registers `PosSaleRefundedEvent`
- `convertToLayaway(Money deposit)` → registers `LayawayCreatedEvent`

**`SalesOrderItem` (Entity)**: `id`, `salesOrderId`, `productId`, `variantId`, `quantity`, `unitPrice: Money`, `totalPrice: Money`, `taxAmount: Money`, `discountAmount: Money`

**`HospitalityTable` (Aggregate Root)**
```
HospitalityTable
├── id: Long
├── organizationId: Long
├── outletId: Long
├── tableNumber: String
├── covers: Integer
├── status: TableStatus               ← AVAILABLE, OCCUPIED, RESERVED, BILL_REQUESTED
└── currentOrderId: Long              ← nullable
```

**`KitchenOrderTicket` (Aggregate Root)**
```
KitchenOrderTicket
├── id: Long
├── salesOrderId: Long
├── tableId: Long
├── outletId: Long
├── items: List<KotItem>
├── status: KotStatus                 ← PENDING, IN_PROGRESS, READY, SERVED
└── sentAt: ZonedDateTime
```

**`Till` (Aggregate Root)**
```
Till
├── id: Long
├── organizationId: Long
├── outletId: Long
├── name: String
├── openingFloat: Money
├── expectedClosingBalance: Money
├── actualClosingBalance: Money       ← nullable until closed
├── status: TillStatus                ← OPEN, CLOSED
├── openedAt: ZonedDateTime
├── closedAt: ZonedDateTime           ← nullable
├── openedBy: Long
└── closedBy: Long                    ← nullable
```

**`CustomerCredit` (Aggregate Root)**
```
CustomerCredit
├── id: Long
├── organizationId: Long
├── customerId: Long
├── creditLimit: Money
├── outstandingDebt: Money
└── status: CreditStatus              ← WITHIN_LIMIT, OVER_LIMIT, SETTLED, BLOCKED
```

**`CustomerDeposit` / Layaway (Aggregate Root)**
```
CustomerDeposit
├── id: Long
├── salesOrderId: Long
├── amountPaid: Money
├── balanceRemaining: Money
└── status: DepositStatus             ← ACTIVE, RECALLED, FULFILLED
```

---

### `inventory` Submodule

**`Inventory` (Aggregate Root)**
```
Inventory
├── id: Long
├── organizationId: Long
├── outletId: Long
├── productId: Long
├── variantId: Long                   ← nullable
├── quantity: Integer
├── reservedQuantity: Integer         ← held during pending orders
├── reorderLevel: Integer
└── safeStock: Integer
```

**Business Methods:**
- `reserveStock(int qty)` — pessimistic lock required; throws INSUFFICIENT_STOCK if `quantity - reservedQuantity < qty`
- `releaseReservedStock(int qty)` — compensation on payment failure
- `deductStock(int qty)` — converts reserved to sold; called on payment success
- `addStock(int qty)` — called on PO receipt or stock transfer arrival
- `adjust(int newQty, AdjustmentReason reason)` — manual correction

**`StockAdjustment` (Aggregate Root)**: `id`, `inventoryId`, `adjustedBy`, `previousQty`, `newQty`, `reason: AdjustmentReason` (DAMAGE, EXPIRY, THEFT, CORRECTION, INITIAL_COUNT)

**`StockCount` (Aggregate Root)**: `id`, `outletId`, `status` (OPEN, RECONCILED), `countedItems: List<StockCountItem>`, `startedAt`, `reconciledAt`

**`StockTransfer` (Aggregate Root)**
```
StockTransfer
├── id: Long
├── organizationId: Long
├── sourceOutletId: Long
├── destinationOutletId: Long
├── status: TransferStatus            ← REQUESTED, APPROVED, DISPATCHED, RECEIVED, CANCELLED
├── items: List<StockTransferItem>
├── requestedAt: ZonedDateTime
└── receivedAt: ZonedDateTime         ← nullable
```

**`CustomerReturn` (Aggregate Root)**: `id`, `salesOrderId`, `organizationId`, `outletId`, `customerId`, `items: List<ReturnItem>`, `refundAmount: Money`, `reason`, `refundMethod: RefundMethod` (CASH, CREDIT_NOTE, WALLET), `status` (PENDING, APPROVED, REFUNDED, REJECTED)

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `ProductCreatedEvent` | Product added | `inventory` (init stock records per outlet) |
| `VendorApprovedEvent` | Vendor application approved | `notifications`, `pay` (create split rule for vendor) |
| `PurchaseOrderSentEvent` | PO sent to supplier | `notifications` (email supplier), `logistics` (schedule inbound shipment) |
| `PurchaseOrderReceivedEvent` | Goods received from supplier | `accounting` (Debit Inventory, Credit AP) |
| `PosSaleCompletedEvent` | Sale completed | `accounting` (Debit Cash/AR, Credit Revenue), `notifications` (receipt), `analytics` |
| `PosSaleFailedEvent` | Payment failed or timed out | Internal (release reserved stock) |
| `PosSaleRefundedEvent` | Sale refunded | `accounting` (reverse entry), `pay` (initiate refund payout) |
| `TillOpenedEvent` | Till opened | `pay` (post opening float ledger entry) |
| `TillClosedEvent` | Till closed | `pay` (sweep till balance to operating account), `accounting` |
| `StockAdjustedEvent` | Manual stock adjustment | `accounting` (Debit Loss/Gain, Credit/Debit Inventory) |
| `StockTransferRequestedEvent` | Transfer initiated | `logistics` (create shipment for inter-city transfers) |
| `StockTransferReceivedEvent` | Transfer arrived at destination | `inventory` (update both outlets), `pay` (post inter-outlet ledger entry), `accounting` |
| `CustomerReturnApprovedEvent` | Return approved | `pay` (initiate refund), `accounting` (reverse sale) |
| `KitchenOrderTicketCreatedEvent` | KOT created | WebSocket broadcast to kitchen display screen |
| `KotReadyEvent` | Kitchen marks order ready | WebSocket broadcast to waiter/cashier |
| `OnlineOrderCreatedEvent` | Online order placed | `logistics` (create shipment), `notifications` (confirm order to customer) |

---

## 4. Checkout Saga (Stock Reservation Pattern)

This is the most critical flow in Commerce — it ensures money and stock never go out of sync.

```
1. CreateSalesOrderUseCase  →  SalesOrder (PENDING)
2. ReserveStockUseCase      →  Inventory.reserveStock() [PESSIMISTIC LOCK]
3. InitializeChargeUseCase  →  pay creates Charge, returns checkoutUrl/ussdCode
4. Order transitions to PAYMENT_PENDING
5a. SUCCESS PATH:
    ChargeSuccessfulEvent received
    → Inventory.deductStock()  (convert reserved → sold)
    → SalesOrder.completePayment()
    → PosSaleCompletedEvent published
5b. FAILURE PATH:
    ChargeFailedEvent received (or 15-minute timeout)
    → Inventory.releaseReservedStock()  (compensation)
    → SalesOrder.failPayment()
    → PosSaleFailedEvent published
```

**Timeout Handling**: If `ChargeSuccessfulEvent` is not received within 15 minutes of `PAYMENT_PENDING`, a scheduled job triggers the failure path. This prevents inventory from being held indefinitely.

---

## 5. Exceptions & Errors

**`CommerceErrorCode`**:
- `PRODUCT_NOT_FOUND`, `CATEGORY_NOT_FOUND`, `SUPPLIER_NOT_FOUND`, `VENDOR_NOT_FOUND`
- `INSUFFICIENT_STOCK`, `INVENTORY_NOT_FOUND`
- `ORDER_ALREADY_COMPLETED`, `INVALID_ORDER_STATE`
- `CREDIT_LIMIT_EXCEEDED`, `CUSTOMER_CREDIT_BLOCKED`
- `TILL_ALREADY_OPEN`, `TILL_NOT_OPEN`, `INVALID_TILL_STATE`
- `TABLE_ALREADY_OCCUPIED`, `TABLE_NOT_FOUND`
- `PURCHASE_ORDER_NOT_FOUND`, `INVALID_PURCHASE_ORDER_STATE`
- `RETURN_NOT_FOUND`, `INVALID_RETURN_STATE`
- `VENDOR_SUSPENDED`, `PRODUCT_PENDING_APPROVAL`
- `DISCOUNT_EXPIRED`, `DISCOUNT_MAX_USES_REACHED`

---

## 6. Commands & Use Cases

### Catalog & Products
- `CreateProductCommand(orgId, vendorId, code, name, categoryId, isTaxable, type)` → `CreateProductUseCase`
- `ApproveVendorProductCommand(productId, approvedByUserId)` → `ApproveVendorProductUseCase`
- `SetProductPriceCommand(productId, variantId, priceLevel, costPrice, sellingPrice)` → `SetProductPriceUseCase`
- `CreateDiscountCommand(orgId, name, type, value, scope, validFrom, validTo)` → `CreateDiscountUseCase`
- `CreateSupplierCommand(...)` → `CreateSupplierUseCase`
- `CreateVendorCommand(orgId, userId, businessName, commissionRate)` → `CreateVendorUseCase`
- `ApproveVendorCommand(vendorId)` → `ApproveVendorUseCase`
- `SuspendVendorCommand(vendorId, reason)` → `SuspendVendorUseCase`

### Purchase Orders
- `CreatePurchaseOrderCommand(...)` → `CreatePurchaseOrderUseCase`
- `SendPurchaseOrderCommand(poId)` → `SendPurchaseOrderUseCase`
- `ReceivePurchaseOrderCommand(poId, receivedItems)` → `ReceivePurchaseOrderUseCase`

### POS & Storefront
- `ProcessPosCheckoutCommand(outletId, tillId, customerId, type, items, discountId, paymentMethod)` → `ProcessPosCheckoutUseCase`
- `ProcessCreditSaleCommand(...)` → `ProcessCreditSaleUseCase`
- `RefundPosSaleCommand(orderId, reason, refundMethod)` → `RefundPosSaleUseCase`
- `CreateLayawayCommand(orderId, depositAmount)` → `CreateLayawayUseCase`
- `OpenTillCommand(outletId, userId, openingFloat)` → `OpenTillUseCase`
- `CloseTillCommand(tillId, userId, actualBalance)` → `CloseTillUseCase`
- `OccupyTableCommand(tableId, covers, waiterId)` → `OccupyTableUseCase`
- `SendKitchenOrderCommand(salesOrderId, tableId, items)` → `SendKitchenOrderUseCase`
- `MarkKotReadyCommand(kotId)` → `MarkKotReadyUseCase`
- `CreateOnlineOrderCommand(orgId, customerId, items, deliveryAddress, paymentMethod)` → `CreateOnlineOrderUseCase`

### Inventory
- `AdjustStockCommand(inventoryId, newQty, reason)` → `AdjustStockUseCase`
- `InitiateStockCountCommand(outletId)` → `InitiateStockCountUseCase`
- `ReconcileStockCountCommand(countId, countedItems)` → `ReconcileStockCountUseCase`
- `RequestStockTransferCommand(orgId, sourceId, destId, items)` → `RequestStockTransferUseCase`
- `ApproveStockTransferCommand(transferId)` → `ApproveStockTransferUseCase`
- `ReceiveStockTransferCommand(transferId, receivedItems)` → `ReceiveStockTransferUseCase`
- `ApproveCustomerReturnCommand(returnId)` → `ApproveCustomerReturnUseCase`

---

## 7. Queries

- `ListProductsQuery(orgId, vendorId, categoryId, status, search)` → `Page<ProductResult>`
- `SearchProductByBarcodeQuery(orgId, barcode)` — barcode scanner
- `GetInventoryLevelQuery(outletId, productId)` → `InventoryResult`
- `GetStockSummaryQuery(outletId)` — stock value, low-stock alerts
- `ListLowStockProductsQuery(orgId, outletId)` → `List<LowStockResult>`
- `ListPosTransactionsQuery(outletId, dateFrom, dateTo, tillId, cashierId)` → `Page<SalesOrderResult>`
- `GetSalesSummaryReportQuery(orgId, dateFrom, dateTo, groupBy)` — by cashier/outlet/product/vendor
- `GetTillSummaryQuery(tillId)` — expected vs actual balance
- `ListActiveTablesQuery(outletId)` → kitchen and floor display
- `ListVendorsQuery(orgId, status)` → `List<VendorResult>`
- `GetVendorEarningsQuery(vendorId, month)` → vendor's payout summary

---

## 8. Listeners

- **`ChargeSuccessfulListener`**: `ChargeSuccessfulEvent` from `pay` — completes pending sale, deducts stock, publishes `PosSaleCompletedEvent`
- **`ChargeFailedListener`**: `ChargeFailedEvent` from `pay` — releases reserved stock, marks order FAILED
- **`ShipmentDeliveredListener`**: `ShipmentDeliveredEvent` from `logistics` — confirms stock receipt for purchase orders and online orders
- **`CustomerReturnReceivedListener`**: `CustomerReturnShipmentReceivedEvent` from `logistics` — triggers `ApproveCustomerReturnUseCase`

---

## 9. Distributed Architecture

### Locking Strategy
- **Pessimistic Locking**: `Inventory` during `reserveStock()`, `deductStock()`, `addStock()` — prevents overselling under concurrent orders
- **Pessimistic Locking**: `CustomerCredit` during credit sales — prevents exceeding credit limit concurrently
- **Optimistic Locking**: `SalesOrder`, `Product`, `Till`, `HospitalityTable`, `Vendor`

### Inbox & Outbox
- **Outbox**: `PosSaleCompletedEvent`, `PurchaseOrderSentEvent`, `VendorApprovedEvent` — must be reliably delivered downstream
- **Inbox**: `ChargeSuccessfulEvent`, `ChargeFailedEvent`, `ShipmentDeliveredEvent` — must be idempotent. Duplicate delivery of `ChargeSuccessfulEvent` must not deduct stock twice or post duplicate accounting entries.
