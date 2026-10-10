# Commerce Inventory Design (`atlashub-commerce` / `com.atlashub.commerce.inventory`)

## Role & Purpose

The `inventory` subpackage tracks stock levels per outlet per product, manages stock adjustments, periodic stock counts, inter-outlet transfers, and customer returns. It is the stock ledger of the Commerce module — all stock movement (add, reserve, deduct, adjust) flows through here.

Gradle module: `atlashub-commerce`  
Package: `com.atlashub.commerce.inventory`

---

## Domain Layer

### `Inventory` (Aggregate Root)

**Package:** `com.atlashub.commerce.inventory.domain.entities`

```
Inventory
├── id               : Long
├── organizationId   : Long
├── outletId         : Long
├── productId        : Long
├── variantId        : Long          ← nullable
├── quantity         : Integer
├── reservedQuantity : Integer       ← held during pending orders
├── reorderLevel     : Integer
└── safeStock        : Integer
```

**Business methods (on entity — all arithmetic on own fields, pessimistic lock at DB level):**

| Method | Inputs | Guard | Events | Exceptions |
|---|---|---|---|---|
| `reserveStock(qty)` | int | `quantity - reservedQuantity >= qty` | — | `InsufficientStockException` |
| `releaseReservedStock(qty)` | int | `reservedQuantity >= qty` | — | `InvalidInventoryOperationException` |
| `deductStock(qty)` | int | `reservedQuantity >= qty` | — | `InvalidInventoryOperationException` |
| `addStock(qty)` | int | qty > 0 | `LowStockResolvedEvent` (if was below reorder level) | — |
| `adjust(newQty, reason)` | int, AdjustmentReason | newQty >= 0 | `StockAdjustedEvent` | — |

**Why on entity + pessimistic lock:** `reserveStock`, `deductStock`, and `addStock` are concurrent hot paths (multiple concurrent orders). The aggregate is simple enough to reside on one entity, but the lock must be at the DB row level (not application-level) to be safe under concurrent load.

---

### `StockAdjustment` (Aggregate Root)

```
StockAdjustment
├── id          : Long
├── inventoryId : Long
├── adjustedBy  : Long
├── previousQty : Integer
├── newQty      : Integer
└── reason      : AdjustmentReason   ← DAMAGE | EXPIRY | THEFT | CORRECTION | INITIAL_COUNT
```

---

### `StockCount` (Aggregate Root)

```
StockCount
├── id             : Long
├── outletId       : Long
├── status         : StockCountStatus ← OPEN | RECONCILED
├── countedItems   : List<StockCountItem>
├── startedAt      : ZonedDateTime
└── reconciledAt   : ZonedDateTime    ← nullable
```

**Business methods:**
- `addCountItem(inventoryId, countedQty)` — adds or updates a counted item
- `reconcile()` — status == OPEN → RECONCILED; for each item: if counted ≠ system quantity → creates `StockAdjustment` for the delta; registers `StockCountReconciledEvent`

### `StockCountItem` (Entity)

```
StockCountItem
├── id          : Long
├── stockCountId: Long
├── inventoryId : Long
├── productId   : Long
├── systemQty   : Integer
└── countedQty  : Integer
```

---

### `StockTransfer` (Aggregate Root)

```
StockTransfer
├── id                  : Long
├── organizationId      : Long
├── sourceOutletId      : Long
├── destinationOutletId : Long
├── status              : TransferStatus ← REQUESTED | APPROVED | DISPATCHED | RECEIVED | CANCELLED
├── items               : List<StockTransferItem>
├── requestedAt         : ZonedDateTime
└── receivedAt          : ZonedDateTime   ← nullable
```

**Business methods:**
- `approve()` → REQUESTED → APPROVED; registers `StockTransferApprovedEvent`
- `dispatch()` → APPROVED → DISPATCHED; registers `StockTransferDispatchedEvent`; decrements stock from sourceOutlet
- `receive(receivedItems)` → DISPATCHED → RECEIVED; registers `StockTransferReceivedEvent`; increments stock at destinationOutlet
- `cancel()` → REQUESTED or APPROVED → CANCELLED

