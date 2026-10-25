> **V2 MODERNIZATION NOTE (2026-10-25):** The target has independent global User registration and Organization creation, and optional merchant-connected providers instead of Atlas mandatory Anchor banking/KYB. The detailed module below may reflect existing legacy software. See [v2 PRD](../atlashub-prd-v2.md) and [banking retirement](../architecture/provider-banking-decommission.md). No implemented code is changed by this note.

# Logistics Shipping Design (`:atlashub-logistics:shipping` / `com.atlashub.logistics.shipping`)

## Role & Purpose

The `:atlashub-logistics:shipping` submodule is the core movement, carrier orchestration, tracking, and shipping incentive engine of AtlasHub. It handles:
1. **Shipping Rate & Window Calculation** (`STANDARD` vs `URGENT` tiers, coverage zone verification, range-based delivery windows).
2. **Shipment Lifecycle & API-Exposed Delivery Code** (`CUSTOMER_ORDER`, `INTER_OUTLET_TRANSFER`, `SUPPLIER_DELIVERY`, `RETURN`, plus storefront-retrievable 6-digit `deliveryCode` verified at handover).
3. **Route-Aware Milestone & Live Tracking** (`INTRA_STATE` direct dispatch vs `INTER_STATE` distribution-center hub-and-spoke milestones + live GPS updates).
4. **Cross-Merchant Consolidation Batching** (`ConsolidationBatch` with `latestDispatchAt` SLA guard).
5. **Monthly 10% Merchant Shipping Incentive** (`MerchantShippingIncentive` calculated at month-end from net consolidation savings on `CUSTOMER_ORDER` shipments).
6. **Hybrid Carrier Execution** (**AtlasHub Internal Fleet** via `CoverageZone`, `DispatchRider`, and `FleetVehicle` alongside **3PL Partner Carriers** via shared `ThirdPartyCarrierPort`).

Gradle module: `:atlashub-logistics:shipping`  
Package: `com.atlashub.logistics.shipping`

---

## Domain Layer

### 1. `Shipment` (Aggregate Root)

**Package:** `com.atlashub.logistics.shipping.domain.entities`

```
Shipment
├── id                       : Long
├── organizationId           : Long                    ← merchant/business organization ID
├── orderId                  : Long                    ← nullable (set for CUSTOMER_ORDER)
├── trackingNumber           : String                  ← unique (e.g., "ATL-2026-ABC123")
├── type                     : ShipmentType            ← CUSTOMER_ORDER | INTER_OUTLET_TRANSFER | SUPPLIER_DELIVERY | RETURN
├── serviceTier              : DeliveryTier            ← STANDARD | URGENT
├── routeType                : RouteType               ← INTRA_STATE | INTER_STATE
├── fulfillmentChannel       : CarrierChannel          ← INTERNAL_FLEET | PARTNER_3PL
├── originAddress            : Address
├── destinationAddress       : Address
├── senderId                 : Long
├── receiverId               : Long
├── weightKg                 : BigDecimal
├── dimensions               : Dimensions
├── declaredValue            : Money
├── customerShippingFee      : Money                   ← paid to AtlasHub (customer checkout or merchant transfer fee)
├── baselineSingleCost       : Money                   ← unbatched single-shipment carrier cost baseline
├── allocatedActualCost      : Money                   ← nullable until dispatched/settled in batch or single run
├── consolidationBatchId     : Long                    ← nullable — set when grouped into a ConsolidationBatch
├── deliveryCode             : String                  ← 6-digit secret code exposed via API to merchant/storefront
├── deliveryWindowStart      : ZonedDateTime           ← start of promised delivery range
├── deliveryWindowEnd        : ZonedDateTime           ← end of promised delivery range
├── latestDispatchAt         : ZonedDateTime           ← max hold timestamp before batch must dispatch
├── currentMilestone         : TrackingMilestone       ← route-aware milestone
├── status                   : ShipmentStatus          ← CREATED | BATCHED | ASSIGNED | IN_TRANSIT | DELIVERED | FAILED | RETURNING
├── deliveryAttempts         : int                     ← count of failed handover attempts (must reach 2 before FAILED)
├── failureFaultType         : DeliveryFaultType       ← nullable: CUSTOMER_FAULT | MERCHANT_FAULT | CARRIER_FAULT
├── failureReason            : String                  ← nullable
├── riderId                  : Long                    ← nullable (INTERNAL_FLEET)
├── vehicleId                : Long                    ← nullable (INTERNAL_FLEET)
├── carrier3PLCode           : String                  ← nullable (PARTNER_3PL)
├── carrier3PLReference      : String                  ← nullable (PARTNER_3PL)
├── proofOfDeliveryUrl       : String                  ← nullable until delivered
├── currentLocation          : String                  ← last known facility or GPS label
├── currentLatitude          : BigDecimal              ← nullable
├── currentLongitude         : BigDecimal              ← nullable
├── incentiveEligible        : boolean                 ← true only for CUSTOMER_ORDER (false for INTER_OUTLET_TRANSFER or MERCHANT_FAULT)
├── dispatchedAt             : ZonedDateTime           ← nullable
├── deliveredAt              : ZonedDateTime           ← nullable
└── createdAt                : ZonedDateTime
```

