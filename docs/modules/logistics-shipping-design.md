# Logistics Shipping Design (`atlashub-logistics` / `com.atlashub.logistics.shipping`)

## Role & Purpose

The `shipping` subpackage is the core of Atlas Logistics. It creates shipments, manages the dispatch lifecycle, assigns riders, tracks real-time location, handles proof-of-delivery, and integrates with third-party carriers (GIG Logistics, DHL). Every physical movement of a package in the system is represented as a `Shipment`.

Gradle module: `atlashub-logistics`  
Package: `com.atlashub.logistics.shipping`

---

## Domain Layer

### `Shipment` (Aggregate Root)

**Package:** `com.atlashub.logistics.shipping.domain.entities`

```
Shipment
├── id                   : Long
├── organizationId       : Long
├── trackingNumber       : String          ← unique, system-generated (e.g., "ATL-2026-ABC123")
├── type                 : ShipmentType    ← CUSTOMER_ORDER | INTER_OUTLET_TRANSFER | SUPPLIER_DELIVERY | RETURN
├── shipperClientId      : Long            ← nullable — set in LaaS mode
├── originAddress        : Address
├── destinationAddress   : Address
├── senderId             : Long
├── receiverId           : Long
├── weight               : BigDecimal      ← kg
├── dimensions           : Dimensions      ← value object: lengthCm, widthCm, heightCm
├── declaredValue        : Money
├── insuranceValue       : Money           ← nullable
├── riderId              : Long            ← nullable until assigned
├── vehicleId            : Long            ← nullable until assigned
├── carrier3PLCode       : String          ← nullable — set if dispatched via 3PL
├── carrier3PLReference  : String          ← nullable — 3PL tracking reference
├── proofOfDeliveryUrl   : String          ← nullable until delivered
├── status               : ShipmentStatus ← CREATED | ASSIGNED | IN_TRANSIT | DELIVERED | FAILED | RETURNING
├── currentLocation      : String          ← last known location
├── deliveryFee          : Money
├── pricingRuleId        : Long
├── notes                : String
├── dispatchedAt         : ZonedDateTime   ← nullable
├── deliveredAt          : ZonedDateTime   ← nullable
└── createdAt            : ZonedDateTime
```

**Business methods (on entity — pure state transitions with no repository access):**

| Method | Guard | Events | Exceptions |
|---|---|---|---|
| `assignRider(riderId, vehicleId)` | status == CREATED | `ShipmentAssignedEvent` | `InvalidShipmentStatusException` |
| `dispatch()` | riderId != null | `ShipmentDispatchedEvent` | `InvalidShipmentStatusException` |
| `updateLocation(location, description)` | status == IN_TRANSIT | `ShipmentLocationUpdatedEvent` | — |
| `markDelivered(podUrl)` | podUrl != null | `ShipmentDeliveredEvent` | `ProofOfDeliveryRequiredException`, `InvalidShipmentStatusException` |
| `markFailed(reason)` | status != DELIVERED | `ShipmentFailedEvent` | — |
| `initiateReturn(reason)` | status == FAILED | `ShipmentReturnInitiatedEvent` | — |
| `route3PL(carrierCode, carrierRef)` | status == CREATED | — | `CarrierNotSupportedException` |

**Domain Rules:**
- Cannot `dispatch()` before `assignRider()`
- Cannot `markDelivered()` without a non-null `proofOfDeliveryUrl`
- Cannot transition from `DELIVERED` to any other status

---

### `ShipmentTrackingHistory` (Entity — append-only)

```
ShipmentTrackingHistory
├── id          : Long
├── shipmentId  : Long
├── location    : String
├── status      : ShipmentStatus
├── description : String
└── recordedAt  : ZonedDateTime
```

---

### `ShipmentRoute` (Entity)

```
ShipmentRoute
├── id                       : Long
├── shipmentId               : Long
├── stops                    : List<RouteStop>
├── estimatedDistanceKm      : BigDecimal
└── estimatedDurationMinutes : Integer
```

### `RouteStop` (Entity)

```
RouteStop
├── id         : Long
├── routeId    : Long
├── sequence   : Integer
├── address    : Address
├── shipmentId : Long           ← for multi-drop
├── status     : RouteStopStatus ← PENDING | REACHED | SKIPPED
└── reachedAt  : ZonedDateTime   ← nullable
```

