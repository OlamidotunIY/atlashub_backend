# Logistics Module Design (`atlashub-logistics`)

## 1. Domain Entities & Aggregates

### `shipping` Submodule
**`Shipment` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `trackingNumber`, `originAddress`, `destinationAddress`, `senderId`, `receiverId`, `weight`, `dimensions`, `status` (CREATED, ASSIGNED, IN_TRANSIT, DELIVERED, RETURNED, FAILED), `dispatchedAt`, `deliveredAt`
- **Methods**: `assignRider(Long riderId)`, `dispatch()`, `updateLocation(String location)`, `markDelivered()`, `markFailed(String reason)`

**`Waybill` (Entity)**
- **Fields**: `id`, `shipmentId`, `barcode`, `instructions`

**`FleetVehicle` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `licensePlate`, `vehicleType` (BIKE, VAN, TRUCK), `capacity`, `status` (ACTIVE, MAINTENANCE, OUT_OF_SERVICE)

**`DispatchRider` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `userId`, `vehicleId`, `licenseNumber`, `status` (AVAILABLE, ON_DELIVERY, OFFLINE)

### `warehousing` Submodule
**`StockTransfer` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `sourceOutletId`, `destinationOutletId`, `status` (REQUESTED, APPROVED, DISPATCHED, RECEIVED, CANCELLED), `requestedAt`, `receivedAt`
- **Methods**: `addTransferItem(...)`, `approve()`, `dispatch()`, `receive(Long receiverId)`

**`StockTransferItem` (Entity)**
- **Fields**: `id`, `transferId`, `productId`, `requestedQty`, `dispatchedQty`, `receivedQty`

## 2. Domain Events
- `ShipmentCreatedEvent(Long shipmentId, String trackingNumber)`
- `ShipmentDispatchedEvent(Long shipmentId)`
- `ShipmentDeliveredEvent(Long shipmentId)`
- `ShipmentFailedEvent(Long shipmentId, String reason)`
- `StockTransferRequestedEvent(Long transferId, Long sourceId, Long destId)`
- `StockTransferDispatchedEvent(Long transferId)`
- `StockTransferReceivedEvent(Long transferId)`

## 3. Exceptions & Errors
**`LogisticsErrorCode`**:
- `SHIPMENT_NOT_FOUND`, `INVALID_SHIPMENT_STATE`
- `VEHICLE_NOT_FOUND`, `VEHICLE_UNAVAILABLE`
- `RIDER_NOT_FOUND`, `RIDER_UNAVAILABLE`
- `TRANSFER_NOT_FOUND`, `INVALID_TRANSFER_STATE`

## 4. Commands & Use Cases
- `CreateShipmentCommand(origin, dest, weight, dims)` -> `CreateShipmentUseCase` (Generates tracking number and Waybill).
- `AssignRiderCommand(shipmentId, riderId)` -> `AssignRiderUseCase`
- `UpdateShipmentLocationCommand(shipmentId, currentLocation)` -> `UpdateShipmentLocationUseCase`
- `DeliverShipmentCommand(shipmentId, proofOfDelivery)` -> `DeliverShipmentUseCase`
- `FailShipmentCommand(shipmentId, reason)`
- `RegisterVehicleCommand(...)`, `UpdateVehicleStatusCommand(...)`
- `RegisterRiderCommand(...)`, `UpdateRiderStatusCommand(...)`
- `InitiateStockTransferCommand(sourceId, destId, items)` -> `InitiateStockTransferUseCase`
- `ApproveStockTransferCommand(transferId)`
- `DispatchStockTransferCommand(transferId)`
- `ReceiveStockTransferCommand(transferId, receivedItemsMap)` -> `ReceiveStockTransferUseCase`

## 5. Queries
- `TrackShipmentQuery(String trackingNumber)` -> `ShipmentTrackingResult`
- `ListShipmentsQuery(orgId, status)` -> `List<ShipmentResult>`
- `ListVehiclesQuery(orgId)`, `ListRidersQuery(orgId)`
- `ListStockTransfersQuery(orgId, outletId, type: INCOMING/OUTGOING)`
- `GetStockTransferDetailsQuery(transferId)`

## 6. Listeners
- `StockTransferReceivedListener`: Listens to `StockTransferReceivedEvent`, triggers `AdjustStockCommand` in Commerce module to increment destination inventory and decrement source inventory.

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Optimistic Locking (@Version)**: Applied to Shipment and FleetVehicle.
- **Pessimistic Locking (@Lock(PESSIMISTIC_WRITE))**: Applied to StockTransfer to handle concurrent partial receipts.

### Inbox & Outbox Patterns
- **Outbox**: Guarantees delivery of ShipmentDeliveredEvent to trigger inventory updates in Commerce.
- **Inbox (EventDeliveryTracker)**: Tracks OrderPreparedEvent to prevent duplicate shipment creation.