**Key Domain Invariants & Behavior Methods:**
- `Shipment.create(...)`:
  - Automatically derives `routeType = originAddress.state().equalsIgnoreCase(destinationAddress.state()) ? RouteType.INTRA_STATE : RouteType.INTER_STATE`.
  - Enforces `deliveryWindowEnd.isAfter(deliveryWindowStart)`.
  - Sets `incentiveEligible = (type == ShipmentType.CUSTOMER_ORDER)`.
  - Generates a 6-digit `deliveryCode` and sets `currentMilestone = TrackingMilestone.CREATED`, `status = ShipmentStatus.CREATED`.
  - Registers `ShipmentCreatedEvent`.
- `assignToBatch(Long batchId)`:
  - Guard: `serviceTier == DeliveryTier.STANDARD` (throws `UrgentShipmentBatchingForbiddenException` if `URGENT`) and `status == ShipmentStatus.CREATED`.
  - Transitions `status` to `BATCHED`.
- `assignInternalRider(Long riderId, Long vehicleId)`:
  - Guard: `status == CREATED || status == BATCHED`.
  - Sets `fulfillmentChannel = CarrierChannel.INTERNAL_FLEET`, `riderId`, `vehicleId`, `status = ASSIGNED`.
  - For `INTRA_STATE`, sets `currentMilestone = TrackingMilestone.ASSIGNED_TO_RIDER`.
  - Registers `ShipmentAssignedEvent`.
- `route3PL(String carrierCode, String carrierReference, Money actualCost)`:
  - Guard: `status == CREATED || status == BATCHED`.
  - Sets `fulfillmentChannel = CarrierChannel.PARTNER_3PL`, `carrier3PLCode`, `carrier3PLReference`, `allocatedActualCost = actualCost`, `status = ASSIGNED`.
- `advanceMilestone(TrackingMilestone nextMilestone, String locationName, BigDecimal latitude, BigDecimal longitude, ZonedDateTime now)`:
  - Enforces valid `routeType` progression (`INTRA_STATE` cannot use distribution center linehaul milestones if not routed via hub; `INTER_STATE` progresses through origin/destination distribution center scans).
  - Transitions `status` to `IN_TRANSIT` when `nextMilestone == TrackingMilestone.PICKED_UP_FROM_STORE` and sets `dispatchedAt`.
  - Registers `ShipmentMilestoneUpdatedEvent` (and `ShipmentDispatchedEvent` on first pickup).