---

### `FleetVehicle` (Aggregate Root)

```
FleetVehicle
├── id             : Long
├── organizationId : Long
├── licensePlate   : String
├── vehicleType    : VehicleType    ← BIKE | CAR | VAN | TRUCK
├── maxWeightKg    : BigDecimal
├── status         : VehicleStatus  ← ACTIVE | MAINTENANCE | OUT_OF_SERVICE
├── currentRiderId : Long           ← nullable
└── registeredAt   : ZonedDateTime
```

**Business methods:** `markMaintenance()`, `returnToService()`

---

### `DispatchRider` (Aggregate Root)

```
DispatchRider
├── id               : Long
├── organizationId   : Long
├── userId           : Long
├── vehicleId        : Long        ← nullable — pre-assigned vehicle
├── licenseNumber    : String
├── status           : RiderStatus ← AVAILABLE | ON_DELIVERY | OFFLINE
├── currentShipmentId: Long        ← nullable
└── onboardedAt      : ZonedDateTime
```

**Business methods (on entity):**
- `goOnline()` → OFFLINE/any → AVAILABLE
- `goOffline()` → guard: status != ON_DELIVERY; throws `RiderAlreadyOnDeliveryException`
- `assignDelivery(shipmentId)` → guard: status == AVAILABLE; throws `RiderUnavailableException` if not; transitions ON_DELIVERY — **pessimistic lock held at DB level**
- `completeDelivery()` → ON_DELIVERY → AVAILABLE; clears `currentShipmentId`

---

### Domain Port — `com.atlashub.logistics.shipping.domain.ports`

```java
public interface ThirdPartyCarrierPort {
    CarrierBookingResult bookShipment(String carrierCode, ShipmentCarrierRequest request);
    CarrierTrackingResult getTrackingStatus(String carrierCode, String carrierReference);
    void cancelShipment(String carrierCode, String carrierReference);
}
```

Implementations in `infrastructure/services/`:
- `GigLogisticsAdapter` — GIG Logistics REST API
- `DhlAdapter` — DHL Ship REST API

The `Route3PLHandler` injects `Map<String, ThirdPartyCarrierPort>` keyed by carrier code and delegates to the matching adapter. Unknown codes throw `CarrierNotSupportedException`.

---

### Value Objects — `com.atlashub.logistics.shipping.domain.valueobject`

| Value Object | Fields |
|---|---|
| `Address` | `street`, `city`, `state`, `country`, `postalCode` |
| `Dimensions` | `lengthCm: BigDecimal`, `widthCm: BigDecimal`, `heightCm: BigDecimal` |

---

### Domain Events — `com.atlashub.logistics.shipping.domain.events`

| Event | Published When | Consumed By |
|---|---|---|
| `ShipmentCreatedEvent` | Shipment created | `notifications` (tracking number to recipient) |
| `ShipmentAssignedEvent` | Rider assigned | `SelectiveWebSocketBroadcaster` → rider WS, `notifications` |
| `ShipmentDispatchedEvent` | Rider picks up package | `notifications` (notify recipient: "On the way!") |
| `ShipmentLocationUpdatedEvent` | Location checkpoint | `SelectiveWebSocketBroadcaster` → `/topic/shipment/{trackingNumber}` |
| `ShipmentDeliveredEvent` | Delivery confirmed with POD | `commerce`, `accounting`, `notifications` |
| `ShipmentFailedEvent` | Delivery failed | `commerce`, `notifications` |
| `ShipmentReturnInitiatedEvent` | Failed delivery returning | `notifications` |

---

