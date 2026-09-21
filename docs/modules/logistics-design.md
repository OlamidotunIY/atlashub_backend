# Logistics Module Design (`atlashub-logistics`)

## Role & Purpose

The Logistics module is the **movement engine** of AtlasHub. It manages everything that physically moves: packages dispatched from warehouses, riders on the road, inter-outlet stock transfers, and return shipments coming back in.

**Atlas Logistics operates in three distinct modes**, and a single organization can use more than one mode simultaneously:

1. **Internal Fleet Mode** — A business manages its own vehicles and drivers for internal logistics (e.g., a supermarket chain dispatching deliveries from its central warehouse to its branches, or delivering online orders to customers).

2. **Logistics-as-a-Service (LaaS) Mode** — The organization IS a logistics company offering delivery services to other businesses as their primary product (e.g., GIG Logistics, Kwik Delivery). These organizations onboard shipper clients, accept delivery requests from them, and dispatch their fleet accordingly.

3. **Marketplace / Driver Network Mode** (Near-future) — A platform connects independent drivers to delivery requesters (like inDrive or Bolt's delivery product). MVP will design and document this model but implement basic manual dispatch; auto-matching algorithms come in the next iteration.

Additionally, Atlas Logistics supports **3PL (Third-Party Logistics)** integrations — organizations can route deliveries through external carriers like DHL or GIG Logistics when their own fleet is unavailable or the delivery is outside their coverage area.

---

## 1. Features

### Shipment Creation & Tracking
`CreateShipmentUseCase` creates a `Shipment` with a unique tracking number. Every status change is recorded in an immutable `ShipmentTrackingHistory`. Tracking is accessible publicly by tracking number — no authentication required.

### Proof of Delivery (POD)
Delivery cannot be confirmed without a proof: a photo URL submitted by the rider (`proofOfDeliveryUrl`). `MarkDeliveredUseCase` rejects calls without this field. This prevents fraudulent delivery confirmations.

### Dispatch Assignment
**MVP: Manual Dispatch** — A dispatcher assigns a rider from the available pool.
**Future: Auto-Dispatch** — The system automatically selects the nearest available rider using geolocation + capacity matching.

### Route Optimization
For multi-stop deliveries, `OptimizeRouteUseCase` reorders stops using a nearest-neighbor algorithm to minimize total travel distance. Riders mark stops as REACHED or SKIPPED in real time.

### Fleet & Rider Management
- Organizations register their own vehicles (`FleetVehicle`) and delivery personnel (`DispatchRider`)
- Vehicles have types (BIKE, VAN, TRUCK) and weight capacity
- Riders have an AVAILABLE/ON_DELIVERY/OFFLINE status
- Assignment logic prevents assigning a rider to two concurrent shipments (pessimistic lock)

### 3PL Integration (External Carrier)
When a shipment needs to be dispatched through an external carrier:
- `Route3PLDeliveryUseCase` calls the carrier's API via the `ThirdPartyCarrierPort` adapter
- The carrier's tracking reference is stored on the `Shipment`
- The carrier sends updates via webhook; AtlasHub translates them into internal `ShipmentLocationUpdatedEvent`s

**Supported 3PL carriers (MVP):**
- GIG Logistics
- DHL (via their API)
- Any carrier with a standard REST API can be added via the `ThirdPartyCarrierPort` abstraction

### LaaS Mode — Shipper Client Management
When an organization operates as a logistics company:
- **Shipper Clients** are the businesses that use the logistics company's services
- Each shipper client has an API key to submit delivery requests programmatically (via HMAC auth)
- Pricing is configured per shipper client (zone-based, weight-based, or flat rate)
- Invoicing for shipper clients is separate from AtlasHub billing (it's B2B billing between the logistics company and their clients)

### Pricing Engine
Delivery fees are calculated based on configurable rules:
- **Flat Rate**: Fixed fee regardless of distance or weight
- **Zone-Based**: Fee depends on origin and destination zone
- **Weight-Based**: Fee per kg or per kg-band
- **Distance-Based**: Fee per kilometer (future, requires mapping API integration)

### Inter-Outlet Stock Transfers
When Commerce raises a `StockTransferRequestedEvent`, Logistics creates a `Shipment` to physically move the goods between outlets. On delivery confirmation (`MarkDeliveredUseCase`), `ShipmentDeliveredEvent` is published → Commerce updates inventory at both outlets.

### Reverse Logistics
Customer return shipments are tracked as `ReturnShipment`. The rider picks up goods from the customer and delivers them to the designated outlet. On receipt, `ReturnShipmentReceivedEvent` is published → Commerce approves the return and triggers refund via Pay.

### Warehousing
The `warehouse` submodule manages stock held in centralized warehouses before distribution. Stock at a warehouse is tracked separately from outlet stock.

---

## 2. Domain Entities & Aggregates

### `shipping` Submodule

**`Shipment` (Aggregate Root)**
```
Shipment
├── id: Long
├── organizationId: Long
├── trackingNumber: String              ← unique, system-generated (e.g., "ATL-2026-ABC123")
├── type: ShipmentType                  ← CUSTOMER_ORDER, INTER_OUTLET_TRANSFER, SUPPLIER_DELIVERY, RETURN
├── shipperClientId: Long               ← nullable — set in LaaS mode
├── originAddress: Address
├── destinationAddress: Address
├── senderId: Long
├── receiverId: Long
├── weight: BigDecimal                  ← kg
├── dimensions: Dimensions              ← value object: length, width, height in cm
├── declaredValue: Money
├── insuranceValue: Money               ← nullable
├── riderId: Long                       ← nullable until assigned
├── vehicleId: Long                     ← nullable until assigned
├── carrier3PLCode: String              ← nullable — set if dispatched via 3PL
├── carrier3PLReference: String         ← nullable — 3PL tracking reference
├── proofOfDeliveryUrl: String          ← nullable until delivered
├── status: ShipmentStatus              ← CREATED, ASSIGNED, IN_TRANSIT, DELIVERED, FAILED, RETURNING
├── currentLocation: String             ← nullable, last known location
├── deliveryFee: Money
├── pricingRuleId: Long                 ← which pricing rule was used
├── notes: String
├── dispatchedAt: ZonedDateTime         ← nullable
├── deliveredAt: ZonedDateTime          ← nullable
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `assignRider(Long riderId, Long vehicleId)` → validates rider is AVAILABLE → registers `ShipmentAssignedEvent`
- `dispatch()` → validates rider assigned → transitions IN_TRANSIT → registers `ShipmentDispatchedEvent`
- `updateLocation(String location)` → appends to tracking history → registers `ShipmentLocationUpdatedEvent`
- `markDelivered(String podUrl)` → validates podUrl not null → transitions DELIVERED → registers `ShipmentDeliveredEvent`
- `markFailed(String reason)` → registers `ShipmentFailedEvent`
- `initiateReturn(String reason)` → transitions RETURNING → registers `ShipmentReturnInitiatedEvent`
- `route3PL(String carrierCode, String carrierRef)` → records 3PL routing

**Domain Rules:**
- Cannot call `dispatch()` before `assignRider()`
- Cannot call `markDelivered()` without `proofOfDeliveryUrl`
- Cannot transition from DELIVERED to any other status

---

**`ShipmentTrackingHistory` (Entity — append-only)**
```
ShipmentTrackingHistory
├── id: Long
├── shipmentId: Long
├── location: String
├── status: ShipmentStatus
├── description: String                 ← e.g., "Package arrived at Lagos sorting facility"
└── recordedAt: ZonedDateTime
```

**`ShipmentRoute` (Entity)**
```
ShipmentRoute
├── id: Long
├── shipmentId: Long
├── stops: List<RouteStop>
├── estimatedDistanceKm: BigDecimal
└── estimatedDurationMinutes: Integer
```

**`RouteStop` (Entity)**: `id`, `routeId`, `sequence`, `address: Address`, `shipmentId: Long` (for multi-drop), `status` (PENDING, REACHED, SKIPPED), `reachedAt: ZonedDateTime`

---

**`FleetVehicle` (Aggregate Root)**
```
FleetVehicle
├── id: Long
├── organizationId: Long
├── licensePlate: String
├── vehicleType: VehicleType            ← BIKE, CAR, VAN, TRUCK
├── maxWeightKg: BigDecimal
├── status: VehicleStatus               ← ACTIVE, MAINTENANCE, OUT_OF_SERVICE
├── currentRiderId: Long                ← nullable
└── registeredAt: ZonedDateTime
```

---

**`DispatchRider` (Aggregate Root)**
```
DispatchRider
├── id: Long
├── organizationId: Long
├── userId: Long
├── vehicleId: Long                     ← nullable — pre-assigned vehicle
├── licenseNumber: String
├── status: RiderStatus                 ← AVAILABLE, ON_DELIVERY, OFFLINE
├── currentShipmentId: Long            ← nullable
└── onboardedAt: ZonedDateTime
```

**Business Methods:**
- `goOnline()`, `goOffline()`
- `assignDelivery(Long shipmentId)` → pessimistic lock; guards: status must be AVAILABLE
- `completeDelivery()` → sets status AVAILABLE, clears `currentShipmentId`

---

### `laas` Submodule (Logistics-as-a-Service)

**`ShipperClient` (Aggregate Root)**
```
ShipperClient
├── id: Long
├── logisticsOrgId: Long                ← the logistics company (organization)
├── clientName: String
├── contactEmail: EmailAddress
├── contactPhone: PhoneNumber
├── apiPublicKey: String                ← for programmatic API access
├── status: ClientStatus                ← ACTIVE, SUSPENDED, TERMINATED
├── defaultPricingRuleId: Long
└── createdAt: ZonedDateTime
```

**`PricingRule` (Aggregate Root)**
```
PricingRule
├── id: Long
├── organizationId: Long
├── name: String
├── type: PricingType                   ← FLAT, ZONE_BASED, WEIGHT_BASED
├── currency: Currency
├── config: PricingConfig               ← varies by type (see below)
└── isActive: Boolean
```

**`ZoneBasedPricingConfig` (Value Object)**: `List<ZoneRate>` where each `ZoneRate` has `originZone`, `destinationZone`, `baseFee: Money`, `additionalKgFee: Money`

**`WeightBasedPricingConfig` (Value Object)**: `List<WeightBand>` where each `WeightBand` has `minWeightKg`, `maxWeightKg`, `fee: Money`

---

### `warehousing` Submodule

**`Warehouse` (Aggregate Root)**
```
Warehouse
├── id: Long
├── organizationId: Long
├── name: String
├── address: Address
├── managerId: Long
└── status: WarehouseStatus             ← ACTIVE, INACTIVE
```

**`WarehouseInventory` (Aggregate Root)**: `id`, `warehouseId`, `productId`, `quantity`, `reservedQuantity`

**`StockTransfer` (Aggregate Root)**
```
StockTransfer
├── id: Long
├── organizationId: Long
├── sourceId: Long                      ← outletId or warehouseId
├── sourceType: LocationType            ← OUTLET | WAREHOUSE
├── destinationId: Long
├── destinationType: LocationType
├── status: TransferStatus              ← REQUESTED, APPROVED, DISPATCHED, RECEIVED, CANCELLED
├── shipmentId: Long                    ← nullable — linked Shipment for tracking
├── items: List<StockTransferItem>
├── requestedAt: ZonedDateTime
└── receivedAt: ZonedDateTime
```

---

### `returns` Submodule

**`ReturnShipment` (Aggregate Root)**
```
ReturnShipment
├── id: Long
├── organizationId: Long
├── originalSalesOrderId: Long
├── customerId: Long
├── originAddress: Address              ← customer's address
├── destinationOutletId: Long
├── shipmentId: Long                    ← linked Shipment for tracking
├── trackingNumber: String
├── status: ReturnStatus                ← CREATED, IN_TRANSIT, RECEIVED, REJECTED
└── receivedAt: ZonedDateTime
```

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `ShipmentCreatedEvent` | Shipment created | `notifications` (notify recipient with tracking number) |
| `ShipmentAssignedEvent` | Rider assigned | `notifications` (notify rider via push/SMS) |
| `ShipmentDispatchedEvent` | Rider picks up package | `notifications` (notify recipient: "On the way!") |
| `ShipmentLocationUpdatedEvent` | Location checkpoint | `notifications` (real-time tracking update via WebSocket) |
| `ShipmentDeliveredEvent` | Delivery confirmed with POD | `commerce` (add stock if stock transfer), `accounting` (confirm COD entry), `notifications` |
| `ShipmentFailedEvent` | Delivery failed | `commerce` (mark order failed), `notifications` |
| `ShipmentReturnInitiatedEvent` | Failed delivery returning | `notifications` |
| `StockTransferDispatched` | Goods sent to destination | `notifications` |
| `StockTransferReceivedEvent` | Transfer confirmed at destination | `commerce` (update inventory at both locations), `pay` (post inter-outlet ledger entry), `accounting` |
| `ReturnShipmentReceivedEvent` | Return received at outlet | `commerce` (approve return, restore stock, initiate refund) |

---

## 4. Exceptions & Errors

**`LogisticsErrorCode`**:
- `SHIPMENT_NOT_FOUND`, `INVALID_SHIPMENT_STATUS`
- `RIDER_NOT_FOUND`, `RIDER_UNAVAILABLE`, `RIDER_ALREADY_ON_DELIVERY`
- `VEHICLE_NOT_FOUND`, `VEHICLE_UNAVAILABLE`
- `PROOF_OF_DELIVERY_REQUIRED`
- `TRANSFER_NOT_FOUND`, `INVALID_TRANSFER_STATE`
- `RETURN_NOT_FOUND`, `INVALID_RETURN_STATE`
- `CARRIER_NOT_SUPPORTED`, `CARRIER_API_ERROR`
- `PRICING_RULE_NOT_FOUND`, `SHIPPER_CLIENT_NOT_FOUND`
- `WAREHOUSE_NOT_FOUND`

---

## 5. Commands & Use Cases

### Shipment
- `CreateShipmentCommand(orgId, type, origin, dest, senderId, receiverId, weight, dims, declaredValue, pricingRuleId)` → `CreateShipmentUseCase`
- `AssignRiderCommand(shipmentId, riderId, vehicleId)` → `AssignRiderUseCase`
- `DispatchShipmentCommand(shipmentId)` → `DispatchShipmentUseCase`
- `UpdateLocationCommand(shipmentId, location, description)` → `UpdateShipmentLocationUseCase`
- `MarkDeliveredCommand(shipmentId, podUrl)` → `MarkDeliveredUseCase`
- `MarkFailedCommand(shipmentId, reason)` → `MarkFailedUseCase`
- `Route3PLCommand(shipmentId, carrierCode)` → `Route3PLDeliveryUseCase`

### Route Management
- `CreateRouteCommand(shipmentId, stops)` → `CreateRouteUseCase`
- `OptimizeRouteCommand(routeId)` → `OptimizeRouteUseCase`
- `MarkStopReachedCommand(routeId, stopId)` → `MarkRouteStopReachedUseCase`

### Fleet & Riders
- `RegisterVehicleCommand(orgId, licensePlate, type, maxWeightKg)` → `RegisterVehicleUseCase`
- `UpdateVehicleStatusCommand(vehicleId, status)` → `UpdateVehicleStatusUseCase`
- `RegisterRiderCommand(orgId, userId, licenseNumber)` → `RegisterRiderUseCase`
- `SetRiderOnlineCommand(riderId)`, `SetRiderOfflineCommand(riderId)`

### LaaS
- `RegisterShipperClientCommand(orgId, clientName, email, phone)` → `RegisterShipperClientUseCase`
- `CreatePricingRuleCommand(orgId, name, type, config)` → `CreatePricingRuleUseCase`
- `SuspendShipperClientCommand(clientId, reason)` → `SuspendShipperClientUseCase`

### Stock Transfers & Returns
- `ApproveStockTransferCommand(transferId)` → `ApproveStockTransferUseCase`
- `ReceiveStockTransferCommand(transferId, receivedItems)` → `ReceiveStockTransferUseCase`
- `CreateReturnShipmentCommand(orgId, salesOrderId, customerId, originAddress, destOutletId)` → `CreateReturnShipmentUseCase`
- `ReceiveReturnShipmentCommand(returnShipmentId)` → `ReceiveReturnShipmentUseCase`
- `RejectReturnShipmentCommand(returnShipmentId, reason)` → `RejectReturnShipmentUseCase`

---

## 6. Queries

- `TrackShipmentQuery(trackingNumber)` → `ShipmentTrackingResult` (full history + current status) — **public, no auth**
- `ListShipmentsQuery(orgId, type, status, riderId, dateFrom, dateTo)` → `Page<ShipmentSummaryResult>`
- `GetShipmentDetailsQuery(shipmentId)` → `ShipmentDetailsResult`
- `ListRidersQuery(orgId, status)` → `List<RiderResult>`
- `GetRiderActiveDeliveryQuery(riderId)` → `ShipmentResult`
- `ListVehiclesQuery(orgId, status)` → `List<VehicleResult>`
- `ListStockTransfersQuery(orgId, locationId, direction, status)` → `List<TransferResult>`
- `CalculateDeliveryFeeQuery(orgId, pricingRuleId, weight, origin, destination)` → `DeliveryFeeResult`
- `ListShipperClientsQuery(orgId)` → `List<ShipperClientResult>`

---

## 7. Listeners

- **`StockTransferApprovedListener`** (internal): Creates a `Shipment` record for the transfer to enable tracking.
- **`PurchaseOrderSentListener`**: `PurchaseOrderSentEvent` from commerce — creates a planned inbound shipment from the supplier's location.
- **`OnlineOrderCreatedListener`**: `OnlineOrderCreatedEvent` from commerce — creates a `Shipment` for customer delivery.

---

## 8. Distributed Architecture

### Locking Strategy
- **Pessimistic Locking**: `DispatchRider` during `assignDelivery()` — prevents assigning the same rider to two concurrent shipments
- **Pessimistic Locking**: `StockTransfer` during `receive()` — handles concurrent partial receipt
- **Optimistic Locking**: `Shipment`, `FleetVehicle`, `ReturnShipment`

### Outbox & Inbox
- **Outbox**: `ShipmentDeliveredEvent`, `StockTransferReceivedEvent` — these trigger financial movements in Pay and Accounting and MUST NOT be lost
- **Inbox**: `PurchaseOrderSentEvent` — idempotent; a replay must not create duplicate planned shipments

---

## 9. Future Roadmap (Post-MVP)

- **Auto-Dispatch Algorithm**: Geolocation-based nearest available rider matching
- **Driver Network / Marketplace Mode**: Independent drivers register on the platform; delivery requesters post jobs; drivers accept and earn per delivery
- **Dynamic Pricing**: Surge pricing during peak hours or high demand zones
- **Earnings & Settlement for Network Drivers**: Wallet per driver, weekly disbursement
- **Driver Ratings & Reviews**: Two-sided ratings (driver ↔ recipient)
- **Advanced Route Optimization**: Integration with Google Maps/HERE Maps Distance Matrix API
- **Cash-on-Delivery (COD)**: Rider collects cash from recipient; cash reconciled against rider's COD wallet
- **Real-Time Driver Location Tracking**: WebSocket broadcast of live GPS coordinates to recipient
