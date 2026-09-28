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
`CreateShipmentHandler` creates a `Shipment` with a unique tracking number. Every status change is recorded in an immutable `ShipmentTrackingHistory`. Tracking is accessible publicly by tracking number — no authentication required (`@PublicEndpoint`).

### Proof of Delivery (POD)
Delivery cannot be confirmed without a proof: a photo URL submitted by the rider (`proofOfDeliveryUrl`). `MarkDeliveredHandler` rejects calls without this field, throwing `ProofOfDeliveryRequiredException`. This prevents fraudulent delivery confirmations.

### Dispatch Assignment
**MVP: Manual Dispatch** — A dispatcher assigns a rider from the available pool via `AssignRiderHandler`.
**Future: Auto-Dispatch** — The system automatically selects the nearest available rider using geolocation + capacity matching.

### Route Optimization
For multi-stop deliveries, `OptimizeRouteHandler` reorders stops using a nearest-neighbor algorithm to minimize total travel distance. Riders mark stops as REACHED or SKIPPED in real time.

### Fleet & Rider Management
- Organizations register their own vehicles (`FleetVehicle`) and delivery personnel (`DispatchRider`)
- Vehicles have types (BIKE, CAR, VAN, TRUCK) and weight capacity
- Riders have an AVAILABLE/ON_DELIVERY/OFFLINE status
- Assignment logic prevents assigning a rider to two concurrent shipments (pessimistic lock)

### 3PL Integration (External Carrier)
When a shipment needs to be dispatched through an external carrier:
- `Route3PLHandler` calls the carrier's API via the `ThirdPartyCarrierPort` adapter
- The carrier's tracking reference is stored on the `Shipment`
- The carrier sends updates via webhook; AtlasHub translates them into internal `ShipmentLocationUpdatedEvent`s

**Supported 3PL carriers (MVP):**
- GIG Logistics — `GigLogisticsAdapter`
- DHL — `DhlAdapter`
- Any carrier with a standard REST API can be added via the `ThirdPartyCarrierPort` abstraction

### LaaS Mode — Shipper Client Management
When an organization operates as a logistics company:
- **Shipper Clients** are the businesses that use the logistics company's services
- Each shipper client has an API key to submit delivery requests programmatically (via HMAC auth)
- Pricing is configured per shipper client (zone-based, weight-based, or flat rate)

### Pricing Engine
Delivery fees are calculated based on configurable rules:
- **Flat Rate**: Fixed fee regardless of distance or weight
- **Zone-Based**: Fee depends on origin and destination zone
- **Weight-Based**: Fee per kg or per kg-band

### Inter-Outlet Stock Transfers
When Commerce raises a `StockTransferApprovedEvent`, Logistics creates a `Shipment` to physically move the goods between outlets. On delivery confirmation (`MarkDeliveredHandler`), `ShipmentDeliveredEvent` is published → Commerce updates inventory at both outlets.

### Reverse Logistics
Customer return shipments are tracked as `ReturnShipment`. The rider picks up goods from the customer and delivers them to the designated outlet. On receipt, `ReturnShipmentReceivedEvent` is published → Commerce approves the return and triggers refund via Pay.

### Warehousing
The `warehousing` submodule manages stock held in centralized warehouses before distribution. Stock at a warehouse is tracked separately from outlet stock.

---

## 2. Domain Layer

### Package Structure

```
com.atlashub.logistics/
├── shipping/
│   ├── domain/
│   │   ├── entities/
│   │   ├── events/
│   │   ├── exceptions/
│   │   ├── ports/               ← ThirdPartyCarrierPort lives here
│   │   ├── repositories/
│   │   └── valueobject/
│   ├── application/
│   │   ├── commands/
│   │   ├── queries/
│   │   └── port/
│   ├── infrastructure/
│   │   ├── messaging/
│   │   ├── persistence/
│   │   └── services/            ← GigLogisticsAdapter, DhlAdapter
│   └── presentation/
├── laas/
├── warehousing/
└── returns/
```

---

### `shipping` Submodule

#### `Shipment` (Aggregate Root)

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

**Business Methods (on entity — no repository access needed):**
- `assignRider(Long riderId, Long vehicleId)` → validates rider is AVAILABLE → registers `ShipmentAssignedEvent`
- `dispatch()` → validates rider assigned; if not, throws `InvalidShipmentStatusException` → transitions IN_TRANSIT → registers `ShipmentDispatchedEvent`
- `updateLocation(String location, String description)` → appends to tracking history → registers `ShipmentLocationUpdatedEvent`
- `markDelivered(String podUrl)` → validates podUrl not null, throws `ProofOfDeliveryRequiredException` → transitions DELIVERED → registers `ShipmentDeliveredEvent`
- `markFailed(String reason)` → registers `ShipmentFailedEvent`
- `initiateReturn(String reason)` → transitions RETURNING → registers `ShipmentReturnInitiatedEvent`
- `route3PL(String carrierCode, String carrierRef)` → records 3PL routing; throws `CarrierNotSupportedException` if code unrecognized

**Domain Rules:**
- Cannot call `dispatch()` before `assignRider()`
- Cannot call `markDelivered()` without `proofOfDeliveryUrl`
- Cannot transition from DELIVERED to any other status

---

#### `ShipmentTrackingHistory` (Entity — append-only)

```
ShipmentTrackingHistory
├── id: Long
├── shipmentId: Long
├── location: String
├── status: ShipmentStatus
├── description: String                 ← e.g., "Package arrived at Lagos sorting facility"
└── recordedAt: ZonedDateTime
```

---

#### `ShipmentRoute` (Entity)

```
ShipmentRoute
├── id: Long
├── shipmentId: Long
├── stops: List<RouteStop>
├── estimatedDistanceKm: BigDecimal
└── estimatedDurationMinutes: Integer
```

#### `RouteStop` (Entity)

```
RouteStop
├── id: Long
├── routeId: Long
├── sequence: Integer
├── address: Address
├── shipmentId: Long                    ← for multi-drop, links to the individual shipment
├── status: RouteStopStatus             ← PENDING, REACHED, SKIPPED
└── reachedAt: ZonedDateTime            ← nullable
```

---

#### `FleetVehicle` (Aggregate Root)

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

**Business Methods:**
- `markMaintenance()` → transitions to MAINTENANCE; throws `VehicleUnavailableException` if already OUT_OF_SERVICE
- `returnToService()` → transitions to ACTIVE

---

#### `DispatchRider` (Aggregate Root)

```
DispatchRider
├── id: Long
├── organizationId: Long
├── userId: Long
├── vehicleId: Long                     ← nullable — pre-assigned vehicle
├── licenseNumber: String
├── status: RiderStatus                 ← AVAILABLE, ON_DELIVERY, OFFLINE
├── currentShipmentId: Long             ← nullable
└── onboardedAt: ZonedDateTime
```

**Business Methods (on entity):**
- `goOnline()` → transitions to AVAILABLE
- `goOffline()` → validates not ON_DELIVERY; throws `RiderAlreadyOnDeliveryException`
- `assignDelivery(Long shipmentId)` → pessimistic lock guard; throws `RiderUnavailableException` if not AVAILABLE; throws `RiderAlreadyOnDeliveryException` if already assigned → transitions ON_DELIVERY
- `completeDelivery()` → sets status AVAILABLE, clears `currentShipmentId`

---

### `laas` Submodule

#### `ShipperClient` (Aggregate Root)

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