### Domain Exceptions — `com.atlashub.logistics.shipping.domain.exceptions`

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
}
public class RiderAlreadyOnDeliveryException extends ConflictException {
    public RiderAlreadyOnDeliveryException() { super("Rider already has an active delivery"); }
}
public class VehicleNotFoundException extends NotFoundException {
    public VehicleNotFoundException(Long id) { super("Vehicle not found: " + id); }
}
public class VehicleUnavailableException extends BusinessRuleException {
    public VehicleUnavailableException() { super("Vehicle is not available for assignment"); }
}
public class ProofOfDeliveryRequiredException extends BusinessRuleException {
    public ProofOfDeliveryRequiredException() {
        super("Proof of delivery URL is required to confirm delivery");
    }
}
public class CarrierNotSupportedException extends BusinessRuleException {
    public CarrierNotSupportedException(String carrierCode) {
        super("Carrier not supported: " + carrierCode);
    }
}
public class CarrierApiException extends ExternalServiceException {
    public CarrierApiException(String message) { super(message); }
    public CarrierApiException(String message, Throwable cause) { super(message, cause); }
}
```

---

## Application Layer

### Commands — `com.atlashub.logistics.shipping.application.commands`

#### `CreateShipmentCommand`
```java
record CreateShipmentCommand(
    Long organizationId, ShipmentType type, Address originAddress,
    Address destinationAddress, Long senderId, Long receiverId,
    BigDecimal weightKg, Dimensions dimensions, Money declaredValue,
    Long pricingRuleId, Long shipperClientId
)
```
**Handler:** `CreateShipmentHandler` | **Response:** `CreateShipmentResponse(Long shipmentId, String trackingNumber)`  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:shipments:create')")` (when called from HTTP; no annotation when called from listeners)  
**Flow:** Generate unique tracking number → validate pricing rule → create `Shipment` (status = CREATED) → `repository.save()` → `ShipmentCreatedEvent` published

---

#### `AssignRiderCommand`
```java
record AssignRiderCommand(Long shipmentId, Long riderId, Long vehicleId)
```
**Handler:** `AssignRiderHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:riders:assign')")`  
**Flow:** Load `Shipment` + `DispatchRider` (**pessimistic lock** on rider) → `shipment.assignRider(riderId, vehicleId)` + `rider.assignDelivery(shipmentId)` → save both → `ShipmentAssignedEvent` published

---

#### `DispatchShipmentCommand`
```java
record DispatchShipmentCommand(Long shipmentId)
```
**Handler:** `DispatchShipmentHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:shipments:dispatch')")`

---

#### `UpdateShipmentLocationCommand`
```java
record UpdateShipmentLocationCommand(Long shipmentId, String location, String description)
```
**Handler:** `UpdateShipmentLocationHandler` | **Response:** `void`  
**RBAC:** None — rider-initiated action (authenticated but no special permission)  
**Flow:** Load `Shipment` → `shipment.updateLocation(location, description)` → append `ShipmentTrackingHistory` → save → `ShipmentLocationUpdatedEvent` published

---

#### `MarkDeliveredCommand`
```java
record MarkDeliveredCommand(Long shipmentId, String proofOfDeliveryUrl)
```
**Handler:** `MarkDeliveredHandler` | **Response:** `void`  
**RBAC:** None — rider-initiated action  
**Flow:** Load `Shipment` → `shipment.markDelivered(podUrl)` (throws `ProofOfDeliveryRequiredException` if null) → load `DispatchRider` → `rider.completeDelivery()` → save both → `ShipmentDeliveredEvent` published

---

#### `MarkFailedCommand`
```java
record MarkFailedCommand(Long shipmentId, String reason)
```
**Handler:** `MarkFailedHandler` | **Response:** `void`  
**RBAC:** None — rider/dispatcher action

---

#### `Route3PLCommand`
```java
record Route3PLCommand(Long shipmentId, String carrierCode)
```
**Handler:** `Route3PLHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:shipments:dispatch')")`  
**Flow:** Load `Shipment` → `ThirdPartyCarrierPort.bookShipment(carrierCode, ...)` → `shipment.route3PL(carrierCode, carrierRef)` → save

---

#### `CreateRouteCommand`
```java
record CreateRouteCommand(Long shipmentId, List<RouteStopRequest> stops)
```
**Handler:** `CreateRouteHandler` | **Response:** `Long routeId`

---

#### `OptimizeRouteCommand`
```java
record OptimizeRouteCommand(Long routeId)
```
**Handler:** `OptimizeRouteHandler` | **Response:** `void`  
**Flow:** Load `ShipmentRoute` → nearest-neighbor reordering of stops → save updated sequence

---

