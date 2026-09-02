# Logistics Module Design (`atlashub-logistics`)

## Role & Purpose

The Logistics module is the **movement engine** of the AtlasHub platform. It manages everything that physically moves: shipments dispatched from warehouses, delivery riders on the road, inter-outlet stock transfers, and customer return shipments coming back in. Where Commerce tracks what stock *is*, Logistics tracks how it *gets there*.

Within an organization's operations, Logistics serves three distinct roles:
1. **Last-mile delivery**: Dispatching orders and tracking their journey to the customer.
2. **Internal logistics**: Moving stock between the organization's own outlets (warehouses, stores, depots).
3. **Reverse logistics**: Managing returned goods being shipped back from customers.

Logistics integrates directly with Commerce (inventory updates on delivery) and Pay (delivery fees, cash-on-delivery collections, insurance payouts) and Accounting (cost of goods movement, insurance claims).

---

## 1. Features

### Shipment Creation & Dispatch
When an order is ready to ship (from Commerce's online orders or a standalone delivery request), `CreateShipmentUseCase` creates a `Shipment` with a unique tracking number and generates a `Waybill`. A dispatch rider is then assigned (`AssignRiderUseCase`) from the organization's fleet. The shipment is dispatched and its journey can be tracked via location updates appended to `ShipmentTrackingHistory`.

### Proof of Delivery (POD)
On delivery, the rider submits a `proofOfDeliveryUrl` (a photo of the signed delivery note or recipient's acknowledgment). The `DeliverShipmentUseCase` requires this field — delivery cannot be confirmed without it. Once delivered, `ShipmentDeliveredEvent` is published, which triggers Commerce to add stock (if it was a purchase order delivery) or mark an order as complete.

### Route Optimization
For organizations running multi-stop delivery routes, the `ShipmentRoute` entity captures a planned sequence of stops. `OptimizeRouteUseCase` reorders stops using a nearest-neighbor algorithm to minimize travel distance. Riders mark each stop as REACHED or SKIPPED as they move.

### Fleet & Rider Management
Organizations register their own vehicles (`FleetVehicle`) and delivery personnel (`DispatchRider`). Vehicles have types (BIKE, VAN, TRUCK) and capacity. Riders have status (AVAILABLE, ON_DELIVERY, OFFLINE). Assignment logic ensures a rider cannot be assigned to two concurrent shipments (enforced via pessimistic locking during `assignDelivery()`).

### Inter-Outlet Stock Transfers
When Commerce raises a `StockTransfer` request (moving goods from Outlet A to Outlet B), Logistics manages the physical movement:
1. A `StockTransfer` is approved and dispatched by a rider/vehicle.
2. On arrival at the destination, `ReceiveStockTransferUseCase` is called.
3. `StockTransferReceivedEvent` is published → Commerce updates inventory at both outlets → Pay posts the inter-outlet ledger transaction → Accounting posts the inventory movement journal entry.

**The financial entry for a stock transfer**:
```
DEBIT  Inventory Asset (Outlet B)   [cost value of transferred goods]
CREDIT Inventory Asset (Outlet A)   [same cost value]
SourceSystem: INTER_OUTLET_TRANSFER
```

### Warehousing & Storage
The `warehousing` submodule manages stock held in centralized warehouses before distribution. Stock transfers originate from or go to warehouses as well as retail outlets.

### Customer Return Shipments (Reverse Logistics)
When a customer wants to return a product, Commerce creates a `CustomerReturn` and Logistics handles the return shipment (`CustomerReturnShipment`). The rider picks up the goods from the customer and delivers them back to the specified outlet. On receipt, `CustomerReturnShipmentReceivedEvent` is published → Commerce processes the refund → Pay initiates the refund payout.

---

## 2. Domain Entities & Aggregates

### `shipping` Submodule

**`Shipment` (Aggregate Root)**
- **Fields**:
  - `id`: Long, `organizationId`: Long, `trackingNumber`: String
  - `originAddress`: String, `destinationAddress`: String
  - `senderId`: Long, `receiverId`: Long
  - `weight`: BigDecimal, `dimensions`: String
  - `declaredValue`: `Money` (for insurance valuation)
  - `insuranceValue`: `Money` (nullable — set if shipment is insured)
  - `proofOfDeliveryUrl`: String (nullable — required to confirm delivery)
  - `riderId`: Long (nullable), `vehicleId`: Long (nullable)
  - `status`: `ShipmentStatus` (CREATED, ASSIGNED, IN_TRANSIT, DELIVERED, RETURNED, FAILED)
  - `currentLocation`: String (nullable)
  - `dispatchedAt`, `deliveredAt`: ZonedDateTime (nullable)
  - `notes`: String (nullable)
- **Methods**:
  - `assignRider(Long riderId, Long vehicleId)` — publishes `ShipmentAssignedEvent`
  - `dispatch()` — publishes `ShipmentDispatchedEvent`
  - `updateLocation(String location)` — appends `ShipmentTrackingHistory` entry
  - `markDelivered(String proofOfDeliveryUrl)` — publishes `ShipmentDeliveredEvent`
  - `markFailed(String reason)` — publishes `ShipmentFailedEvent`
  - `initiateReturn(String reason)` — publishes `ShipmentReturnInitiatedEvent`

**`ShipmentTrackingHistory` (Entity)**
Immutable append-only log.
- **Fields**: `id`, `shipmentId`, `location`: String, `status`: `ShipmentStatus`, `recordedAt`: ZonedDateTime

**`Waybill` (Entity)**
- **Fields**: `id`, `shipmentId`, `barcode`: String, `instructions`: String

**`ShipmentRoute` (Entity)**
- **Fields**: `id`, `shipmentId`, `stops`: `List<RouteStop>` (ordered), `estimatedDistanceKm`: BigDecimal, `estimatedDurationMinutes`: Integer
- **Methods**: `optimizeStops()`

**`RouteStop` (Entity)**
- **Fields**: `id`, `routeId`, `sequence`: Integer, `address`: String, `shipmentId`: Long, `status` (PENDING, REACHED, SKIPPED)

**`FleetVehicle` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `licensePlate`, `vehicleType` (`VehicleType`: BIKE, VAN, TRUCK), `capacity`: BigDecimal, `status` (ACTIVE, MAINTENANCE, OUT_OF_SERVICE), `currentRiderId`: Long
- **Methods**: `assignRider(Long riderId)`, `releaseRider()`, `markMaintenance()`, `markActive()`

**`DispatchRider` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `userId`: Long, `vehicleId`: Long (nullable), `licenseNumber`: String, `status` (`RiderStatus`: AVAILABLE, ON_DELIVERY, OFFLINE)
- **Methods**: `goOnline()`, `goOffline()`, `assignDelivery(Long shipmentId)`, `completeDelivery()`

### `warehousing` Submodule

**`StockTransfer` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `sourceOutletId`: Long, `destinationOutletId`: Long, `status` (REQUESTED, APPROVED, DISPATCHED, RECEIVED, CANCELLED), `requestedAt`, `receivedAt`
- **Methods**: `addTransferItem(StockTransferItem)`, `approve()`, `dispatch()`, `receive(Long receiverId, List<ReceivedItem>)`, `cancel()`

**`StockTransferItem` (Entity)**
- **Fields**: `id`, `transferId`, `productId`, `requestedQty`, `dispatchedQty`, `receivedQty`

### `returns` Submodule

**`CustomerReturnShipment` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `originalSalesOrderId`: Long, `customerId`: Long, `originAddress`: String, `destinationOutletId`: Long, `trackingNumber`: String, `status` (CREATED, IN_TRANSIT, RECEIVED, REJECTED), `receivedAt`
- **Methods**: `dispatch()`, `markReceived()`, `reject(String reason)`

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `ShipmentCreatedEvent` | Shipment created | `notifications` (notify recipient with tracking number) |
| `ShipmentAssignedEvent` | Rider assigned | `notifications` (notify rider) |
| `ShipmentDispatchedEvent` | Rider picks up shipment | `notifications` (notify recipient: "Your order is on the way") |
| `ShipmentLocationUpdatedEvent` | Location checkpoint added | `notifications` (real-time tracking update) |
| `ShipmentDeliveredEvent` | Delivery confirmed with POD | `commerce` (add stock if PO delivery), `accounting` (confirm COD entry if applicable) |
| `ShipmentFailedEvent` | Delivery failed | `commerce` (mark order failed), `notifications` |
| `ShipmentReturnInitiatedEvent` | Failed delivery being returned | `notifications` |
| `StockTransferRequestedEvent` | Transfer request created | Internal (approval workflow) |
| `StockTransferApprovedEvent` | Transfer approved by manager | Internal (triggers dispatch) |
| `StockTransferDispatchedEvent` | Goods physically dispatched | `notifications` |
| `StockTransferReceivedEvent` | Goods confirmed at destination | `commerce` (update inventory at both outlets), `pay` (post inter-outlet ledger transaction), `accounting` (post inventory movement entry) |
| `StockTransferCancelledEvent` | Transfer cancelled | `commerce` (release any reserved stock) |
| `CustomerReturnShipmentReceivedEvent` | Return received at outlet | `commerce` (approve return, restore stock, initiate refund) |

---

## 4. Exceptions & Errors

**`LogisticsErrorCode`**:
- `SHIPMENT_NOT_FOUND`, `INVALID_SHIPMENT_STATE`
- `VEHICLE_NOT_FOUND`, `VEHICLE_UNAVAILABLE`
- `RIDER_NOT_FOUND`, `RIDER_UNAVAILABLE`
- `TRANSFER_NOT_FOUND`, `INVALID_TRANSFER_STATE`
- `PROOF_OF_DELIVERY_REQUIRED`
- `RETURN_SHIPMENT_NOT_FOUND`, `INVALID_RETURN_STATE`

---

## 5. Commands & Use Cases

### Shipment
- `CreateShipmentCommand(orgId, origin, dest, senderId, receiverId, weight, dims, declaredValue, insuranceValue)` → `CreateShipmentUseCase`
- `AssignRiderCommand(shipmentId, riderId, vehicleId)` → `AssignRiderUseCase`
- `DispatchShipmentCommand(shipmentId)` → `DispatchShipmentUseCase`
- `UpdateShipmentLocationCommand(shipmentId, currentLocation)` → `UpdateShipmentLocationUseCase`
- `DeliverShipmentCommand(shipmentId, proofOfDeliveryUrl)` → `DeliverShipmentUseCase`
- `FailShipmentCommand(shipmentId, reason)` → `FailShipmentUseCase`
- `InitiateReturnShipmentCommand(shipmentId, reason)` → `InitiateReturnShipmentUseCase`

### Route Management
- `CreateRouteForShipmentCommand(shipmentId, stops)` → `CreateRouteUseCase`
- `OptimizeRouteCommand(routeId)` → `OptimizeRouteUseCase`
- `MarkRouteStopReachedCommand(routeId, stopId)` → `MarkRouteStopReachedUseCase`

### Fleet & Riders
- `RegisterVehicleCommand(orgId, licensePlate, vehicleType, capacity)` → `RegisterVehicleUseCase`
- `UpdateVehicleStatusCommand(vehicleId, status)` → `UpdateVehicleStatusUseCase`
- `RegisterRiderCommand(orgId, userId, licenseNumber)` → `RegisterRiderUseCase`
- `UpdateRiderStatusCommand(riderId, status)` → `UpdateRiderStatusUseCase`

### Stock Transfers
- `InitiateStockTransferCommand(orgId, sourceId, destId, items)` → `InitiateStockTransferUseCase`
- `ApproveStockTransferCommand(transferId)` → `ApproveStockTransferUseCase`
- `DispatchStockTransferCommand(transferId)` → `DispatchStockTransferUseCase`
- `ReceiveStockTransferCommand(transferId, receivedItems)` → `ReceiveStockTransferUseCase`
- `CancelStockTransferCommand(transferId, reason)` → `CancelStockTransferUseCase`

### Customer Returns
- `CreateCustomerReturnShipmentCommand(orgId, salesOrderId, customerId, originAddress, destOutletId)` → `CreateCustomerReturnShipmentUseCase`
- `ReceiveCustomerReturnCommand(returnShipmentId)` → `ReceiveCustomerReturnUseCase`
- `RejectCustomerReturnCommand(returnShipmentId, reason)` → `RejectCustomerReturnUseCase`

---

## 6. Queries

- `TrackShipmentQuery(String trackingNumber)` → `ShipmentTrackingResult` (full history + current status)
- `ListShipmentsQuery(orgId, status, riderId)` → `List<ShipmentResult>`
- `GetShipmentDetailsQuery(shipmentId)` → `ShipmentDetailsResult`
- `ListVehiclesQuery(orgId, status)`, `ListRidersQuery(orgId, status)`
- `GetRiderActiveDeliveryQuery(riderId)` → active `ShipmentResult`
- `ListStockTransfersQuery(orgId, outletId, direction: INCOMING/OUTGOING, status)`
- `GetStockTransferDetailsQuery(transferId)`
- `ListCustomerReturnShipmentsQuery(orgId, status)`

---

## 7. Listeners

- **`StockTransferDispatchedListener`**: Internal. On `StockTransferDispatchedEvent`, optionally creates a `Shipment` record for tracking if a rider is assigned to the transfer.
- **`PurchaseOrderSentListener`**: Listens to `PurchaseOrderSentEvent` (from `commerce`). Creates a planned inbound shipment from the supplier's location to the receiving outlet.

---

## 8. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `Shipment`, `FleetVehicle`, and `DispatchRider`.
- **Pessimistic Locking (`@Lock(PESSIMISTIC_WRITE)`)**: Applied to `StockTransfer` during `receive()` to handle concurrent partial receipts. Applied to `DispatchRider` during `assignDelivery()` to prevent assigning the same rider to two concurrent shipments.

### Inbox & Outbox Patterns
- **Outbox**: Guarantees delivery of `ShipmentDeliveredEvent` and `StockTransferReceivedEvent` — these events trigger financial movements in Pay and Accounting and must not be lost.
- **Inbox (`EventDeliveryTracker`)**: Ensures `PurchaseOrderSentEvent` from Commerce creates exactly one planned inbound shipment.