### `StockTransferItem` (Entity)

```
StockTransferItem
├── id              : Long
├── transferId      : Long
├── productId       : Long
├── quantityRequested: Integer
└── quantityReceived: Integer   ← nullable until received
```

---

### `CustomerReturn` (Aggregate Root)

```
CustomerReturn
├── id             : Long
├── salesOrderId   : Long
├── organizationId : Long
├── outletId       : Long
├── customerId     : Long
├── items          : List<ReturnItem>
├── refundAmount   : Money
├── reason         : String
├── refundMethod   : RefundMethod    ← CASH | CREDIT_NOTE | WALLET
├── status         : ReturnStatus   ← PENDING | APPROVED | REFUNDED | REJECTED
└── createdAt      : ZonedDateTime
```

**Business methods:**
- `approve()` → PENDING → APPROVED; registers `CustomerReturnApprovedEvent`; calls `inventory.addStock()` for each returned item
- `reject(reason)` → PENDING → REJECTED

### `ReturnItem` (Entity)

```
ReturnItem
├── id          : Long
├── returnId    : Long
├── productId   : Long
└── quantity    : Integer
```

---

### `StockReservation` (Aggregate Root)

```
StockReservation
├── id             : Long
├── salesOrderId   : Long
├── organizationId : Long
├── outletId       : Long
├── items          : List<StockReservationItem>
├── status         : ReservationStatus   ← ACTIVE | FULFILLED | RELEASED | FAILED
├── failureReason  : String              ← nullable
├── createdAt      : ZonedDateTime
└── updatedAt      : ZonedDateTime
```

### `StockReservationItem` (Entity)

```
StockReservationItem
├── id             : Long
├── reservationId  : Long
├── productId      : Long
└── quantity       : Integer
```

---

### Domain Events — `com.atlashub.commerce.inventory.domain.events`

| Event | Published When | Consumed By |
|---|---|---|
| `StockReservedEvent` | Stock successfully reserved for order | `commerce-storefront` (triggers payment initiation) |
| `StockReservationFailedEvent` | Stock insufficient or missing | `commerce-storefront` (marks order FAILED) |
| `StockAdjustedEvent` | Manual stock adjustment | `accounting` (Dr Loss/Gain, Cr/Dr Inventory) |
| `StockTransferApprovedEvent` | Transfer approved | `logistics` (create inter-outlet shipment if inter-city) |
| `StockTransferReceivedEvent` | Transfer received | `pay` (inter-outlet ledger entry), `accounting` |
| `CustomerReturnApprovedEvent` | Return approved | `pay` (initiate refund), `accounting` (reverse sale entry) |
| `StockCountReconciledEvent` | Count reconciled | `accounting` (post variance adjustments) |

---

### Domain Exceptions — `com.atlashub.commerce.inventory.domain.exceptions`

```java
public class InventoryNotFoundException extends NotFoundException {
    public InventoryNotFoundException(Long productId, Long outletId) {
        super("Inventory not found for product " + productId + " at outlet " + outletId);
    }
}
public class InsufficientStockException extends BusinessRuleException {
    public InsufficientStockException(Long productId, int requested, int available) {
        super("Insufficient stock for product " + productId + ": requested " + requested + ", available " + available);
    }
}
public class InvalidInventoryOperationException extends BusinessRuleException {
    public InvalidInventoryOperationException(String message) { super(message); }
}
public class StockCountNotFoundException extends NotFoundException {
    public StockCountNotFoundException(Long id) { super("Stock count not found: " + id); }
}
public class StockTransferNotFoundException extends NotFoundException {
    public StockTransferNotFoundException(Long id) { super("Stock transfer not found: " + id); }
}
public class InvalidTransferStateException extends BusinessRuleException {
    public InvalidTransferStateException(String message) { super(message); }
}
public class CustomerReturnNotFoundException extends NotFoundException {
    public CustomerReturnNotFoundException(Long id) { super("Customer return not found: " + id); }
}
public class InvalidReturnStateException extends BusinessRuleException {
    public InvalidReturnStateException(String message) { super(message); }
}
```