#### `MarkRouteStopReachedCommand`
```java
record MarkRouteStopReachedCommand(Long routeId, Long stopId)
```
**Handler:** `MarkRouteStopReachedHandler` | **Response:** `void`

---

#### `RegisterVehicleCommand`
```java
record RegisterVehicleCommand(Long organizationId, String licensePlate, VehicleType vehicleType, BigDecimal maxWeightKg)
```
**Handler:** `RegisterVehicleHandler` | **Response:** `Long vehicleId`  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:fleet:manage')")`

---

#### `RegisterRiderCommand`
```java
record RegisterRiderCommand(Long organizationId, Long userId, String licenseNumber)
```
**Handler:** `RegisterRiderHandler` | **Response:** `Long riderId`  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:riders:manage')")`

---

#### `SetRiderOnlineCommand` / `SetRiderOfflineCommand`
```java
record SetRiderOnlineCommand(Long riderId) {}
record SetRiderOfflineCommand(Long riderId) {}
```
**Handlers:** `SetRiderOnlineHandler`, `SetRiderOfflineHandler` | **Response:** `void`  
**RBAC:** None — rider self-action

---

### Queries — `com.atlashub.logistics.shipping.application.queries`

#### `TrackShipmentQuery`
```java
record TrackShipmentQuery(String trackingNumber)
```
**Handler:** `TrackShipmentHandler` | **Result:** `ShipmentTrackingResult`  
**Auth:** None — public endpoint (`@PublicEndpoint`)

`ShipmentTrackingResult`: `trackingNumber`, `status`, `currentLocation`, `history: List<TrackingHistoryEntry>`, `estimatedDelivery`

---

#### `ListShipmentsQuery`
```java
record ListShipmentsQuery(Long organizationId, ShipmentType type, ShipmentStatus status,
                          Long riderId, ZonedDateTime dateFrom, ZonedDateTime dateTo, int page, int size)
```
**Handler:** `ListShipmentsHandler` | **Result:** `PageResult<ShipmentSummaryResult>`  
**Justification:** Large, time-bounded — paginated view required.  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:shipments:read')")`

---

#### `GetShipmentDetailsQuery`
```java
record GetShipmentDetailsQuery(Long shipmentId)
```
**Handler:** `GetShipmentDetailsHandler` | **Result:** `ShipmentDetailsResult` (includes tracking history + route + rider)

---

#### `ListRidersQuery`
```java
record ListRidersQuery(Long organizationId, RiderStatus status)
```
**Handler:** `ListRidersHandler` | **Result:** `List<RiderResult>` (bounded fleet)  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:riders:manage')")`

---

#### `ListVehiclesQuery`
```java
record ListVehiclesQuery(Long organizationId, VehicleStatus status)
```
**Handler:** `ListVehiclesHandler` | **Result:** `List<VehicleResult>` (bounded fleet)  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:fleet:manage')")`

---

## Infrastructure Layer

### Persistence

| JPA Entity | Table | Locking |
|---|---|---|
| `ShipmentJpaEntity` | `logistics_shipments` | `@Version` optimistic |
| `ShipmentTrackingHistoryJpaEntity` | `logistics_tracking_history` | — (append-only) |
| `ShipmentRouteJpaEntity` | `logistics_shipment_routes` | — |
| `RouteStopJpaEntity` | `logistics_route_stops` | — |
| `FleetVehicleJpaEntity` | `logistics_fleet_vehicles` | `@Version` optimistic |
| `DispatchRiderJpaEntity` | `logistics_dispatch_riders` | `@Lock(PESSIMISTIC_WRITE)` during assignment |

**Spring Data key methods:**
```
SpringDataShipmentRepository
  + findByTrackingNumber(String trackingNumber): Optional<ShipmentJpaEntity>
  + findByOrganizationIdAndStatusAndType(..., Pageable): Page<ShipmentJpaEntity>

SpringDataDispatchRiderRepository
  + findByIdForUpdate(Long id): Optional<DispatchRiderJpaEntity>  // @Lock PESSIMISTIC_WRITE
```

**Repository Adapters + Sequences:**
- `ShipmentRepositoryAdapter` → `logistics_shipment_seq`
- `DispatchRiderRepositoryAdapter` → `logistics_rider_seq`
- `FleetVehicleRepositoryAdapter` → `logistics_vehicle_seq`

