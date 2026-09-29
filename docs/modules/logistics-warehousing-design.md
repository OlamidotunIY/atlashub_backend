# Logistics Warehousing Design (`atlashub-logistics` / `com.atlashub.logistics.warehousing`)

## Role & Purpose

The `warehousing` subpackage manages centralized warehouses and their stock. Organizations that operate warehouses use this to track stock held before distribution to outlets. Stock at a warehouse is tracked separately from outlet stock. Inter-location stock transfers (warehouse ↔ outlet or warehouse ↔ warehouse) go through this subpackage.

Gradle module: `atlashub-logistics`  
Package: `com.atlashub.logistics.warehousing`

---

## Domain Layer

### `Warehouse` (Aggregate Root)

**Package:** `com.atlashub.logistics.warehousing.domain.entities`

```
Warehouse
├── id             : Long
├── organizationId : Long
├── name           : String
├── address        : Address
├── managerId      : Long
└── status         : WarehouseStatus ← ACTIVE | INACTIVE
```

**Business methods:** `deactivate()`, `reactivate()`

---

### `WarehouseInventory` (Aggregate Root)

```
WarehouseInventory
├── id               : Long
├── warehouseId      : Long
├── productId        : Long
├── quantity         : Integer
└── reservedQuantity : Integer
```

**Business methods (on entity — pessimistic lock at DB level for all mutations):**
- `addStock(qty)` — qty > 0
- `reserveStock(qty)` — `quantity - reservedQuantity >= qty`; throws `InsufficientWarehouseStockException`
- `deductStock(qty)` — `reservedQuantity >= qty`
- `releaseReservedStock(qty)` — `reservedQuantity >= qty`

---

### `StockTransfer` (Aggregate Root)

```
StockTransfer
├── id                  : Long
├── organizationId      : Long
├── sourceId            : Long            ← outletId or warehouseId
├── sourceType          : LocationType    ← OUTLET | WAREHOUSE
├── destinationId       : Long
├── destinationType     : LocationType
├── status              : TransferStatus  ← REQUESTED | APPROVED | DISPATCHED | RECEIVED | CANCELLED
├── shipmentId          : Long            ← nullable — linked Shipment for tracking
├── items               : List<StockTransferItem>
├── requestedAt         : ZonedDateTime
└── receivedAt          : ZonedDateTime   ← nullable
```

**Business methods (on entity):**
- `approve()` → REQUESTED → APPROVED; registers `StockTransferApprovedEvent`
- `dispatch(shipmentId)` → APPROVED → DISPATCHED; links shipment for tracking
- `receive(receivedItems)` → DISPATCHED → RECEIVED; registers `StockTransferReceivedEvent`
- `cancel()` → REQUESTED or APPROVED → CANCELLED

### `StockTransferItem` (Entity)

```
StockTransferItem
├── id                : Long
├── transferId        : Long
├── productId         : Long
└── quantity          : Integer
```

---

### Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `StockTransferApprovedEvent` | Transfer approved | `shipping` (creates tracking shipment if inter-city) |
| `StockTransferDispatchedEvent` | Goods dispatched | `notifications` |
| `StockTransferReceivedEvent` | Transfer received | `commerce` (update outlet inventory), `accounting` (Dr Inventory-Dest, Cr Inventory-Source) |

---

### Domain Exceptions — `com.atlashub.logistics.warehousing.domain.exceptions`

```java
public class WarehouseNotFoundException extends NotFoundException {
    public WarehouseNotFoundException(Long id) { super("Warehouse not found: " + id); }
}
public class TransferNotFoundException extends NotFoundException {
    public TransferNotFoundException(Long id) { super("Stock transfer not found: " + id); }
}
public class InvalidTransferStateException extends BusinessRuleException {
    public InvalidTransferStateException(String message) { super(message); }
}
public class InsufficientWarehouseStockException extends BusinessRuleException {
    public InsufficientWarehouseStockException(Long productId) {
        super("Insufficient warehouse stock for product: " + productId);
    }
}
```

---

## Application Layer

### Commands — `com.atlashub.logistics.warehousing.application.commands`

#### `RegisterWarehouseCommand`
```java
record RegisterWarehouseCommand(Long organizationId, String name, Address address, Long managerId)
```
**Handler:** `RegisterWarehouseHandler` | **Response:** `Long warehouseId`  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:fleet:manage')")`

---

#### `RequestStockTransferCommand`
```java
record RequestStockTransferCommand(
    Long organizationId, Long sourceId, LocationType sourceType,
    Long destinationId, LocationType destinationType, List<TransferItemDto> items
)
```
**Handler:** `RequestStockTransferHandler` | **Response:** `Long transferId`

---