- `updateLiveLocation(String locationName, BigDecimal latitude, BigDecimal longitude)`:
  - Guard: `status == ShipmentStatus.IN_TRANSIT`.
  - Updates `currentLocation`, `currentLatitude`, `currentLongitude`.
  - Registers `ShipmentLocationUpdatedEvent`.
- `completeDelivery(String providedDeliveryCode, String podUrl, ZonedDateTime now)`:
  - Guard: `status == ShipmentStatus.IN_TRANSIT`.
  - Guard: `podUrl != null && !podUrl.isBlank()` (throws `ProofOfDeliveryRequiredException`).
  - Guard: `this.deliveryCode.equals(providedDeliveryCode)` (throws `InvalidDeliveryCodeException`).
  - Sets `proofOfDeliveryUrl = podUrl`, `status = ShipmentStatus.DELIVERED`, `currentMilestone = TrackingMilestone.DELIVERED`, `deliveredAt = now`.
  - Registers `ShipmentDeliveredEvent`.
- `recordFailedAttempt(String reason)`:
  - Guard: `status == ShipmentStatus.IN_TRANSIT`.
  - Increments `deliveryAttempts`.
  - Registers `ShipmentDeliveryAttemptFailedEvent`.
- `markFailed(DeliveryFaultType faultType, String reason)`:
  - Guard: `status != ShipmentStatus.DELIVERED`.
  - Guard: if `faultType == DeliveryFaultType.CUSTOMER_FAULT`, requires `deliveryAttempts >= 2` (throws `InsufficientDeliveryAttemptsException`).
  - If `faultType == DeliveryFaultType.MERCHANT_FAULT`, sets `incentiveEligible = false`.
  - Sets `status = ShipmentStatus.FAILED`, `failureFaultType = faultType`, `failureReason = reason`.
  - Registers `ShipmentFailedEvent`.
- `initiateReturn(String reason)`:
  - Guard: `status == ShipmentStatus.FAILED`.
  - Sets `status = ShipmentStatus.RETURNING`.
  - Registers `ShipmentReturnInitiatedEvent`.
- `netCostSavings()`:
  - If `!incentiveEligible` or `allocatedActualCost == null` or `status != ShipmentStatus.DELIVERED`, returns `Money.zero(customerShippingFee.currency())`.
  - If `baselineSingleCost` > `allocatedActualCost`, returns `baselineSingleCost.subtract(allocatedActualCost)`, else `Money.zero(...)`.

---

### 2. `ConsolidationBatch` (Aggregate Root)

Groups multiple `STANDARD` shipments moving along the same corridor/zone so AtlasHub can reduce per-shipment carrier cost.

```
ConsolidationBatch
├── id                   : Long
├── batchReference       : String                  ← e.g., "BATCH-202610-001"
├── originState          : String
├── originCity           : String
├── destinationState     : String
├── destinationCity      : String
├── routeType            : RouteType               ← INTRA_STATE | INTER_STATE
├── shipmentIds          : List<Long>
├── totalWeightKg        : BigDecimal
├── latestDispatchAt     : ZonedDateTime           ← earliest `latestDispatchAt` across all member shipments
├── totalBaselineCost    : Money
├── actualBatchCost      : Money                   ← nullable until dispatched
├── status               : BatchStatus             ← OPEN | DISPATCHED | COMPLETED | CANCELLED
├── carrierChannel       : CarrierChannel          ← nullable until dispatched
├── carrierCode          : String                  ← nullable
├── dispatchedAt         : ZonedDateTime           ← nullable
└── createdAt            : ZonedDateTime
```

**Behavior Methods:**
- `addShipment(Long shipmentId, BigDecimal weightKg, Money baselineCost, ZonedDateTime shipmentLatestDispatchAt)`
- `dispatchBatch(CarrierChannel channel, String carrierCode, Money actualBatchCost, ZonedDateTime now)`
- `allocateCostForWeight(BigDecimal shipmentWeightKg)`: proportional allocation of `actualBatchCost` by weight (or equal split when weights are zero/equal).