### Listeners — `infrastructure/messaging/listeners/`

#### `StockTransferApprovedListener`
| Attribute | Value |
|---|---|
| **Topic** | `commerce-events` |
| **Group ID** | `logistics-stock-transfer-approved` |
| **Event consumed** | `StockTransferApprovedEvent` |
| **Payload** | `organizationId`, `sourceAddress`, `destinationAddress`, `requestedBy`, `totalWeightKg`, `defaultPricingRuleId` |
| **Command called** | `CreateShipmentCommand` (type=INTER_OUTLET_TRANSFER) |
| **Idempotency** | `(eventId, "logistics-stock-transfer-approved")` — must not create duplicate shipments |

#### `PurchaseOrderSentListener`
| Attribute | Value |
|---|---|
| **Topic** | `commerce-events` |
| **Group ID** | `logistics-purchase-order-sent` |
| **Event consumed** | `PurchaseOrderSentEvent` |
| **Command called** | `CreateShipmentCommand` (type=SUPPLIER_DELIVERY) |

#### `OnlineOrderCreatedListener`
| Attribute | Value |
|---|---|
| **Topic** | `commerce-events` |
| **Group ID** | `logistics-online-order-created` |
| **Event consumed** | `OnlineOrderCreatedEvent` |
| **Command called** | `CreateShipmentCommand` (type=CUSTOMER_ORDER) |

### Infrastructure Services — `infrastructure/services/`

| Adapter | Implements | External |
|---|---|---|
| `GigLogisticsAdapter` | `ThirdPartyCarrierPort` | GIG Logistics REST API |
| `DhlAdapter` | `ThirdPartyCarrierPort` | DHL Ship REST API |

---

## Presentation Layer

### Controllers

- `ShippingController` — `/api/v1/logistics`
- `FleetController` — `/api/v1/logistics/fleet`

| Method | Path | Auth | RBAC | Request | Response |
|---|---|---|---|---|---|
| `POST` | `/logistics/shipments` | Bearer | `logistics:shipments:create` | `CreateShipmentRequest` | `CreateShipmentResponse` |
| `GET` | `/logistics/tracking` | `@PublicEndpoint` | — | `?trackingNumber` | `ShipmentTrackingResult` |
| `PATCH` | `/logistics/shipments/{id}/assign-rider` | Bearer | `logistics:riders:assign` | `AssignRiderRequest` | `void` |
| `PATCH` | `/logistics/shipments/{id}/dispatch` | Bearer | `logistics:shipments:dispatch` | — | `void` |
| `PATCH` | `/logistics/shipments/{id}/location` | Bearer | — | `UpdateLocationRequest` | `void` |
| `PATCH` | `/logistics/shipments/{id}/deliver` | Bearer | — | `MarkDeliveredRequest` | `void` |
| `PATCH` | `/logistics/shipments/{id}/fail` | Bearer | — | `MarkFailedRequest` | `void` |
| `GET` | `/logistics/shipments` | Bearer | `logistics:shipments:read` | `?orgId&type&status&page&size` | `PageResult<ShipmentSummaryResult>` |
| `GET` | `/logistics/shipments/{id}` | Bearer | `logistics:shipments:read` | — | `ShipmentDetailsResult` |
| `POST` | `/logistics/fleet/vehicles` | Bearer | `logistics:fleet:manage` | `RegisterVehicleRequest` | `Long` |
| `GET` | `/logistics/fleet/vehicles` | Bearer | `logistics:fleet:manage` | `?orgId&status` | `List<VehicleResult>` |
| `POST` | `/logistics/fleet/riders` | Bearer | `logistics:riders:manage` | `RegisterRiderRequest` | `Long` |
| `GET` | `/logistics/fleet/riders` | Bearer | `logistics:riders:manage` | `?orgId&status` | `List<RiderResult>` |
| `PATCH` | `/logistics/fleet/riders/{id}/online` | Bearer | — | — | `void` |
| `PATCH` | `/logistics/fleet/riders/{id}/offline` | Bearer | — | — | `void` |

---

## RBAC Table