**Business Methods:**
- `suspend(String reason)` → transitions SUSPENDED; throws `ShipperClientNotFoundException` if not found
- `terminate()` → transitions TERMINATED

---

#### `PricingRule` (Aggregate Root)

```
PricingRule
├── id: Long
├── organizationId: Long
├── name: String
├── type: PricingType                   ← FLAT, ZONE_BASED, WEIGHT_BASED
├── currency: CurrencyCode
├── config: PricingConfig               ← varies by type (see below)
└── isActive: Boolean
```

**Value Objects (pricing config):**

`ZoneBasedPricingConfig`: `List<ZoneRate>` where each `ZoneRate` has `originZone: String`, `destinationZone: String`, `baseFee: Money`, `additionalKgFee: Money`

`WeightBasedPricingConfig`: `List<WeightBand>` where each `WeightBand` has `minWeightKg: BigDecimal`, `maxWeightKg: BigDecimal`, `fee: Money`

`FlatPricingConfig`: `flatFee: Money`

---

### `warehousing` Submodule

#### `Warehouse` (Aggregate Root)

```
Warehouse
├── id: Long
├── organizationId: Long
├── name: String
├── address: Address
├── managerId: Long
└── status: WarehouseStatus             ← ACTIVE, INACTIVE
```

#### `WarehouseInventory` (Aggregate Root)

```
WarehouseInventory
├── id: Long
├── warehouseId: Long
├── productId: Long
├── quantity: Integer
└── reservedQuantity: Integer
```

#### `StockTransfer` (Aggregate Root)

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
└── receivedAt: ZonedDateTime           ← nullable
```

`StockTransferItem` (Entity): `id`, `transferId`, `productId: Long`, `quantity: Integer`

---

### `returns` Submodule

#### `ReturnShipment` (Aggregate Root)

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
└── receivedAt: ZonedDateTime           ← nullable
```

---

### Domain Port

```java
// com.atlashub.logistics.shipping.domain.ports.ThirdPartyCarrierPort
public interface ThirdPartyCarrierPort {
    CarrierBookingResult bookShipment(String carrierCode, ShipmentCarrierRequest request);
    CarrierTrackingResult getTrackingStatus(String carrierCode, String carrierReference);
    void cancelShipment(String carrierCode, String carrierReference);
}
```

Implementations live in `infrastructure/services/`:
- `GigLogisticsAdapter` — integrates GIG Logistics REST API
- `DhlAdapter` — integrates DHL Ship REST API

---

### Value Objects

| Value Object | Fields |
|---|---|
| `Address` | `street`, `city`, `state`, `country: Country`, `postalCode` |
| `Dimensions` | `lengthCm: BigDecimal`, `widthCm: BigDecimal`, `heightCm: BigDecimal` |
| `Money` | `amount: BigDecimal`, `currency: CurrencyCode` (from shared) |

---

### Domain Exceptions

All exceptions extend the appropriate base class from `atlashub-shared`:

```java
public class ShipmentNotFoundException extends NotFoundException {
    public ShipmentNotFoundException(Long id) { super("Shipment not found: " + id); }
    public ShipmentNotFoundException(String trackingNumber) { super("Shipment not found: " + trackingNumber); }
}

public class InvalidShipmentStatusException extends BusinessRuleException {
    public InvalidShipmentStatusException(String message) { super(message); }
}

public class RiderNotFoundException extends NotFoundException {
    public RiderNotFoundException(Long id) { super("Rider not found: " + id); }
}

public class RiderUnavailableException extends BusinessRuleException {
    public RiderUnavailableException() { super("Rider is not available for assignment"); }
    public RiderUnavailableException(String message) { super(message); }
}

public class RiderAlreadyOnDeliveryException extends ConflictException {
    public RiderAlreadyOnDeliveryException() { super("Rider already has an active delivery"); }
}

public class VehicleNotFoundException extends NotFoundException {
    public VehicleNotFoundException(Long id) { super("Vehicle not found: " + id); }
}

public class VehicleUnavailableException extends BusinessRuleException {
    public VehicleUnavailableException() { super("Vehicle is not available for assignment"); }
    public VehicleUnavailableException(String message) { super(message); }
}

public class ProofOfDeliveryRequiredException extends BusinessRuleException {
    public ProofOfDeliveryRequiredException() {
        super("Proof of delivery URL is required to confirm delivery");
    }
}

public class TransferNotFoundException extends NotFoundException {
    public TransferNotFoundException(Long id) { super("Stock transfer not found: " + id); }
}

public class ReturnNotFoundException extends NotFoundException {
    public ReturnNotFoundException(Long id) { super("Return shipment not found: " + id); }
}

public class CarrierNotSupportedException extends BusinessRuleException {
    public CarrierNotSupportedException(String carrierCode) {
        super("Carrier not supported: " + carrierCode);
    }
}

public class CarrierApiException extends DomainException {
    public CarrierApiException(String message) { super(message); }
    public CarrierApiException(String message, Throwable cause) { super(message, cause); }
}

public class PricingRuleNotFoundException extends NotFoundException {
    public PricingRuleNotFoundException(Long id) { super("Pricing rule not found: " + id); }
}

public class ShipperClientNotFoundException extends NotFoundException {
    public ShipperClientNotFoundException(Long id) { super("Shipper client not found: " + id); }
}

public class WarehouseNotFoundException extends NotFoundException {
    public WarehouseNotFoundException(Long id) { super("Warehouse not found: " + id); }
}
```

---

### Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `ShipmentCreatedEvent` | Shipment created | `notifications` (notify recipient with tracking number) |
| `ShipmentAssignedEvent` | Rider assigned | `notifications` (WebSocket push to rider + email), `SelectiveWebSocketBroadcaster` |
| `ShipmentDispatchedEvent` | Rider picks up package | `notifications` (notify recipient: "On the way!") |
| `ShipmentLocationUpdatedEvent` | Location checkpoint | `SelectiveWebSocketBroadcaster` → `/topic/shipment/{trackingNumber}` |
| `ShipmentDeliveredEvent` | Delivery confirmed with POD | `commerce` (add stock if stock transfer), `accounting` (confirm COD entry), `notifications` |
| `ShipmentFailedEvent` | Delivery failed | `commerce` (mark order failed), `notifications` |
| `ShipmentReturnInitiatedEvent` | Failed delivery returning | `notifications` |
| `StockTransferDispatchedEvent` | Goods sent to destination | `notifications` |
| `StockTransferReceivedEvent` | Transfer confirmed at destination | `commerce` (update inventory at both locations), `accounting` |
| `ReturnShipmentReceivedEvent` | Return received at outlet | `commerce` (approve return, restore stock, initiate refund) |

---

## 3. Application Layer

### Commands

#### Shipment Commands

**`CreateShipmentCommand`**
```java
public record CreateShipmentCommand(
    Long organizationId,
    ShipmentType type,
    Address originAddress,
    Address destinationAddress,
    Long senderId,
    Long receiverId,
    BigDecimal weightKg,
    Dimensions dimensions,
    Money declaredValue,
    Long pricingRuleId,
    Long shipperClientId   // nullable
) {}
```
- **Handler**: `CreateShipmentHandler extends Command<CreateShipmentCommand, CreateShipmentResponse>`
- **Flow**: Generates unique tracking number → validates pricing rule → creates `Shipment` → `repository.save()` publishes `ShipmentCreatedEvent`
- **Response**: `CreateShipmentResponse(Long shipmentId, String trackingNumber)`
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:shipments:create')")`

