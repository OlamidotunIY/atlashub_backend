# Logistics Module Design (`atlashub-logistics`)

## 1. Domain Entities & Aggregates

### `shipping` Submodule

**`Shipment` (Aggregate Root)**
- **Fields**:
  - `id`: Long, `organizationId`: Long, `trackingNumber`: String
  - `originAddress`: String, `destinationAddress`: String
  - `senderId`: Long, `receiverId`: Long
  - `weight`: BigDecimal, `dimensions`: String (e.g., "30x20x10 cm")
  - `declaredValue`: **`Money`** (for insurance purposes)
  - `insuranceValue`: **`Money`** (nullable — set if shipment is insured)
  - `proofOfDeliveryUrl`: String (nullable — file URL of signed POD image/PDF)
  - `riderId`: Long (nullable — assigned after creation)
  - `vehicleId`: Long (nullable)
  - `status`: `ShipmentStatus` (CREATED, ASSIGNED, IN_TRANSIT, DELIVERED, RETURNED, FAILED)
  - `currentLocation`: String (nullable — last known checkpoint)
  - `dispatchedAt`: ZonedDateTime (nullable)
  - `deliveredAt`: ZonedDateTime (nullable)
  - `notes`: String (nullable)
- **Methods**:
  - `assignRider(Long riderId, Long vehicleId)`
  - `dispatch()`
  - `updateLocation(String location)` — appends to `ShipmentTrackingHistory`
  - `markDelivered(String proofOfDeliveryUrl)`
  - `markFailed(String reason)`
  - `initiateReturn(String reason)`

**`ShipmentTrackingHistory` (Entity)**
Immutable append-only log of location updates.
- **Fields**: `id`, `shipmentId`, `location`: String, `status`: `ShipmentStatus`, `recordedAt`: ZonedDateTime

**`Waybill` (Entity)**
- **Fields**: `id`, `shipmentId`, `barcode`: String, `instructions`: String

**`ShipmentRoute` (Entity)**
Represents a planned multi-stop route for optimized delivery.
- **Fields**: `id`, `shipmentId`, `stops`: `List<RouteStop>` (ordered), `estimatedDistanceKm`: BigDecimal, `estimatedDurationMinutes`: Integer
- **Methods**: `optimizeStops()` (reorders `stops` using a nearest-neighbor or external route optimization algorithm)

**`RouteStop` (Entity)**
- **Fields**: `id`, `routeId`, `sequence`: Integer, `address`: String, `shipmentId`: Long, `status` (PENDING, REACHED, SKIPPED)

**`FleetVehicle` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `licensePlate`, `vehicleType` (`VehicleType`: BIKE, VAN, TRUCK), `capacity`: BigDecimal (kg), `status` (ACTIVE, MAINTENANCE, OUT_OF_SERVICE), `currentRiderId`: Long (nullable)
- **Methods**: `assignRider(Long riderId)`, `releaseRider()`, `markMaintenance()`, `markActive()`

**`DispatchRider` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `userId`: Long, `vehicleId`: Long (nullable), `licenseNumber`: String, `status` (`RiderStatus`: AVAILABLE, ON_DELIVERY, OFFLINE)
- **Methods**: `goOnline()`, `goOffline()`, `assignDelivery(Long shipmentId)`, `completeDelivery()`

### `warehousing` Submodule

**`StockTransfer` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `sourceOutletId`: Long, `destinationOutletId`: Long, `status` (REQUESTED, APPROVED, DISPATCHED, RECEIVED, CANCELLED), `requestedAt`: ZonedDateTime, `receivedAt`: ZonedDateTime (nullable)
- **Methods**: `addTransferItem(StockTransferItem)`, `approve()`, `dispatch()`, `receive(Long receiverId, List<ReceivedItem> items)`, `cancel()`

**`StockTransferItem` (Entity)**
- **Fields**: `id`, `transferId`, `productId`, `requestedQty`, `dispatchedQty`, `receivedQty`

### `returns` Submodule

**`CustomerReturnShipment` (Aggregate Root)**
Represents a reverse-logistics shipment for a customer returning goods.
- **Fields**: `id`, `organizationId`, `originalSalesOrderId`: Long, `customerId`: Long, `originAddress`: String, `destinationOutletId`: Long, `trackingNumber`: String, `status` (CREATED, IN_TRANSIT, RECEIVED, REJECTED), `receivedAt`: ZonedDateTime (nullable)
- **Methods**: `dispatch()`, `markReceived()`, `reject(String reason)`

## 2. Domain Events
- `ShipmentCreatedEvent(Long shipmentId, String trackingNumber)`
- `ShipmentAssignedEvent(Long shipmentId, Long riderId)`
- `ShipmentDispatchedEvent(Long shipmentId)`
- `ShipmentLocationUpdatedEvent(Long shipmentId, String currentLocation)`
- `ShipmentDeliveredEvent(Long shipmentId, String proofOfDeliveryUrl)`
- `ShipmentFailedEvent(Long shipmentId, String reason)`
- `ShipmentReturnInitiatedEvent(Long shipmentId)`
- `StockTransferRequestedEvent(Long transferId, Long sourceId, Long destId)`
- `StockTransferApprovedEvent(Long transferId)`
- `StockTransferDispatchedEvent(Long transferId)`
- `StockTransferReceivedEvent(Long transferId, Long destinationOutletId)`
- `StockTransferCancelledEvent(Long transferId)`
- `CustomerReturnShipmentReceivedEvent(Long returnShipmentId, Long salesOrderId)`