---

### 3. `MerchantShippingIncentive` (Aggregate Root)

Monthly shipping incentive rebate record for a business (10% of net consolidation savings on delivered `CUSTOMER_ORDER` shipments).

```
MerchantShippingIncentive
├── id                         : Long
├── organizationId             : Long
├── billingYearMonth           : String            ← e.g., "2026-10"
├── eligibleShipmentCount      : int
├── totalBaselineCarrierCost   : Money             ← internal audit only (never exposed to merchant)
├── totalActualCarrierCost     : Money             ← internal audit only (never exposed to merchant)
├── totalNetSavings            : Money             ← internal audit only (never exposed to merchant)
├── incentivePercentage        : BigDecimal        ← system constant 0.10 (10%, never exposed to merchant)
├── rebateAmount               : Money             ← 10% of totalNetSavings — credited to merchant
├── status                     : IncentiveStatus   ← CALCULATED | CREDITED
├── creditedAt                 : ZonedDateTime     ← nullable
└── createdAt                  : ZonedDateTime
```

**Behavior Methods:**
- `MerchantShippingIncentive.calculate(Long id, Long organizationId, String billingYearMonth, int eligibleShipmentCount, Money totalBaselineCost, Money totalActualCost, ZonedDateTime now)`:
  - Computes `totalNetSavings = max(0, totalBaselineCost - totalActualCost)`.
  - Computes `rebateAmount = totalNetSavings * 0.10`.
- `markCredited(ZonedDateTime now)` → transitions `CALCULATED` → `CREDITED` and registers `MerchantShippingIncentiveCreditedEvent`.

---

### 4. `CoverageZone` (Aggregate Root)

Defines areas where AtlasHub operates its **Internal Fleet** and/or **3PL Partners**, base rate cards, Urgent Delivery availability, and standard/urgent delivery window hours.

```
CoverageZone
├── id                         : Long
├── state                      : String
├── city                       : String
├── zoneCode                   : String            ← e.g., "LOS-IKEJA", "ABJ-CENTRAL"
├── internalFleetCovered       : boolean           ← true if AtlasHub own bikes/vehicles cover this zone
├── partner3PLCovered          : boolean           ← true if 3PL partners cover this zone
├── preferred3PLCarrierCode    : String            ← nullable (e.g., "GIG", "DHL")
├── urgentDeliverySupported    : boolean
├── baseStandardFee            : Money
├── perKgStandardFee           : Money
├── urgentMultiplier           : BigDecimal        ← e.g., 2.50 (2.5x standard fee)
├── intraStateStandardMinHours : int               ← e.g., 12
├── intraStateStandardMaxHours : int               ← e.g., 36
├── interStateStandardMinHours : int               ← e.g., 48
├── interStateStandardMaxHours : int               ← e.g., 96
├── urgentMinHours             : int               ← e.g., 2
├── urgentMaxHours             : int               ← e.g., 6
├── active                     : boolean
└── createdAt                  : ZonedDateTime
```

---

### 5. `MerchantShippingConfig` (Aggregate Root)

Stores per-business storefront logistics preferences (such as whether the merchant enables `URGENT` delivery on their storefront).

```
MerchantShippingConfig
├── id                         : Long
├── organizationId             : Long
├── urgentDeliveryEnabled      : boolean
├── defaultPickupAddress       : Address
├── updatedAt                  : ZonedDateTime
```

---

### 6. `FleetVehicle` & `DispatchRider` (Aggregate Roots)

Represent AtlasHub's expanding internal fleet of bikes, vans, trucks, and riders.
- `FleetVehicle`: `id`, `organizationId`, `zoneCode`, `licensePlate`, `vehicleType` (`BIKE`, `CAR`, `VAN`, `TRUCK`), `maxWeightKg`, `status` (`ACTIVE`, `MAINTENANCE`, `OUT_OF_SERVICE`), `currentRiderId`, `registeredAt`.
- `DispatchRider`: `id`, `organizationId`, `userId`, `zoneCode`, `vehicleId`, `licenseNumber`, `status` (`AVAILABLE`, `ON_DELIVERY`, `OFFLINE`), `currentShipmentId`, `onboardedAt`.