---

**`AssignRiderCommand`**
```java
public record AssignRiderCommand(Long shipmentId, Long riderId, Long vehicleId) {}
```
- **Handler**: `AssignRiderHandler extends Command<AssignRiderCommand, Void>`
- **Flow**: Loads `Shipment` + `DispatchRider` (pessimistic lock) → `shipment.assignRider(riderId, vehicleId)` + `rider.assignDelivery(shipmentId)` → saves both → publishes `ShipmentAssignedEvent`
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:riders:assign')")`

---

**`DispatchShipmentCommand`**
```java
public record DispatchShipmentCommand(Long shipmentId) {}
```
- **Handler**: `DispatchShipmentHandler extends Command<DispatchShipmentCommand, Void>`
- **Flow**: Loads `Shipment` → `shipment.dispatch()` → `repository.save()` publishes `ShipmentDispatchedEvent`
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:shipments:dispatch')")`

---

**`UpdateShipmentLocationCommand`**
```java
public record UpdateShipmentLocationCommand(Long shipmentId, String location, String description) {}
```
- **Handler**: `UpdateShipmentLocationHandler extends Command<UpdateShipmentLocationCommand, Void>`
- **Flow**: Loads `Shipment` → `shipment.updateLocation(location, description)` → appends `ShipmentTrackingHistory` → `repository.save()` publishes `ShipmentLocationUpdatedEvent`
- **RBAC**: No `@PreAuthorize` — called by rider mobile app (authenticated but no special permission)

---

**`MarkDeliveredCommand`**
```java
public record MarkDeliveredCommand(Long shipmentId, String proofOfDeliveryUrl) {}
```
- **Handler**: `MarkDeliveredHandler extends Command<MarkDeliveredCommand, Void>`
- **Flow**: Loads `Shipment` → `shipment.markDelivered(podUrl)` (throws `ProofOfDeliveryRequiredException` if null) → loads `DispatchRider` → `rider.completeDelivery()` → saves both → publishes `ShipmentDeliveredEvent`
- **RBAC**: No `@PreAuthorize` — rider-initiated via authenticated session

---

**`MarkFailedCommand`**
```java
public record MarkFailedCommand(Long shipmentId, String reason) {}
```
- **Handler**: `MarkFailedHandler extends Command<MarkFailedCommand, Void>`
- **Flow**: Loads `Shipment` → `shipment.markFailed(reason)` → `repository.save()` publishes `ShipmentFailedEvent`
- **RBAC**: No `@PreAuthorize` — rider/dispatcher action

---

**`Route3PLCommand`**
```java
public record Route3PLCommand(Long shipmentId, String carrierCode) {}
```
- **Handler**: `Route3PLHandler extends Command<Route3PLCommand, Void>`
- **Flow**: Loads `Shipment` → calls `ThirdPartyCarrierPort.bookShipment()` (throws `CarrierNotSupportedException` or `CarrierApiException`) → `shipment.route3PL(carrierCode, carrierRef)` → `repository.save()`
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:shipments:dispatch')")`

---

#### Route Commands

**`CreateRouteCommand`**
```java
public record CreateRouteCommand(Long shipmentId, List<RouteStopRequest> stops) {}
```
- **Handler**: `CreateRouteHandler extends Command<CreateRouteCommand, Long>`
- **Response**: `Long routeId`

**`OptimizeRouteCommand`**
```java
public record OptimizeRouteCommand(Long routeId) {}
```
- **Handler**: `OptimizeRouteHandler extends Command<OptimizeRouteCommand, Void>`
- **Flow**: Loads `ShipmentRoute` → nearest-neighbor reordering of stops → saves updated sequence

**`MarkRouteStopReachedCommand`**
```java
public record MarkRouteStopReachedCommand(Long routeId, Long stopId) {}
```
- **Handler**: `MarkRouteStopReachedHandler extends Command<MarkRouteStopReachedCommand, Void>`

---

#### Fleet & Rider Commands

**`RegisterVehicleCommand`**
```java
public record RegisterVehicleCommand(
    Long organizationId, String licensePlate, VehicleType vehicleType, BigDecimal maxWeightKg
) {}
```
- **Handler**: `RegisterVehicleHandler extends Command<RegisterVehicleCommand, Long>`
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:fleet:manage')")`

**`UpdateVehicleStatusCommand`**
```java
public record UpdateVehicleStatusCommand(Long vehicleId, VehicleStatus status) {}
```
- **Handler**: `UpdateVehicleStatusHandler extends Command<UpdateVehicleStatusCommand, Void>`
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:fleet:manage')")`

**`RegisterRiderCommand`**
```java
public record RegisterRiderCommand(Long organizationId, Long userId, String licenseNumber) {}
```
- **Handler**: `RegisterRiderHandler extends Command<RegisterRiderCommand, Long>`
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:riders:manage')")`

**`SetRiderOnlineCommand`** / **`SetRiderOfflineCommand`**
```java
public record SetRiderOnlineCommand(Long riderId) {}
public record SetRiderOfflineCommand(Long riderId) {}
```
- **Handlers**: `SetRiderOnlineHandler`, `SetRiderOfflineHandler`
- **RBAC**: No `@PreAuthorize` — rider self-action

---

#### LaaS Commands

**`RegisterShipperClientCommand`**
```java
public record RegisterShipperClientCommand(
    Long logisticsOrgId, String clientName, EmailAddress contactEmail, PhoneNumber contactPhone
) {}
```
- **Handler**: `RegisterShipperClientHandler extends Command<RegisterShipperClientCommand, Long>`
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:shipments:create')")`

**`CreatePricingRuleCommand`**
```java
public record CreatePricingRuleCommand(
    Long organizationId, String name, PricingType type, PricingConfig config
) {}
```
- **Handler**: `CreatePricingRuleHandler extends Command<CreatePricingRuleCommand, Long>`
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:shipments:create')")`

**`SuspendShipperClientCommand`**
```java
public record SuspendShipperClientCommand(Long clientId, String reason) {}
```
- **Handler**: `SuspendShipperClientHandler extends Command<SuspendShipperClientCommand, Void>`
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:shipments:create')")`

---

#### Stock Transfer & Return Commands

**`ApproveStockTransferCommand`**
```java
public record ApproveStockTransferCommand(Long transferId) {}
```
- **Handler**: `ApproveStockTransferHandler extends Command<ApproveStockTransferCommand, Void>`
- **Flow**: Loads `StockTransfer` → transitions to APPROVED → saves → publishes `StockTransferApprovedEvent`

**`ReceiveStockTransferCommand`**
```java
public record ReceiveStockTransferCommand(Long transferId, List<ReceivedItemDto> receivedItems) {}
```
- **Handler**: `ReceiveStockTransferHandler extends Command<ReceiveStockTransferCommand, Void>`
- **Flow**: Loads `StockTransfer` (pessimistic lock) → transitions RECEIVED → saves → publishes `StockTransferReceivedEvent`

**`CreateReturnShipmentCommand`**
```java
public record CreateReturnShipmentCommand(
    Long organizationId, Long originalSalesOrderId, Long customerId,
    Address originAddress, Long destinationOutletId
) {}
```
- **Handler**: `CreateReturnShipmentHandler extends Command<CreateReturnShipmentCommand, Long>`