#### `ApproveStockTransferCommand`
```java
record ApproveStockTransferCommand(Long transferId)
```
**Handler:** `ApproveStockTransferHandler` | **Response:** `void`  
**Flow:** Load `StockTransfer` → `transfer.approve()` → save → `StockTransferApprovedEvent` published → if cross-city, shipping subpackage creates a `Shipment`

---

#### `ReceiveStockTransferCommand`
```java
record ReceiveStockTransferCommand(Long transferId, List<ReceivedItemDto> receivedItems)
```
**Handler:** `ReceiveStockTransferHandler` | **Response:** `void`  
**Flow:** Load `StockTransfer` (pessimistic lock) → `transfer.receive(items)` → update `WarehouseInventory.addStock()` at destination → save → `StockTransferReceivedEvent` published

---

### Queries — `com.atlashub.logistics.warehousing.application.queries`

#### `ListStockTransfersQuery`
```java
record ListStockTransfersQuery(Long organizationId, Long locationId, TransferStatus status)
```
**Handler:** `ListStockTransfersHandler` | **Result:** `List<StockTransferResult>` (bounded per org/location)

---

#### `ListWarehousesQuery`
```java
record ListWarehousesQuery(Long organizationId, WarehouseStatus status)
```
**Handler:** `ListWarehousesHandler` | **Result:** `List<WarehouseResult>` (bounded per org)

---

#### `GetWarehouseInventoryQuery`
```java
record GetWarehouseInventoryQuery(Long warehouseId, Long productId)
```
**Handler:** `GetWarehouseInventoryHandler` | **Result:** `WarehouseInventoryResult(warehouseId, productId, quantity, reservedQuantity, availableQuantity)`

---

## Infrastructure Layer

### Persistence

| JPA Entity | Table | Locking |
|---|---|---|
| `WarehouseJpaEntity` | `logistics_warehouses` | — |
| `WarehouseInventoryJpaEntity` | `logistics_warehouse_inventory` | `@Lock(PESSIMISTIC_WRITE)` on mutations |
| `StockTransferJpaEntity` | `logistics_stock_transfers` | `@Lock(PESSIMISTIC_WRITE)` during receive |
| `StockTransferItemJpaEntity` | `logistics_stock_transfer_items` | — |

**Repository Adapters:**
- `WarehouseRepositoryAdapter` → `logistics_warehouse_seq`
- `WarehouseInventoryRepositoryAdapter` → `logistics_warehouse_inventory_seq`
- `StockTransferRepositoryAdapter` → `logistics_stock_transfer_seq`

---

## Presentation Layer

### Controller: `WarehouseController` — `/api/v1/logistics/warehouses`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/warehouses` | `logistics:fleet:manage` | `RegisterWarehouseRequest` | `Long` |
| `GET` | `/warehouses` | — | `?orgId&status` | `List<WarehouseResult>` |
| `GET` | `/warehouses/{id}/inventory` | — | `?productId` | `WarehouseInventoryResult` |
| `POST` | `/stock-transfers` | — | `RequestStockTransferRequest` | `Long` |
| `POST` | `/stock-transfers/{id}/approve` | `logistics:fleet:manage` | — | `void` |
| `POST` | `/stock-transfers/{id}/receive` | — | `ReceiveStockTransferRequest` | `void` |
| `GET` | `/stock-transfers` | — | `?orgId&locationId&status` | `List<StockTransferResult>` |

---

## Complete File List

```
atlashub-logistics/src/main/java/com/atlashub/logistics/warehousing/
├── domain/
│   ├── entities/ [Warehouse, WarehouseInventory, StockTransfer, StockTransferItem]
│   ├── events/ [StockTransferApprovedEvent, StockTransferDispatchedEvent, StockTransferReceivedEvent]
│   ├── exceptions/ [WarehouseNotFoundException, TransferNotFoundException, InvalidTransferStateException, InsufficientWarehouseStockException]
│   ├── repositories/ [WarehouseRepository, WarehouseInventoryRepository, StockTransferRepository]
│   └── valueobject/ [WarehouseStatus, LocationType, TransferStatus]
├── application/
│   ├── commands/ [RegisterWarehouse, RequestStockTransfer, ApproveStockTransfer, ReceiveStockTransfer]
│   └── queries/ [ListWarehouses, GetWarehouseInventory, ListStockTransfers]
├── infrastructure/
│   └── persistence/ [adapters, entities, mappers, repositories]
└── presentation/
    ├── dto/ [RegisterWarehouseRequest, RequestStockTransferRequest, ReceiveStockTransferRequest, WarehouseResult, WarehouseInventoryResult, StockTransferResult]
    └── rest/
        └── WarehouseController.java
```