---

## Application Layer

### Commands — `com.atlashub.commerce.inventory.application.commands`

#### `AdjustStockCommand`
```java
record AdjustStockCommand(Long inventoryId, int newQuantity, AdjustmentReason reason, Long adjustedBy)
```
**Handler:** `AdjustStockHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('commerce:inventory:adjust')")`  
**Flow:** Load `Inventory` → `inventory.adjust(newQty, reason)` → create `StockAdjustment` → `repository.save()` → `StockAdjustedEvent` published

---

#### `InitiateStockCountCommand`
```java
record InitiateStockCountCommand(Long outletId, Long initiatedBy)
```
**Handler:** `InitiateStockCountHandler` | **Response:** `InitiateStockCountResponse(Long stockCountId)`  
**RBAC:** `@PreAuthorize("hasAuthority('commerce:inventory:adjust')")`  
**Flow:** Load all `Inventory` records for outlet → snapshot system quantities → create `StockCount` (OPEN) with `StockCountItem` for each → `repository.save()`

---

#### `ReconcileStockCountCommand`
```java
record ReconcileStockCountCommand(Long stockCountId, List<CountedItemDto> countedItems)
```
**Handler:** `ReconcileStockCountHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('commerce:inventory:adjust')")`  
**Flow:** Load `StockCount` → update `countedQty` for each item → `stockCount.reconcile()` → for each delta: create `StockAdjustment` + update `Inventory.adjust()` → `repository.save()` → `StockCountReconciledEvent` published

---

#### `RequestStockTransferCommand`
```java
record RequestStockTransferCommand(Long organizationId, Long sourceOutletId,
                                   Long destinationOutletId, List<TransferItemDto> items)
```
**Handler:** `RequestStockTransferHandler` | **Response:** `RequestStockTransferResponse(Long transferId)`

---

#### `ApproveStockTransferCommand`
```java
record ApproveStockTransferCommand(Long transferId)
```
**Handler:** `ApproveStockTransferHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('commerce:inventory:adjust')")`

---

#### `ReceiveStockTransferCommand`
```java
record ReceiveStockTransferCommand(Long transferId, List<ReceivedTransferItemDto> receivedItems)
```
**Handler:** `ReceiveStockTransferHandler` | **Response:** `void`  
**Flow:** Load `StockTransfer` → `transfer.receive(items)` → for each item: `destinationInventory.addStock(qty)` (pessimistic lock) → `repository.save()` → `StockTransferReceivedEvent` published

---

#### `CreateCustomerReturnCommand`
```java
record CreateCustomerReturnCommand(Long salesOrderId, Long organizationId, Long outletId,
                                   Long customerId, List<ReturnItemDto> items,
                                   Money refundAmount, String reason, RefundMethod refundMethod)
```
**Handler:** `CreateCustomerReturnHandler` | **Response:** `CreateCustomerReturnResponse(Long returnId)`

---

#### `ApproveCustomerReturnCommand`
```java
record ApproveCustomerReturnCommand(Long returnId)
```
**Handler:** `ApproveCustomerReturnHandler` | **Response:** `void`  
**Invocation source:** Either HTTP (manager approval) or `ReturnShipmentReceivedListener`  
**Flow:** Load `CustomerReturn` → `customerReturn.approve()` → for each item: `inventory.addStock(qty)` (pessimistic lock) → `repository.save()` → `CustomerReturnApprovedEvent` published

---