**`ReceiveReturnShipmentCommand`**
```java
public record ReceiveReturnShipmentCommand(Long returnShipmentId) {}
```
- **Handler**: `ReceiveReturnShipmentHandler extends Command<ReceiveReturnShipmentCommand, Void>`
- **Flow**: Loads `ReturnShipment` → transitions RECEIVED → saves → publishes `ReturnShipmentReceivedEvent`

**`RejectReturnShipmentCommand`**
```java
public record RejectReturnShipmentCommand(Long returnShipmentId, String reason) {}
```
- **Handler**: `RejectReturnShipmentHandler extends Command<RejectReturnShipmentCommand, Void>`

---

### Queries

**`TrackShipmentQuery`**
```java
public record TrackShipmentQuery(String trackingNumber) {}
```
- **Handler**: `TrackShipmentHandler extends Query<TrackShipmentQuery, ShipmentTrackingResult>`
- **Result**: `ShipmentTrackingResult(String trackingNumber, ShipmentStatus status, String currentLocation, List<TrackingHistoryEntry> history, ZonedDateTime estimatedDelivery)`
- **Auth**: None — public endpoint (`@PublicEndpoint`)
- **List vs PageResult**: Single entity — no pagination

---

**`ListShipmentsQuery`**
```java
public record ListShipmentsQuery(
    Long organizationId, ShipmentType type, ShipmentStatus status,
    Long riderId, ZonedDateTime dateFrom, ZonedDateTime dateTo, int page, int size
) {}
```
- **Handler**: `ListShipmentsHandler extends Query<ListShipmentsQuery, PageResult<ShipmentSummaryResult>>`
- **Result**: `PageResult<ShipmentSummaryResult>` — paginated; operational view can grow large
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:shipments:read')")`

---

**`GetShipmentDetailsQuery`**
```java
public record GetShipmentDetailsQuery(Long shipmentId) {}
```
- **Handler**: `GetShipmentDetailsHandler extends Query<GetShipmentDetailsQuery, ShipmentDetailsResult>`
- **Result**: Full `ShipmentDetailsResult` including tracking history, rider info, route

---

**`ListRidersQuery`**
```java
public record ListRidersQuery(Long organizationId, RiderStatus status) {}
```
- **Handler**: `ListRidersHandler extends Query<ListRidersQuery, List<RiderResult>>`
- **Result**: `List<RiderResult>` — bounded by org; fleet size is small
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:riders:manage')")`

---

**`GetRiderActiveDeliveryQuery`**
```java
public record GetRiderActiveDeliveryQuery(Long riderId) {}
```
- **Handler**: `GetRiderActiveDeliveryHandler extends Query<GetRiderActiveDeliveryQuery, ShipmentResult>`

---

**`ListVehiclesQuery`**
```java
public record ListVehiclesQuery(Long organizationId, VehicleStatus status) {}
```
- **Handler**: `ListVehiclesHandler extends Query<ListVehiclesQuery, List<VehicleResult>>`
- **Result**: `List<VehicleResult>` — bounded fleet
- **RBAC**: `@PreAuthorize("hasAuthority('logistics:fleet:manage')")`

---

**`ListStockTransfersQuery`**
```java
public record ListStockTransfersQuery(
    Long organizationId, Long locationId, TransferDirection direction, TransferStatus status
) {}
```
- **Handler**: `ListStockTransfersHandler extends Query<ListStockTransfersQuery, List<TransferResult>>`
- **Result**: `List<TransferResult>` — bounded by location/org

---

**`CalculateDeliveryFeeQuery`**
```java
public record CalculateDeliveryFeeQuery(
    Long organizationId, Long pricingRuleId, BigDecimal weightKg,
    Address origin, Address destination
) {}
```
- **Handler**: `CalculateDeliveryFeeHandler extends Query<CalculateDeliveryFeeQuery, DeliveryFeeResult>`
- **Result**: `DeliveryFeeResult(Money fee, String pricingRuleName)`

---

**`ListShipperClientsQuery`**
```java
public record ListShipperClientsQuery(Long organizationId) {}
```
- **Handler**: `ListShipperClientsHandler extends Query<ListShipperClientsQuery, List<ShipperClientResult>>`
- **Result**: `List<ShipperClientResult>` — bounded per logistics org

---

## 4. Infrastructure Layer

### Persistence

**JPA Entities** (in `infrastructure/persistence/entities/`):
- `ShipmentJpa` — maps `Shipment` domain aggregate
- `ShipmentTrackingHistoryJpa` — maps append-only tracking history
- `ShipmentRouteJpa` — maps `ShipmentRoute`
- `RouteStopJpa` — maps `RouteStop`
- `FleetVehicleJpa` — maps `FleetVehicle`
- `DispatchRiderJpa` — maps `DispatchRider`
- `ShipperClientJpa` — maps `ShipperClient`
- `PricingRuleJpa` — maps `PricingRule`
- `WarehouseJpa` — maps `Warehouse`
- `WarehouseInventoryJpa` — maps `WarehouseInventory`
- `StockTransferJpa` — maps `StockTransfer`
- `StockTransferItemJpa` — maps `StockTransferItem`
- `ReturnShipmentJpa` — maps `ReturnShipment`

**Mappers** (in `infrastructure/persistence/mappers/`):
- `ShipmentMapper`, `FleetVehicleMapper`, `DispatchRiderMapper`
- `ShipperClientMapper`, `PricingRuleMapper`
- `WarehouseMapper`, `StockTransferMapper`, `ReturnShipmentMapper`

**Spring Data Repositories** (in `infrastructure/persistence/repositories/`):
- `SpringDataShipmentRepository extends JpaRepository<ShipmentJpa, Long>`
  - `findByTrackingNumber(String trackingNumber): Optional<ShipmentJpa>`
  - `findByOrganizationIdAndStatusAndType(...)`: pageable
- `SpringDataDispatchRiderRepository` — with `@Lock(PESSIMISTIC_WRITE)` on `findByIdForUpdate()`
- `SpringDataStockTransferRepository` — with pessimistic lock support
- `SpringDataFleetVehicleRepository`, `SpringDataShipperClientRepository`, etc.

**Repository Adapters** (in `infrastructure/persistence/adapters/`):
- `ShipmentRepositoryAdapter implements ShipmentRepository`
- `DispatchRiderRepositoryAdapter implements DispatchRiderRepository`
- `FleetVehicleRepositoryAdapter implements FleetVehicleRepository`
- `StockTransferRepositoryAdapter implements StockTransferRepository`
- `ReturnShipmentRepositoryAdapter implements ReturnShipmentRepository`
- `ShipperClientRepositoryAdapter implements ShipperClientRepository`
- `PricingRuleRepositoryAdapter implements PricingRuleRepository`
- `WarehouseRepositoryAdapter implements WarehouseRepository`

---

### Listeners

#### `StockTransferApprovedListener`

```java
@Component
public class StockTransferApprovedListener extends BaseKafkaEventListener {
    private static final String GROUP_ID = "logistics-stock-transfer-approved";

    @PostConstruct
    public void init() { registerSubscription("StockTransferApprovedEvent", GROUP_ID); }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "StockTransferApprovedEvent",
            StockTransferApprovedEvent.class, log, GROUP_ID,
            e -> e instanceof TimeoutException,
            event -> createShipmentHandler.execute(new CreateShipmentCommand(
                event.payload().organizationId(),
                ShipmentType.INTER_OUTLET_TRANSFER,
                event.payload().sourceAddress(),
                event.payload().destinationAddress(),
                event.payload().requestedBy(),
                null,
                event.payload().totalWeightKg(),
                null,
                Money.ZERO,
                event.payload().defaultPricingRuleId(),
                null
            )));
    }
}
```