---

### 7. `ShipmentMilestoneLog` (Aggregate Root / Append-Only Entry)

```
ShipmentMilestoneLog
├── id             : Long
├── shipmentId     : Long
├── trackingNumber : String
├── routeType      : RouteType
├── milestone      : TrackingMilestone
├── status         : ShipmentStatus
├── locationName   : String
├── state          : String
├── description    : String
├── latitude       : BigDecimal       ← nullable
├── longitude      : BigDecimal       ← nullable
└── occurredAt     : ZonedDateTime
```

---

### Value Objects & Enums (`com.atlashub.logistics.shipping.domain.valueobject`)

- `ShipmentType`: `CUSTOMER_ORDER`, `INTER_OUTLET_TRANSFER`, `SUPPLIER_DELIVERY`, `RETURN`
- `DeliveryTier`: `STANDARD`, `URGENT`
- `RouteType`: `INTRA_STATE`, `INTER_STATE`
- `CarrierChannel`: `INTERNAL_FLEET`, `PARTNER_3PL`
- `ShipmentStatus`: `CREATED`, `BATCHED`, `ASSIGNED`, `IN_TRANSIT`, `DELIVERED`, `FAILED`, `RETURNING`
- `TrackingMilestone`:
  - Shared: `CREATED`, `PICKED_UP_FROM_STORE`, `DELIVERED`
  - `INTRA_STATE` specific: `ASSIGNED_TO_RIDER`, `IN_TRANSIT`, `ARRIVING_SOON`
  - `INTER_STATE` specific: `ARRIVED_ORIGIN_DISTRIBUTION_CENTER`, `CONSOLIDATED_FOR_LINEHAUL`, `LEFT_ORIGIN_DISTRIBUTION_CENTER`, `IN_TRANSIT_TO_DESTINATION_STATE`, `ARRIVED_DESTINATION_DISTRIBUTION_CENTER`, `OUT_FOR_DELIVERY`
- `DeliveryFaultType`: `CUSTOMER_FAULT`, `MERCHANT_FAULT`, `CARRIER_FAULT`
- `BatchStatus`: `OPEN`, `DISPATCHED`, `COMPLETED`, `CANCELLED`
- `IncentiveStatus`: `CALCULATED`, `CREDITED`
- `VehicleType`: `BIKE`, `CAR`, `VAN`, `TRUCK`
- `VehicleStatus`: `ACTIVE`, `MAINTENANCE`, `OUT_OF_SERVICE`
- `RiderStatus`: `AVAILABLE`, `ON_DELIVERY`, `OFFLINE`
- `Address`: `street`, `city`, `state`, `country`, `postalCode`
- `Dimensions`: `lengthCm`, `widthCm`, `heightCm`

---

### Outbound & Cross-Module Shared Ports (`atlashub-shared` / `com.atlashub.shared.application.port`)

Per AtlasHub architecture rules, all outbound provider ports and cross-module query ports live in `atlashub-shared`:
1. **`ThirdPartyCarrierPort`** (`com.atlashub.shared.application.port.ThirdPartyCarrierPort`):
   - `CarrierBookingResult bookShipment(String carrierCode, CarrierBookingRequest request)`
   - `CarrierQuoteResult quoteShipment(String carrierCode, CarrierQuoteRequest request)`
   - `CarrierTrackingResult getTrackingStatus(String carrierCode, String carrierReference)`
   - `void cancelShipment(String carrierCode, String carrierReference)`
2. **`LogisticsShippingQueryPort`** (`com.atlashub.shared.application.port.LogisticsShippingQueryPort`):
   - Allows `commerce`, `returns`, and `warehousing` to query shipping quotes, coverage availability, and shipment snapshots synchronously without cross-module imports.