## 3. Exceptions & Errors
**`LogisticsErrorCode`**:
- `SHIPMENT_NOT_FOUND`, `INVALID_SHIPMENT_STATE`
- `VEHICLE_NOT_FOUND`, `VEHICLE_UNAVAILABLE`
- `RIDER_NOT_FOUND`, `RIDER_UNAVAILABLE`
- `TRANSFER_NOT_FOUND`, `INVALID_TRANSFER_STATE`
- `PROOF_OF_DELIVERY_REQUIRED`
- `RETURN_SHIPMENT_NOT_FOUND`, `INVALID_RETURN_STATE`

## 4. Commands & Use Cases

### Shipment
- `CreateShipmentCommand(orgId, origin, dest, senderId, receiverId, weight, dims, declaredValue, insuranceValue)` → `CreateShipmentUseCase` (Generates `trackingNumber` and `Waybill`.)
- `AssignRiderCommand(shipmentId, riderId, vehicleId)` → `AssignRiderUseCase`
- `DispatchShipmentCommand(shipmentId)` → `DispatchShipmentUseCase`
- `UpdateShipmentLocationCommand(shipmentId, currentLocation)` → `UpdateShipmentLocationUseCase` (Appends `ShipmentTrackingHistory` entry.)
- `DeliverShipmentCommand(shipmentId, proofOfDeliveryUrl)` → `DeliverShipmentUseCase` (Requires `proofOfDeliveryUrl`. Guards: status must be IN_TRANSIT.)
- `FailShipmentCommand(shipmentId, reason)` → `FailShipmentUseCase`
- `InitiateReturnShipmentCommand(shipmentId, reason)` → `InitiateReturnShipmentUseCase`

### Route Management
- `CreateRouteForShipmentCommand(shipmentId, stops)` → `CreateRouteUseCase`
- `OptimizeRouteCommand(routeId)` → `OptimizeRouteUseCase` (Reorders stops for optimal delivery path.)
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
- `ReceiveStockTransferCommand(transferId, receivedItems)` → `ReceiveStockTransferUseCase` (Publishes `StockTransferReceivedEvent`.)
- `CancelStockTransferCommand(transferId, reason)` → `CancelStockTransferUseCase`

### Customer Returns
- `CreateCustomerReturnShipmentCommand(orgId, salesOrderId, customerId, originAddress, destOutletId)` → `CreateCustomerReturnShipmentUseCase`
- `ReceiveCustomerReturnCommand(returnShipmentId)` → `ReceiveCustomerReturnUseCase` (Publishes `CustomerReturnShipmentReceivedEvent` → Commerce processes the refund.)
- `RejectCustomerReturnCommand(returnShipmentId, reason)` → `RejectCustomerReturnUseCase`

## 5. Queries
- `TrackShipmentQuery(String trackingNumber)` → `ShipmentTrackingResult` (Full history + current status.)
- `ListShipmentsQuery(orgId, status, riderId)` → `List<ShipmentResult>`
- `GetShipmentDetailsQuery(shipmentId)` → `ShipmentDetailsResult`
- `ListVehiclesQuery(orgId, status)`, `ListRidersQuery(orgId, status)`
- `GetRiderActiveDeliveryQuery(riderId)` → active `ShipmentResult` (if any)
- `ListStockTransfersQuery(orgId, outletId, direction: INCOMING/OUTGOING, status)`
- `GetStockTransferDetailsQuery(transferId)`
- `ListCustomerReturnShipmentsQuery(orgId, status)`

## 6. Listeners
- `StockTransferReceivedListener`: Listens to `StockTransferReceivedEvent`, triggers `AdjustStockCommand` in Commerce (add stock to destination outlet, confirm deduction from source outlet).
- `CustomerReturnReceivedListener`: Listens to `CustomerReturnShipmentReceivedEvent`, triggers `ApproveCustomerReturnCommand` in Commerce to process the refund.

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `Shipment`, `FleetVehicle`, and `DispatchRider`.
- **Pessimistic Locking (`@Lock(PESSIMISTIC_WRITE)`)**: Applied to `StockTransfer` during `receive()` to handle concurrent partial receipts.
- **Pessimistic Locking**: Applied to `DispatchRider` during `assignDelivery()` to prevent assigning the same rider to two concurrent shipments.

### Inbox & Outbox Patterns
- **Outbox**: Guarantees delivery of `ShipmentDeliveredEvent` and `StockTransferReceivedEvent` to Commerce.
- **Inbox (`EventDeliveryTracker`)**: Ensures `OrderPreparedEvent` from Commerce creates exactly one `Shipment`.