| Property | Value |
|---|---|
| Topic | `commerce-events` |
| Group ID | `logistics-stock-transfer-approved` |
| Event | `StockTransferApprovedEvent` |
| Command invoked | `CreateShipmentCommand` |
| Flow | Commerce approves transfer → Logistics creates Shipment for tracking |

---

#### `PurchaseOrderSentListener`

| Property | Value |
|---|---|
| Topic | `commerce-events` |
| Group ID | `logistics-purchase-order-sent` |
| Event | `PurchaseOrderSentEvent` |
| Command invoked | `CreateShipmentCommand` (type=SUPPLIER_DELIVERY) |
| Flow | Commerce raises PO → Logistics creates planned inbound shipment from supplier location |

---

#### `OnlineOrderCreatedListener`

| Property | Value |
|---|---|
| Topic | `commerce-events` |
| Group ID | `logistics-online-order-created` |
| Event | `OnlineOrderCreatedEvent` |
| Command invoked | `CreateShipmentCommand` (type=CUSTOMER_ORDER) |
| Flow | Commerce creates online order → Logistics creates outbound customer delivery shipment |

---

### Infrastructure Services (3PL Adapters)

```java
// infrastructure/services/GigLogisticsAdapter.java
@Component
public class GigLogisticsAdapter implements ThirdPartyCarrierPort {
    // integrates GIG Logistics REST API
    // endpoint: https://api.giglogistics.com/v1/shipments
    // auth: Bearer token (configured via environment variable)
}

// infrastructure/services/DhlAdapter.java
@Component
public class DhlAdapter implements ThirdPartyCarrierPort {
    // integrates DHL Ship REST API
    // endpoint: https://api.dhl.com/shipments
    // auth: DHL API key
}
```

Both adapters implement the `ThirdPartyCarrierPort` interface declared in `shipping/domain/ports/`. The `Route3PLHandler` injects a `Map<String, ThirdPartyCarrierPort>` keyed by carrier code and delegates to the matching adapter. Unknown carrier codes throw `CarrierNotSupportedException`.

---

## 5. Presentation Layer

### `ShippingController`

```java
@RestController
@RequestMapping("/api/v1/logistics")
@Tag(name = "Logistics - Shipments")
public class ShippingController {

    @PostMapping("/shipments")
    @Operation(summary = "Create a new shipment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CreateShipmentResponse>> createShipment(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody CreateShipmentRequest request) { ... }

    @GetMapping("/tracking")
    @PublicEndpoint
    @Operation(summary = "Track a shipment by tracking number — no auth required")
    public ResponseEntity<ApiResponse<ShipmentTrackingResponse>> trackShipment(
        @RequestParam String trackingNumber) { ... }

    @PatchMapping("/shipments/{shipmentId}/assign-rider")
    @Operation(summary = "Assign a rider to a shipment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> assignRider(
        @PathVariable Long shipmentId,
        @Valid @RequestBody AssignRiderRequest request) { ... }

    @PatchMapping("/shipments/{shipmentId}/dispatch")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> dispatchShipment(
        @PathVariable Long shipmentId) { ... }

    @PatchMapping("/shipments/{shipmentId}/location")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
        @PathVariable Long shipmentId,
        @Valid @RequestBody UpdateLocationRequest request) { ... }

    @PatchMapping("/shipments/{shipmentId}/deliver")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> markDelivered(
        @PathVariable Long shipmentId,
        @Valid @RequestBody MarkDeliveredRequest request) { ... }

    @GetMapping("/shipments")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<PageResult<ShipmentSummaryResponse>>> listShipments(
        @AuthenticationPrincipal Long userId,
        @RequestParam(required = false) ShipmentType type,
        @RequestParam(required = false) ShipmentStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) { ... }

    @GetMapping("/shipments/{shipmentId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ShipmentDetailsResponse>> getShipmentDetails(
        @PathVariable Long shipmentId) { ... }
}
```

### `FleetController`

```java
@RestController
@RequestMapping("/api/v1/logistics/fleet")
@Tag(name = "Logistics - Fleet & Riders")
public class FleetController {

    @PostMapping("/vehicles")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Long>> registerVehicle(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody RegisterVehicleRequest request) { ... }

    @GetMapping("/vehicles")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> listVehicles(
        @AuthenticationPrincipal Long userId,
        @RequestParam(required = false) VehicleStatus status) { ... }

    @PostMapping("/riders")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Long>> registerRider(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody RegisterRiderRequest request) { ... }

    @GetMapping("/riders")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<RiderResponse>>> listRiders(
        @AuthenticationPrincipal Long userId,
        @RequestParam(required = false) RiderStatus status) { ... }

    @PatchMapping("/riders/{riderId}/online")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> setOnline(@PathVariable Long riderId) { ... }

    @PatchMapping("/riders/{riderId}/offline")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> setOffline(@PathVariable Long riderId) { ... }
}
```

### `LaasController`

```java
@RestController
@RequestMapping("/api/v1/logistics/laas")
@Tag(name = "Logistics - LaaS")
public class LaasController {
    // RegisterShipperClient, CreatePricingRule, SuspendShipperClient
    // ListShipperClients, CalculateDeliveryFee
}
```

### `WarehouseController`

```java
@RestController
@RequestMapping("/api/v1/logistics/warehouses")
@Tag(name = "Logistics - Warehousing")
public class WarehouseController {
    // RegisterWarehouse, ListWarehouses, StockTransfer management
}
```

---

### Request DTOs

**`CreateShipmentRequest`**: `type`, `originAddress`, `destinationAddress`, `senderId`, `receiverId`, `weightKg`, `dimensions`, `declaredValue`, `pricingRuleId`, `shipperClientId` (nullable)

**`AssignRiderRequest`**: `riderId`, `vehicleId`

**`UpdateLocationRequest`**: `location`, `description`

**`MarkDeliveredRequest`**: `proofOfDeliveryUrl`

**`RegisterVehicleRequest`**: `licensePlate`, `vehicleType`, `maxWeightKg`

**`RegisterRiderRequest`**: `userId`, `licenseNumber`

**`RegisterShipperClientRequest`**: `clientName`, `contactEmail`, `contactPhone`

**`CreatePricingRuleRequest`**: `name`, `type`, `config`

---

### Response DTOs

**`CreateShipmentResponse`**: `shipmentId: Long`, `trackingNumber: String`

**`ShipmentTrackingResponse`**: `trackingNumber`, `status`, `currentLocation`, `history: List<TrackingHistoryEntry>`, `estimatedDelivery`

**`TrackingHistoryEntry`**: `location`, `status`, `description`, `recordedAt`

**`ShipmentSummaryResponse`**: `shipmentId`, `trackingNumber`, `type`, `status`, `originAddress`, `destinationAddress`, `createdAt`

**`ShipmentDetailsResponse`**: Full shipment + tracking history + rider info + route

**`RiderResponse`**: `riderId`, `userId`, `licenseNumber`, `status`, `currentShipmentId`, `vehicleId`

**`VehicleResponse`**: `vehicleId`, `licensePlate`, `vehicleType`, `maxWeightKg`, `status`

**`DeliveryFeeResult`**: `fee: Money`, `pricingRuleName: String`