---

## Application Layer

### Commands (`com.atlashub.logistics.shipping.application.command`)
1. `CreateShipment` (`CreateShipmentCommand`, `CreateShipmentHandler`, `CreateShipmentResult`) — creates a `Shipment`, calculates delivery window and baseline cost, generates the 6-digit `deliveryCode`, and appends the initial `CREATED` milestone.
2. `CreateConsolidationBatch` (`CreateConsolidationBatchCommand`, `CreateConsolidationBatchHandler`, `CreateConsolidationBatchResult`) — groups eligible `STANDARD` shipments on the same corridor into a `ConsolidationBatch`.
3. `DispatchConsolidationBatch` (`DispatchConsolidationBatchCommand`, `DispatchConsolidationBatchHandler`, `DispatchConsolidationBatchResult`) — dispatches an open batch via Internal Fleet or 3PL, allocates actual carrier cost across member shipments, and records consolidation savings.
4. `AssignRider` (`AssignRiderCommand`, `AssignRiderHandler`) — assigns an available `DispatchRider` and `FleetVehicle` to a shipment.
5. `Route3PL` (`Route3PLCommand`, `Route3PLHandler`) — books a shipment with a 3PL carrier through `ThirdPartyCarrierPort`.
6. `AdvanceShipmentMilestone` (`AdvanceShipmentMilestoneCommand`, `AdvanceShipmentMilestoneHandler`) — records hub scans (`ARRIVED_ORIGIN_DISTRIBUTION_CENTER`, `LEFT_ORIGIN_DISTRIBUTION_CENTER`, `ARRIVED_DESTINATION_DISTRIBUTION_CENTER`, `OUT_FOR_DELIVERY`) or intra-state milestones (`PICKED_UP_FROM_STORE`, `IN_TRANSIT`, `ARRIVING_SOON`) and appends `ShipmentMilestoneLog`.
7. `UpdateShipmentLocation` (`UpdateShipmentLocationCommand`, `UpdateShipmentLocationHandler`) — updates live GPS coordinates during transit.
8. `CompleteDelivery` (`CompleteDeliveryCommand`, `CompleteDeliveryHandler`) — verifies the customer's 6-digit `deliveryCode` + `proofOfDeliveryUrl`, marks `DELIVERED`, and releases the assigned rider.
9. `RecordFailedDeliveryAttempt` (`RecordFailedDeliveryAttemptCommand`, `RecordFailedDeliveryAttemptHandler`) — records an unsuccessful delivery attempt (`deliveryAttempts++`) or transitions to `FAILED` with `DeliveryFaultType` (`CUSTOMER_FAULT` requires $\ge 2$ attempts).
10. `CalculateMonthlyMerchantIncentive` (`CalculateMonthlyMerchantIncentiveCommand`, `CalculateMonthlyMerchantIncentiveHandler`, `CalculateMonthlyMerchantIncentiveResult`) — calculates and credits the 10% monthly shipping incentive for delivered `CUSTOMER_ORDER` shipments.
11. `ConfigureCoverageZone` (`ConfigureCoverageZoneCommand`, `ConfigureCoverageZoneHandler`, `ConfigureCoverageZoneResult`) — registers or updates a `CoverageZone`.
12. `UpdateMerchantShippingConfig` (`UpdateMerchantShippingConfigCommand`, `UpdateMerchantShippingConfigHandler`) — enables/disables `URGENT` delivery for a merchant's storefront.
13. `RegisterVehicle` / `RegisterRider` / `SetRiderOnline` / `SetRiderOffline` — manages AtlasHub's internal fleet.