#### `ReserveStockForOrderCommand`
```java
record ReserveStockForOrderCommand(Long salesOrderId, Long organizationId, Long outletId, List<OrderItemDto> items)
```
**Handler:** `ReserveStockForOrderHandler` | **Response:** `ReserveStockForOrderResult(Long reservationId, ReservationStatus status, boolean success, String message)`  
**Invocation source:** `SalesOrderCreatedListener`  
**Flow:** Idempotent check on `salesOrderId` → lock inventory rows with `PESSIMISTIC_WRITE` → if stock available: call `inventory.reserveStock(qty)`, confirm `StockReservation` → `repository.save()` publishes `StockReservedEvent`. If insufficient: fail `StockReservation` → `repository.save()` publishes `StockReservationFailedEvent`.

---

#### `DeductReservedStockCommand`
```java
record DeductReservedStockCommand(Long salesOrderId)
```
**Handler:** `DeductReservedStockHandler` | **Response:** `void`  
**Invocation source:** `PosSaleCompletedListener`  
**Flow:** Load `StockReservation` by `salesOrderId` → for each item: `inventory.deductStock(qty)` → fulfill reservation → `repository.save()`.

---

#### `ReleaseReservedStockCommand`
```java
record ReleaseReservedStockCommand(Long salesOrderId)
```
**Handler:** `ReleaseReservedStockHandler` | **Response:** `void`  
**Invocation source:** `PosSaleFailedListener`  
**Flow:** Load `StockReservation` by `salesOrderId` → for each item: `inventory.releaseReservedStock(qty)` → release reservation → `repository.save()`.

---

### Queries — `com.atlashub.commerce.inventory.application.queries`

#### `GetInventoryLevelQuery`
```java
record GetInventoryLevelQuery(Long outletId, Long productId)
```
**Handler:** `GetInventoryLevelHandler` | **Result:** `InventoryResult`

`InventoryResult`: `inventoryId`, `productId`, `outletId`, `quantity`, `reservedQuantity`, `availableQuantity`, `reorderLevel`, `safeStock`, `isLowStock`

---

#### `GetStockSummaryQuery`
```java
record GetStockSummaryQuery(Long outletId)
```
**Handler:** `GetStockSummaryHandler` | **Result:** `StockSummaryResult`

`StockSummaryResult`: `totalSkus`, `totalStockValue`, `lowStockCount`, `outOfStockCount`

---

#### `ListLowStockProductsQuery`
```java
record ListLowStockProductsQuery(Long organizationId, Long outletId)
```
**Handler:** `ListLowStockProductsHandler` | **Result:** `List<LowStockResult>` (bounded per outlet)

---

#### `ListStockTransfersQuery`
```java
record ListStockTransfersQuery(Long organizationId, TransferStatus status, int page, int size)
```
**Handler:** `ListStockTransfersHandler` | **Result:** `PageResult<StockTransferResult>`

---

## Infrastructure Layer

### Persistence

**JPA Entities:**

| Entity | Table | Locking |
|---|---|---|
| `InventoryJpaEntity` | `commerce_inventory` | **`@Lock(PESSIMISTIC_WRITE)`** for all stock movements |
| `StockAdjustmentJpaEntity` | `commerce_stock_adjustments` | — |
| `StockCountJpaEntity` | `commerce_stock_counts` | `@Version` optimistic |
| `StockCountItemJpaEntity` | `commerce_stock_count_items` | — |
| `StockTransferJpaEntity` | `commerce_stock_transfers` | `@Version` optimistic |
| `StockTransferItemJpaEntity` | `commerce_stock_transfer_items` | — |
| `CustomerReturnJpaEntity` | `commerce_customer_returns` | `@Version` optimistic |
| `ReturnItemJpaEntity` | `commerce_return_items` | — |
| `StockReservationJpaEntity` | `commerce_stock_reservations` | `@Version` optimistic |
| `StockReservationItemJpaEntity` | `commerce_stock_reservation_items` | — |

**Spring Data Repositories:**