| Permission | Commands |
|---|---|
| `logistics:shipments:create` | `CreateShipmentHandler` |
| `logistics:shipments:dispatch` | `DispatchShipmentHandler`, `Route3PLHandler` |
| `logistics:shipments:read` | `ListShipmentsHandler`, `GetShipmentDetailsHandler` |
| `logistics:riders:assign` | `AssignRiderHandler` |
| `logistics:riders:manage` | `RegisterRiderHandler`, `ListRidersHandler` |
| `logistics:fleet:manage` | `RegisterVehicleHandler`, `UpdateVehicleStatusHandler`, `ListVehiclesHandler` |

**Public (no auth):** `TrackShipmentHandler` — `@PublicEndpoint`  
**System (no auth):** Listener-invoked `CreateShipmentHandler` calls — no security context

---

## Socket Events

| Event | WS Channel | Payload |
|---|---|---|
| `ShipmentLocationUpdatedEvent` | `/topic/shipment/{trackingNumber}` | `type: "LOCATION_UPDATE"`, `data: { location, status }` |
| `ShipmentAssignedEvent` | `/user/{riderId}/queue/notifications` | `type: "DELIVERY_ASSIGNED"`, `data: { shipmentId, trackingNumber }` |

Logistics does **not** push to WebSocket directly — it publishes events; `SelectiveWebSocketBroadcaster` pushes.

---

## Distributed Architecture

### Locking
- `DispatchRider` — **Pessimistic Write** during `assignDelivery()` — prevents concurrent double-assignment
- `Shipment`, `FleetVehicle` — Optimistic (`@Version`)

### Outbox
- `ShipmentDeliveredEvent` — triggers stock update in Commerce, journal entry in Accounting
- `StockTransferReceivedEvent` — triggers inventory updates in Commerce

### Inbox
- All three listeners keyed on `(eventId, groupId)` — duplicate events must not create duplicate shipments

---

## Complete File List

```
atlashub-logistics/src/main/java/com/atlashub/logistics/shipping/
├── domain/
│   ├── entities/ [Shipment, ShipmentTrackingHistory, ShipmentRoute, RouteStop, FleetVehicle, DispatchRider]
│   ├── events/ [ShipmentCreatedEvent, ShipmentAssignedEvent, ShipmentDispatchedEvent, ShipmentLocationUpdatedEvent, ShipmentDeliveredEvent, ShipmentFailedEvent, ShipmentReturnInitiatedEvent]
│   ├── exceptions/ [ShipmentNotFoundException, InvalidShipmentStatusException, RiderNotFoundException, RiderUnavailableException, RiderAlreadyOnDeliveryException, VehicleNotFoundException, VehicleUnavailableException, ProofOfDeliveryRequiredException, CarrierNotSupportedException, CarrierApiException]
│   ├── ports/
│   │   └── ThirdPartyCarrierPort.java
│   ├── repositories/ [ShipmentRepository, FleetVehicleRepository, DispatchRiderRepository]
│   └── valueobject/ [ShipmentType, ShipmentStatus, VehicleType, VehicleStatus, RiderStatus, RouteStopStatus, Address, Dimensions]
├── application/
│   ├── commands/ [CreateShipment, AssignRider, DispatchShipment, UpdateShipmentLocation, MarkDelivered, MarkFailed, Route3PL, CreateRoute, OptimizeRoute, MarkRouteStopReached, RegisterVehicle, UpdateVehicleStatus, RegisterRider, SetRiderOnline, SetRiderOffline]
│   └── queries/ [TrackShipment, ListShipments, GetShipmentDetails, ListRiders, GetRiderActiveDelivery, ListVehicles]
├── infrastructure/
│   ├── messaging/
│   │   ├── events/ [StockTransferApprovedPayload, PurchaseOrderSentPayload, OnlineOrderCreatedPayload]
│   │   └── listeners/ [StockTransferApprovedListener, PurchaseOrderSentListener, OnlineOrderCreatedListener]
│   ├── persistence/ [adapters, entities, mappers, repositories]
│   └── services/
│       ├── GigLogisticsAdapter.java
│       └── DhlAdapter.java
└── presentation/
    ├── dto/ [...]
    └── rest/ [ShippingController, FleetController]
```
