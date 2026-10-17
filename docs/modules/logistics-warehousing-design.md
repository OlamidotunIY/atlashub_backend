# Logistics Warehousing Design (`:atlashub-logistics:warehousing` / `com.atlashub.logistics.warehousing`)

## Role & Purpose

The `:atlashub-logistics:warehousing` submodule manages:
1. **Merchant Warehouses & AtlasHub Distribution Centers** (`Warehouse` with `FacilityType`: `MERCHANT_WAREHOUSE` | `ATLASHUB_DISTRIBUTION_CENTER`).
2. **Warehouse Stock & Reservations** (`WarehouseInventory`).
3. **Inter-Outlet & Warehouse Stock Transfers** (`StockTransfer`: `OUTLET` ↔ `OUTLET`, `WAREHOUSE` ↔ `OUTLET`, `WAREHOUSE` ↔ `WAREHOUSE`).
   - Inter-outlet transfers are initiated by the business from their dashboard.
   - The business pays the transfer shipping fee directly, and `INTER_OUTLET_TRANSFER` shipments are **ineligible for the monthly 10% merchant shipping incentive**.

Gradle module: `:atlashub-logistics:warehousing`  
Package: `com.atlashub.logistics.warehousing`

---

## Domain Layer

### 1. `Warehouse` (Aggregate Root)

**Package:** `com.atlashub.logistics.warehousing.domain.entities`

```
Warehouse
├── id             : Long
├── organizationId : Long
├── name           : String
├── code           : String            ← e.g., "DC-LOS-01" or "WH-MERCH-01"
├── facilityType   : FacilityType      ← MERCHANT_WAREHOUSE | ATLASHUB_DISTRIBUTION_CENTER
├── address        : Address
├── managerId      : Long
├── status         : WarehouseStatus   ← ACTIVE | INACTIVE
└── createdAt      : ZonedDateTime
```

**Business methods:** `deactivate()`, `reactivate()`

---

### 2. `WarehouseInventory` (Aggregate Root)

```
WarehouseInventory
├── id               : Long
├── organizationId   : Long
├── warehouseId      : Long
├── productId        : Long
├── quantity         : int
├── reservedQuantity : int
└── updatedAt        : ZonedDateTime
```

**Business methods:**
- `addStock(int qty)` — `qty > 0`
- `reserveStock(int qty)` — `quantity - reservedQuantity >= qty`; throws `InsufficientWarehouseStockException`
- `deductStock(int qty)` — `reservedQuantity >= qty`
- `releaseReservedStock(int qty)` — `reservedQuantity >= qty`
- `availableQuantity()` — returns `quantity - reservedQuantity`

---

### 3. `StockTransfer` (Aggregate Root)

```
StockTransfer
├── id                  : Long
├── organizationId      : Long
├── sourceId            : Long            ← outletId or warehouseId
├── sourceType          : LocationType    ← OUTLET | WAREHOUSE
├── sourceAddress       : Address
├── destinationId       : Long
├── destinationType     : LocationType    ← OUTLET | WAREHOUSE
├── destinationAddress  : Address
├── totalWeightKg       : BigDecimal
├── transferShippingFee : Money           ← paid by business; 0% monthly incentive eligibility
├── status              : TransferStatus  ← REQUESTED | APPROVED | DISPATCHED | RECEIVED | CANCELLED
├── shipmentId          : Long            ← nullable — linked Shipment (type = INTER_OUTLET_TRANSFER)
├── items               : List<StockTransferItem>
├── requestedBy         : Long
├── approvedBy          : Long            ← nullable
├── requestedAt         : ZonedDateTime
└── receivedAt          : ZonedDateTime   ← nullable
```

**Business methods:**
- `approve(Long approverId)` → `REQUESTED` → `APPROVED`; registers `StockTransferApprovedEvent`
- `markDispatched(Long shipmentId)` → `APPROVED` → `DISPATCHED`; links `shipmentId` and registers `StockTransferDispatchedEvent`
- `receive(ZonedDateTime now)` → `DISPATCHED` → `RECEIVED`; sets `receivedAt` and registers `StockTransferReceivedEvent`
- `cancel()` → `REQUESTED` or `APPROVED` → `CANCELLED`

---

## Application Layer

### Commands (`com.atlashub.logistics.warehousing.application.command`)
- `RegisterWarehouse` (`RegisterWarehouseCommand`, `RegisterWarehouseHandler`, `RegisterWarehouseResult`)
- `AdjustWarehouseStock` (`AdjustWarehouseStockCommand`, `AdjustWarehouseStockHandler`, `AdjustWarehouseStockResult`)
- `RequestStockTransfer` (`RequestStockTransferCommand`, `RequestStockTransferHandler`, `RequestStockTransferResult`)
- `ApproveStockTransfer` (`ApproveStockTransferCommand`, `ApproveStockTransferHandler`)
- `DispatchStockTransfer` (`DispatchStockTransferCommand`, `DispatchStockTransferHandler`)
- `ReceiveStockTransfer` (`ReceiveStockTransferCommand`, `ReceiveStockTransferHandler`)

### Queries (`com.atlashub.logistics.warehousing.application.query`)
- `ListWarehouses` (`ListWarehousesQuery`, `ListWarehousesHandler`, `WarehouseResult`)
- `GetWarehouseInventory` (`GetWarehouseInventoryQuery`, `GetWarehouseInventoryHandler`, `WarehouseInventoryResult`)
- `ListStockTransfers` (`ListStockTransfersQuery`, `ListStockTransfersHandler`, `StockTransferResult`)

---

## Presentation Layer

### Controller: `WarehouseController` (`/api/v1/warehouses` & `/api/v1/stock-transfers`)
- `POST /api/v1/warehouses`
- `GET /api/v1/warehouses`
- `POST /api/v1/warehouses/{id}/inventory`
- `GET /api/v1/warehouses/{id}/inventory`
- `POST /api/v1/stock-transfers`
- `POST /api/v1/stock-transfers/{id}/approve`
- `POST /api/v1/stock-transfers/{id}/dispatch`
- `POST /api/v1/stock-transfers/{id}/receive`
- `GET /api/v1/stock-transfers`