```
InventoryJpaRepository
  + findByOutletIdAndProductId(Long outletId, Long productId): Optional<InventoryJpaEntity>
  + findByOutletId(Long outletId): List<InventoryJpaEntity>
  + findByOrganizationIdAndQuantityLessThanEqualReorderLevel(Long orgId): List<InventoryJpaEntity>
  + findByIdWithPessimisticLock(Long id): Optional<InventoryJpaEntity>  // @Lock PESSIMISTIC_WRITE

StockTransferJpaRepository
  + findByOrganizationIdAndStatus(Long orgId, TransferStatus status, Pageable p): Page<StockTransferJpaEntity>

CustomerReturnJpaRepository
  + findBySalesOrderId(Long salesOrderId): Optional<CustomerReturnJpaEntity>

StockReservationJpaRepository
  + findBySalesOrderId(Long salesOrderId): Optional<StockReservationJpaEntity>
  + findByOrganizationIdAndStatus(Long orgId, ReservationStatus status): List<StockReservationJpaEntity>
```

**Repository Adapters:**
- `InventoryRepositoryAdapter` → `commerce_inventory_seq`
- `StockAdjustmentRepositoryAdapter` → `commerce_stock_adjustment_seq`
- `StockCountRepositoryAdapter` → `commerce_stock_count_seq`
- `StockTransferRepositoryAdapter` → `commerce_stock_transfer_seq`
- `CustomerReturnRepositoryAdapter` → `commerce_customer_return_seq`
- `StockReservationRepositoryAdapter` → `commerce_stock_reservation_seq`

### Listeners — `infrastructure/messaging/listeners/`

#### `ProductCreatedListener` (from catalog subpackage)
| Attribute | Value |
|---|---|
| **Topic** | `commerce-events` |
| **Group ID** | `commerce-inventory-group` |
| **Event consumed** | `ProductCreatedEvent` |
| **Payload** | `productId`, `organizationId`, `isService`, `outletIds` |
| **Action** | Creates `Inventory` record (`quantity = 0`) for each outlet if `isService == false` |

#### `SalesOrderCreatedListener` (Checkout Saga Step 2)
| Attribute | Value |
|---|---|
| **Topic** | `commerce-events` |
| **Group ID** | `commerce-inventory-order-created` |
| **Event consumed** | `SalesOrderCreatedEvent` |
| **Payload** | `salesOrderId`, `organizationId`, `outletId`, `items[]` |
| **Command called** | `ReserveStockForOrderHandler` |
| **Flow** | Asynchronously reserves stock with pessimistic row lock; publishes `StockReservedEvent` or `StockReservationFailedEvent` via Outbox. |

#### `PosSaleCompletedListener` (Checkout Saga Finalization)
| Attribute | Value |
|---|---|
| **Topic** | `commerce-events` |
| **Group ID** | `commerce-inventory-sale-completed` |
| **Event consumed** | `PosSaleCompletedEvent` |
| **Payload** | `salesOrderId`, `organizationId`, `outletId` |
| **Command called** | `DeductReservedStockHandler` |
| **Flow** | Permanently converts reserved stock to deducted stock and fulfills the reservation. |

#### `PosSaleFailedListener` (Checkout Saga Compensation)
| Attribute | Value |
|---|---|
| **Topic** | `commerce-events` |
| **Group ID** | `commerce-inventory-sale-failed` |
| **Event consumed** | `PosSaleFailedEvent` |
| **Payload** | `salesOrderId`, `organizationId`, `reason` |
| **Command called** | `ReleaseReservedStockHandler` |
| **Flow** | Releases reserved stock back to available inventory and marks reservation released. |

#### `ReturnShipmentReceivedListener`

| Attribute | Value |
|---|---|
| **Topic** | `logistics-events` |
| **Group ID** | `commerce-inventory-return-received` |
| **Event consumed** | `ReturnShipmentReceivedEvent` |
| **Payload** | `returnShipmentId`, `salesOrderId`, `organizationId`, `outletId`, `items[]` (each: `productId`, `quantity`) |
| **Command called** | `ApproveCustomerReturnHandler` |
| **Flow** | Logistics marks the physical shipment as received at the outlet. This listener auto-approves the `CustomerReturn` (no manual approval needed once goods are confirmed received), restores stock for each item, and publishes `CustomerReturnApprovedEvent` → `pay:transfers` initiates the refund payout. |