### Queries (`com.atlashub.logistics.shipping.application.query`)
1. `CalculateShippingQuotes` (`CalculateShippingQuotesQuery`, `CalculateShippingQuotesHandler`, `ShippingQuoteResult`) — returns coverage status (`covered`, `fulfillmentChannel`), `STANDARD` quote + delivery window, and `URGENT` quote + delivery window (if enabled by merchant and supported in zone).
2. `GetShipmentDeliveryCode` (`GetShipmentDeliveryCodeQuery`, `GetShipmentDeliveryCodeHandler`, `DeliveryCodeResult`) — returns the 6-digit `deliveryCode` for the business/storefront to display to the customer.
3. `TrackShipment` (`TrackShipmentQuery`, `TrackShipmentHandler`, `ShipmentTrackingResult`) — `@PublicEndpoint` returning `routeType`, `currentMilestone`, `deliveryWindowStart`, `deliveryWindowEnd`, `milestoneHistory`, and `liveLocation`.
4. `GetShipmentDetails` / `ListShipments` — merchant/dispatcher shipment queries.
5. `ListMerchantShippingIncentives` (`ListMerchantShippingIncentivesQuery`, `ListMerchantShippingIncentivesHandler`, `MerchantShippingIncentiveSummary`) — returns the merchant-visible monthly rebate credit (`billingYearMonth`, `eligibleShipmentCount`, `rebateAmount`, `status`, `creditedAt`) **without** exposing internal carrier costs or the 10% percentage.
6. `ListCoverageZones` / `ListRiders` / `ListVehicles` — fleet and coverage queries.

---

## Presentation Layer (`com.atlashub.logistics.shipping.presentation`)

### Controllers (`presentation/rest`)
1. `ShipmentController` (`/api/v1/shipments`)
   - `POST /api/v1/shipments/quotes` — calculate `STANDARD` & `URGENT` shipping fee and delivery windows at checkout
   - `POST /api/v1/shipments` — create shipment
   - `GET /api/v1/shipments` — list shipments for organization
   - `GET /api/v1/shipments/details?id={id}` — get shipment details
   - `GET /api/v1/shipments/delivery-code?shipmentId={id}` — retrieve storefront delivery code for customer display
   - `GET /api/v1/shipments/track?trackingNumber={trackingNumber}` (`@PublicEndpoint`) — route-aware milestone & live GPS tracking
   - `POST /api/v1/shipments/{id}/assign-rider` — assign internal rider
   - `POST /api/v1/shipments/{id}/route-3pl` — dispatch via 3PL partner
   - `POST /api/v1/shipments/{id}/milestones` — advance `INTRA_STATE` / `INTER_STATE` milestone
   - `POST /api/v1/shipments/{id}/location` — update live GPS location
   - `POST /api/v1/shipments/{id}/complete-delivery` — verify `deliveryCode` + `proofOfDeliveryUrl` and mark delivered
   - `POST /api/v1/shipments/{id}/failed-attempt` — record failed attempt or mark failed under 3-part policy
2. `ConsolidationController` (`/api/v1/shipments/batches` & `/api/v1/shipments/incentives`)
   - `POST /api/v1/shipments/batches` — create consolidation batch
   - `POST /api/v1/shipments/batches/{id}/dispatch` — dispatch consolidation batch
   - `GET /api/v1/shipments/incentives` — view merchant monthly shipping incentive rebates
   - `POST /api/v1/shipments/incentives/calculate` — trigger monthly incentive calculation
3. `FleetController` (`/api/v1/logistics/fleet`)
   - Manage `CoverageZone`, `MerchantShippingConfig`, `FleetVehicle`, and `DispatchRider`.


---

## Target v2 module-specific changes

External delivery becomes **merchant-connected carrier capability** per location/country, including international legs and last mile when provider coverage allows. Existing Atlas fleet/flat state routes/mock ThirdPartyCarrierPort quotes are legacy/test-only, not real global shipment proof. Merchant may use inventory and business accounts without any carrier; connected eligible carrier enables quote/book/track/returns. AtlasHub does not default to taking carrier fees or managing the fleet. [Provider transition](../architecture/provider-banking-decommission.md).