**`ShipperClientResult`**: `clientId`, `clientName`, `contactEmail`, `status`, `defaultPricingRuleId`

---

## 6. RBAC

| Permission Code | Description | Applies To |
|---|---|---|
| `logistics:shipments:create` | Create and manage shipments, pricing rules, shipper clients | `CreateShipmentHandler`, `RegisterShipperClientHandler`, `CreatePricingRuleHandler` |
| `logistics:shipments:dispatch` | Dispatch shipments, route via 3PL | `DispatchShipmentHandler`, `Route3PLHandler` |
| `logistics:shipments:read` | View shipments list and details | `ListShipmentsHandler`, `GetShipmentDetailsHandler` |
| `logistics:riders:assign` | Assign riders to shipments | `AssignRiderHandler` |
| `logistics:riders:manage` | Register and manage riders | `RegisterRiderHandler`, `ListRidersHandler` |
| `logistics:fleet:manage` | Register and manage fleet vehicles | `RegisterVehicleHandler`, `UpdateVehicleStatusHandler`, `ListVehiclesHandler` |

**Public (no auth):** `TrackShipmentHandler` — annotated `@PublicEndpoint` on controller method; handler has no `@PreAuthorize`

**System (no auth):** All listener-invoked handlers (`CreateShipmentHandler` when called from `StockTransferApprovedListener`, etc.) run without a security context — no `@PreAuthorize`

---

## 7. Socket Events

Logistics modules publishes domain events to Kafka topic `logistics-events`. The `SelectiveWebSocketBroadcaster` (in `atlashub-platform:notifications`) subscribes and pushes to the appropriate channel.

| Domain Event | WebSocket Channel | Push Type | Payload |
|---|---|---|---|
| `ShipmentLocationUpdatedEvent` | `/topic/shipment/{trackingNumber}` | Public broadcast (no auth) | `PushNotification("LOCATION_UPDATE", description, {location, status})` |
| `ShipmentAssignedEvent` | `/user/{riderId}/queue/notifications` | Private (rider only) | `PushNotification("DELIVERY_ASSIGNED", "You have a new delivery", {shipmentId, trackingNumber})` |

The logistics module does **not** push to WebSocket directly. It publishes `ShipmentLocationUpdatedEvent` and `ShipmentAssignedEvent`; the `SelectiveWebSocketBroadcaster` makes the push decision.

---

## 8. Saga Participation

### Saga: Stock Transfer (Choreography)

Logistics participates as the **movement layer** in the Stock Transfer saga:

```
1. Commerce: RequestStockTransferHandler
   → StockTransferRequestedEvent published
   
2. Commerce: ApproveStockTransferHandler
   → StockTransferApprovedEvent published

3. Logistics: StockTransferApprovedListener → CreateShipmentHandler
   → Shipment created (type=INTER_OUTLET_TRANSFER)
   → ShipmentCreatedEvent published

4. Logistics: MarkDeliveredHandler (rider confirms POD)
   → Shipment → DELIVERED
   → ShipmentDeliveredEvent published

5. Commerce: ReceiveStockTransferHandler (listens on ShipmentDeliveredEvent where type=INTER_OUTLET_TRANSFER)
   → Stock added to destination outlet
   → StockTransferReceivedEvent published

6. Accounting: PostStockTransferJournalEntry (listens on StockTransferReceivedEvent)
```

**Compensation:**
```
Logistics: MarkFailedHandler → ShipmentFailedEvent
    → Commerce: ReverseStockTransferHandler → stock restored to source outlet
    → Notifications: alert operations team
```

---

## 9. Domain Events Table

| Event | Kafka Topic | Published By | Consumed By |
|---|---|---|---|
| `ShipmentCreatedEvent` | `logistics-events` | `CreateShipmentHandler` | `notifications` |
| `ShipmentAssignedEvent` | `logistics-events` | `Shipment.assignRider()` via `AssignRiderHandler` | `SelectiveWebSocketBroadcaster` → rider WS |
| `ShipmentDispatchedEvent` | `logistics-events` | `Shipment.dispatch()` via `DispatchShipmentHandler` | `notifications` |
| `ShipmentLocationUpdatedEvent` | `logistics-events` | `Shipment.updateLocation()` | `SelectiveWebSocketBroadcaster` → tracking WS |
| `ShipmentDeliveredEvent` | `logistics-events` | `Shipment.markDelivered()` | `commerce`, `accounting`, `notifications` |
| `ShipmentFailedEvent` | `logistics-events` | `Shipment.markFailed()` | `commerce`, `notifications` |
| `ShipmentReturnInitiatedEvent` | `logistics-events` | `Shipment.initiateReturn()` | `notifications` |
| `StockTransferDispatchedEvent` | `logistics-events` | `StockTransfer` state change | `notifications` |
| `StockTransferReceivedEvent` | `logistics-events` | `ReceiveStockTransferHandler` | `commerce`, `accounting` |
| `ReturnShipmentReceivedEvent` | `logistics-events` | `ReceiveReturnShipmentHandler` | `commerce` |

---

## 10. Distributed Architecture

### Locking Strategy

| Entity | Lock Type | Reason |
|---|---|---|
| `DispatchRider` | **Pessimistic** (`PESSIMISTIC_WRITE`) | Prevents assigning same rider to two concurrent shipments |
| `StockTransfer` | **Pessimistic** (`PESSIMISTIC_WRITE`) | Handles concurrent partial receipt |
| `Shipment` | **Optimistic** (`@Version`) | High read volume; concurrent edits unlikely for same shipment |
| `FleetVehicle` | **Optimistic** | Low contention |
| `ReturnShipment` | **Optimistic** | Low contention |

### Outbox & Inbox

**Outbox (critical events — must not be lost):**
- `ShipmentDeliveredEvent` — triggers financial movement in Accounting and stock update in Commerce
- `StockTransferReceivedEvent` — triggers inventory updates and inter-outlet ledger entries

**Inbox (idempotency — critical consumers):**
- `StockTransferApprovedListener` — inbox key `(eventId, logistics-stock-transfer-approved)`; replay must not create duplicate shipments
- `PurchaseOrderSentListener` — inbox key `(eventId, logistics-purchase-order-sent)`; replay must not create duplicate planned shipments
- `OnlineOrderCreatedListener` — inbox key `(eventId, logistics-online-order-created)`; replay must not create duplicate customer deliveries

---

## 11. Complete File List