---

## Presentation Layer

### Controller: `CommerceInventoryController`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `GET` | `/inventory` | — | `?outletId&productId` | `InventoryResult` |
| `GET` | `/inventory/summary` | — | `?outletId` | `StockSummaryResult` |
| `GET` | `/inventory/low-stock` | — | `?orgId&outletId` | `List<LowStockResult>` |
| `POST` | `/inventory/adjust` | `commerce:inventory:adjust` | `AdjustStockRequest` | `void` |
| `POST` | `/stock-counts` | `commerce:inventory:adjust` | `InitiateStockCountRequest` | `InitiateStockCountResponse` |
| `POST` | `/stock-counts/{id}/reconcile` | `commerce:inventory:adjust` | `ReconcileStockCountRequest` | `void` |
| `POST` | `/stock-transfers` | — | `RequestStockTransferRequest` | `RequestStockTransferResponse` |
| `POST` | `/stock-transfers/{id}/approve` | `commerce:inventory:adjust` | — | `void` |
| `POST` | `/stock-transfers/{id}/receive` | — | `ReceiveStockTransferRequest` | `void` |
| `GET` | `/stock-transfers` | — | `?orgId&status&page&size` | `PageResult<StockTransferResult>` |
| `POST` | `/returns` | — | `CreateCustomerReturnRequest` | `CreateCustomerReturnResponse` |
| `POST` | `/returns/{id}/approve` | — | — | `void` |

---

## RBAC Table

| Permission | Commands |
|---|---|
| `commerce:inventory:adjust` | `AdjustStockHandler`, `InitiateStockCountHandler`, `ReconcileStockCountHandler`, `ApproveStockTransferHandler` |

---

## Distributed Architecture

### Locking
- `Inventory` — **Pessimistic Write** on ALL stock mutation methods (`reserveStock`, `deductStock`, `addStock`, `adjust`) — prevents overselling
- All others — Optimistic (`@Version`)

### Outbox
- `StockTransferApprovedEvent` — triggers logistics to create a shipment for inter-outlet transfer
- `CustomerReturnApprovedEvent` — triggers pay refund + accounting reversal

---

## Complete File List

```
atlashub-commerce/src/main/java/com/atlashub/commerce/inventory/
├── domain/entities/ [Inventory, StockAdjustment, StockCount, StockCountItem, StockTransfer, StockTransferItem, CustomerReturn, ReturnItem]
├── domain/events/ [StockAdjustedEvent, StockTransferApprovedEvent, StockTransferReceivedEvent, CustomerReturnApprovedEvent, StockCountReconciledEvent]
├── domain/exceptions/ [InventoryNotFoundException, InsufficientStockException, InvalidInventoryOperationException, StockCountNotFoundException, StockTransferNotFoundException, InvalidTransferStateException, CustomerReturnNotFoundException, InvalidReturnStateException]
├── domain/repositories/ [InventoryRepository, StockAdjustmentRepository, StockCountRepository, StockTransferRepository, CustomerReturnRepository]
├── domain/valueobject/ [AdjustmentReason, StockCountStatus, TransferStatus, ReturnStatus, RefundMethod]
├── application/
│   ├── commands/ [AdjustStock, InitiateStockCount, ReconcileStockCount, RequestStockTransfer, ApproveStockTransfer, ReceiveStockTransfer, CreateCustomerReturn, ApproveCustomerReturn]
│   └── queries/ [GetInventoryLevel, GetStockSummary, ListLowStockProducts, ListStockTransfers]
├── infrastructure/
│   ├── messaging/events/ [ProductCreatedPayload, CustomerReturnShipmentReceivedPayload]
│   ├── messaging/listeners/ [ProductCreatedListener, CustomerReturnShipmentReceivedListener]
│   ├── persistence/ [adapters, entities, mappers, repositories]
└── presentation/
    ├── dto/ [...]
    └── rest/
        └── CommerceInventoryController.java
```