```
atlashub-logistics/
└── src/main/java/com/atlashub/logistics/
    ├── shipping/
    │   ├── domain/
    │   │   ├── entities/
    │   │   │   ├── Shipment.java
    │   │   │   ├── ShipmentTrackingHistory.java
    │   │   │   ├── ShipmentRoute.java
    │   │   │   ├── RouteStop.java
    │   │   │   ├── FleetVehicle.java
    │   │   │   └── DispatchRider.java
    │   │   ├── events/
    │   │   │   ├── ShipmentCreatedEvent.java
    │   │   │   ├── ShipmentAssignedEvent.java
    │   │   │   ├── ShipmentDispatchedEvent.java
    │   │   │   ├── ShipmentLocationUpdatedEvent.java
    │   │   │   ├── ShipmentDeliveredEvent.java
    │   │   │   ├── ShipmentFailedEvent.java
    │   │   │   └── ShipmentReturnInitiatedEvent.java
    │   │   ├── exceptions/
    │   │   │   ├── ShipmentNotFoundException.java
    │   │   │   ├── InvalidShipmentStatusException.java
    │   │   │   ├── RiderNotFoundException.java
    │   │   │   ├── RiderUnavailableException.java
    │   │   │   ├── RiderAlreadyOnDeliveryException.java
    │   │   │   ├── VehicleNotFoundException.java
    │   │   │   ├── VehicleUnavailableException.java
    │   │   │   ├── ProofOfDeliveryRequiredException.java
    │   │   │   ├── CarrierNotSupportedException.java
    │   │   │   └── CarrierApiException.java
    │   │   ├── ports/
    │   │   │   └── ThirdPartyCarrierPort.java
    │   │   ├── repositories/
    │   │   │   ├── ShipmentRepository.java
    │   │   │   ├── DispatchRiderRepository.java
    │   │   │   └── FleetVehicleRepository.java
    │   │   └── valueobject/
    │   │       ├── ShipmentType.java
    │   │       ├── ShipmentStatus.java
    │   │       ├── RiderStatus.java
    │   │       ├── VehicleType.java
    │   │       ├── VehicleStatus.java
    │   │       ├── RouteStopStatus.java
    │   │       ├── Dimensions.java
    │   │       └── Address.java
    │   ├── application/
    │   │   ├── commands/
    │   │   │   ├── CreateShipment/
    │   │   │   │   ├── CreateShipmentCommand.java
    │   │   │   │   ├── CreateShipmentHandler.java
    │   │   │   │   └── CreateShipmentResponse.java
    │   │   │   ├── AssignRider/
    │   │   │   │   ├── AssignRiderCommand.java
    │   │   │   │   └── AssignRiderHandler.java
    │   │   │   ├── DispatchShipment/
    │   │   │   │   ├── DispatchShipmentCommand.java
    │   │   │   │   └── DispatchShipmentHandler.java
    │   │   │   ├── UpdateShipmentLocation/
    │   │   │   │   ├── UpdateShipmentLocationCommand.java
    │   │   │   │   └── UpdateShipmentLocationHandler.java
    │   │   │   ├── MarkDelivered/
    │   │   │   │   ├── MarkDeliveredCommand.java
    │   │   │   │   └── MarkDeliveredHandler.java
    │   │   │   ├── MarkFailed/
    │   │   │   │   ├── MarkFailedCommand.java
    │   │   │   │   └── MarkFailedHandler.java
    │   │   │   ├── Route3PL/
    │   │   │   │   ├── Route3PLCommand.java
    │   │   │   │   └── Route3PLHandler.java
    │   │   │   ├── CreateRoute/
    │   │   │   │   ├── CreateRouteCommand.java
    │   │   │   │   └── CreateRouteHandler.java
    │   │   │   ├── OptimizeRoute/
    │   │   │   │   ├── OptimizeRouteCommand.java
    │   │   │   │   └── OptimizeRouteHandler.java
    │   │   │   ├── MarkRouteStopReached/
    │   │   │   │   ├── MarkRouteStopReachedCommand.java
    │   │   │   │   └── MarkRouteStopReachedHandler.java
    │   │   │   ├── RegisterVehicle/
    │   │   │   │   ├── RegisterVehicleCommand.java
    │   │   │   │   └── RegisterVehicleHandler.java
    │   │   │   ├── UpdateVehicleStatus/
    │   │   │   │   ├── UpdateVehicleStatusCommand.java
    │   │   │   │   └── UpdateVehicleStatusHandler.java
    │   │   │   ├── RegisterRider/
    │   │   │   │   ├── RegisterRiderCommand.java
    │   │   │   │   └── RegisterRiderHandler.java
    │   │   │   ├── SetRiderOnline/
    │   │   │   │   ├── SetRiderOnlineCommand.java
    │   │   │   │   └── SetRiderOnlineHandler.java
    │   │   │   └── SetRiderOffline/
    │   │   │       ├── SetRiderOfflineCommand.java
    │   │   │       └── SetRiderOfflineHandler.java
    │   │   ├── queries/
    │   │   │   ├── TrackShipment/
    │   │   │   │   ├── TrackShipmentQuery.java
    │   │   │   │   ├── TrackShipmentHandler.java
    │   │   │   │   └── ShipmentTrackingResult.java
    │   │   │   ├── ListShipments/
    │   │   │   │   ├── ListShipmentsQuery.java
    │   │   │   │   ├── ListShipmentsHandler.java
    │   │   │   │   └── ShipmentSummaryResult.java
    │   │   │   ├── GetShipmentDetails/
    │   │   │   │   ├── GetShipmentDetailsQuery.java
    │   │   │   │   ├── GetShipmentDetailsHandler.java
    │   │   │   │   └── ShipmentDetailsResult.java
    │   │   │   ├── ListRiders/
    │   │   │   │   ├── ListRidersQuery.java
    │   │   │   │   ├── ListRidersHandler.java
    │   │   │   │   └── RiderResult.java
    │   │   │   ├── GetRiderActiveDelivery/
    │   │   │   │   ├── GetRiderActiveDeliveryQuery.java
    │   │   │   │   ├── GetRiderActiveDeliveryHandler.java
    │   │   │   │   └── ShipmentResult.java
    │   │   │   └── ListVehicles/
    │   │   │       ├── ListVehiclesQuery.java
    │   │   │       ├── ListVehiclesHandler.java
    │   │   │       └── VehicleResult.java
    │   │   └── port/
    │   │       └── CarrierBookingPort.java
    │   ├── infrastructure/
    │   │   ├── messaging/
    │   │   │   ├── events/
    │   │   │   │   └── (event payload classes)
    │   │   │   └── listeners/
    │   │   │       ├── StockTransferApprovedListener.java
    │   │   │       ├── PurchaseOrderSentListener.java
    │   │   │       └── OnlineOrderCreatedListener.java
    │   │   ├── persistence/
    │   │   │   ├── adapters/
    │   │   │   │   ├── ShipmentRepositoryAdapter.java
    │   │   │   │   ├── DispatchRiderRepositoryAdapter.java
    │   │   │   │   └── FleetVehicleRepositoryAdapter.java
    │   │   │   ├── entities/
    │   │   │   │   ├── ShipmentJpa.java
    │   │   │   │   ├── ShipmentTrackingHistoryJpa.java
    │   │   │   │   ├── ShipmentRouteJpa.java
    │   │   │   │   ├── RouteStopJpa.java
    │   │   │   │   ├── FleetVehicleJpa.java
    │   │   │   │   └── DispatchRiderJpa.java
    │   │   │   ├── mappers/
    │   │   │   │   ├── ShipmentMapper.java
    │   │   │   │   ├── FleetVehicleMapper.java
    │   │   │   │   └── DispatchRiderMapper.java
    │   │   │   └── repositories/
    │   │   │       ├── SpringDataShipmentRepository.java
    │   │   │       ├── SpringDataDispatchRiderRepository.java
    │   │   │       └── SpringDataFleetVehicleRepository.java
    │   │   └── services/
    │   │       ├── GigLogisticsAdapter.java
    │   │       └── DhlAdapter.java
    │   └── presentation/
    │       ├── dto/
    │       │   ├── CreateShipmentRequest.java
    │       │   ├── CreateShipmentResponse.java
    │       │   ├── AssignRiderRequest.java
    │       │   ├── UpdateLocationRequest.java
    │       │   ├── MarkDeliveredRequest.java
    │       │   ├── ShipmentTrackingResponse.java
    │       │   ├── ShipmentSummaryResponse.java
    │       │   ├── ShipmentDetailsResponse.java
    │       │   ├── RegisterVehicleRequest.java
    │       │   ├── VehicleResponse.java
    │       │   ├── RegisterRiderRequest.java
    │       │   └── RiderResponse.java
    │       └── rest/
    │           ├── ShippingController.java
    │           └── FleetController.java
    ├── laas/
    │   ├── domain/
    │   │   ├── entities/
    │   │   │   ├── ShipperClient.java
    │   │   │   └── PricingRule.java
    │   │   ├── exceptions/
    │   │   │   ├── ShipperClientNotFoundException.java
    │   │   │   └── PricingRuleNotFoundException.java
    │   │   ├── repositories/
    │   │   │   ├── ShipperClientRepository.java
    │   │   │   └── PricingRuleRepository.java
    │   │   └── valueobject/
    │   │       ├── ClientStatus.java
    │   │       ├── PricingType.java
    │   │       ├── PricingConfig.java
    │   │       ├── FlatPricingConfig.java
    │   │       ├── ZoneBasedPricingConfig.java
    │   │       ├── ZoneRate.java
    │   │       ├── WeightBasedPricingConfig.java
    │   │       └── WeightBand.java
    │   ├── application/
    │   │   ├── commands/
    │   │   │   ├── RegisterShipperClient/
    │   │   │   │   ├── RegisterShipperClientCommand.java
    │   │   │   │   └── RegisterShipperClientHandler.java
    │   │   │   ├── CreatePricingRule/
    │   │   │   │   ├── CreatePricingRuleCommand.java
    │   │   │   │   └── CreatePricingRuleHandler.java
    │   │   │   └── SuspendShipperClient/
    │   │   │       ├── SuspendShipperClientCommand.java
    │   │   │       └── SuspendShipperClientHandler.java
    │   │   └── queries/
    │   │       ├── ListShipperClients/
    │   │       │   ├── ListShipperClientsQuery.java
    │   │       │   ├── ListShipperClientsHandler.java
    │   │       │   └── ShipperClientResult.java
    │   │       └── CalculateDeliveryFee/
    │   │           ├── CalculateDeliveryFeeQuery.java
    │   │           ├── CalculateDeliveryFeeHandler.java
    │   │           └── DeliveryFeeResult.java
    │   ├── infrastructure/
    │   │   └── persistence/
    │   │       ├── adapters/
    │   │       │   ├── ShipperClientRepositoryAdapter.java
    │   │       │   └── PricingRuleRepositoryAdapter.java
    │   │       ├── entities/
    │   │       │   ├── ShipperClientJpa.java
    │   │       │   └── PricingRuleJpa.java
    │   │       ├── mappers/
    │   │       │   ├── ShipperClientMapper.java
    │   │       │   └── PricingRuleMapper.java
    │   │       └── repositories/
    │   │           ├── SpringDataShipperClientRepository.java
    │   │           └── SpringDataPricingRuleRepository.java
    │   └── presentation/
    │       ├── dto/
    │       │   ├── RegisterShipperClientRequest.java
    │       │   ├── CreatePricingRuleRequest.java
    │       │   ├── ShipperClientResponse.java
    │       │   └── DeliveryFeeResponse.java
    │       └── rest/
    │           └── LaasController.java
    ├── warehousing/
    │   ├── domain/
    │   │   ├── entities/
    │   │   │   ├── Warehouse.java
    │   │   │   ├── WarehouseInventory.java
    │   │   │   ├── StockTransfer.java
    │   │   │   └── StockTransferItem.java
    │   │   ├── events/
    │   │   │   ├── StockTransferDispatchedEvent.java
    │   │   │   └── StockTransferReceivedEvent.java
    │   │   ├── exceptions/
    │   │   │   ├── WarehouseNotFoundException.java
    │   │   │   └── TransferNotFoundException.java
    │   │   ├── repositories/
    │   │   │   ├── WarehouseRepository.java
    │   │   │   └── StockTransferRepository.java
    │   │   └── valueobject/
    │   │       ├── WarehouseStatus.java
    │   │       ├── TransferStatus.java
    │   │       └── LocationType.java
    │   ├── application/
    │   │   ├── commands/
    │   │   │   ├── ApproveStockTransfer/
    │   │   │   │   ├── ApproveStockTransferCommand.java
    │   │   │   │   └── ApproveStockTransferHandler.java
    │   │   │   └── ReceiveStockTransfer/
    │   │   │       ├── ReceiveStockTransferCommand.java
    │   │   │       └── ReceiveStockTransferHandler.java
    │   │   └── queries/
    │   │       └── ListStockTransfers/
    │   │           ├── ListStockTransfersQuery.java
    │   │           ├── ListStockTransfersHandler.java
    │   │           └── TransferResult.java
    │   └── infrastructure/
    │       └── persistence/
    │           ├── adapters/
    │           │   └── StockTransferRepositoryAdapter.java
    │           ├── entities/
    │           │   ├── StockTransferJpa.java
    │           │   └── StockTransferItemJpa.java
    │           ├── mappers/
    │           │   └── StockTransferMapper.java
    │           └── repositories/
    │               └── SpringDataStockTransferRepository.java
    └── returns/
        ├── domain/
        │   ├── entities/
        │   │   └── ReturnShipment.java
        │   ├── events/
        │   │   └── ReturnShipmentReceivedEvent.java
        │   ├── exceptions/
        │   │   └── ReturnNotFoundException.java
        │   ├── repositories/
        │   │   └── ReturnShipmentRepository.java
        │   └── valueobject/
        │       └── ReturnStatus.java
        ├── application/
        │   ├── commands/
        │   │   ├── CreateReturnShipment/
        │   │   │   ├── CreateReturnShipmentCommand.java
        │   │   │   └── CreateReturnShipmentHandler.java
        │   │   ├── ReceiveReturnShipment/
        │   │   │   ├── ReceiveReturnShipmentCommand.java
        │   │   │   └── ReceiveReturnShipmentHandler.java
        │   │   └── RejectReturnShipment/
        │   │       ├── RejectReturnShipmentCommand.java
        │   │       └── RejectReturnShipmentHandler.java
        │   └── queries/
        │       └── (return queries)
        └── infrastructure/
            └── persistence/
                ├── adapters/
                │   └── ReturnShipmentRepositoryAdapter.java
                ├── entities/
                │   └── ReturnShipmentJpa.java
                ├── mappers/
                │   └── ReturnShipmentMapper.java
                └── repositories/
                    └── SpringDataReturnShipmentRepository.java

---

## 12. Future Roadmap (Post-MVP)

- **Auto-Dispatch Algorithm**: Geolocation-based nearest available rider matching
- **Driver Network / Marketplace Mode**: Independent drivers register on the platform; delivery requesters post jobs; drivers accept and earn per delivery
- **Dynamic Pricing**: Surge pricing during peak hours or high demand zones
- **Earnings & Settlement for Network Drivers**: Wallet per driver, weekly disbursement
- **Driver Ratings & Reviews**: Two-sided ratings (driver ↔ recipient)
- **Advanced Route Optimization**: Integration with Google Maps/HERE Maps Distance Matrix API
- **Cash-on-Delivery (COD)**: Rider collects cash from recipient; cash reconciled against rider's COD wallet
- **Real-Time Driver Location Tracking**: WebSocket broadcast of live GPS coordinates to recipient
