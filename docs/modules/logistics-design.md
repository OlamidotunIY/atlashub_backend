# Logistics Module Design (`atlashub-logistics`) — Index

## Overview

The `atlashub-logistics` module is the **movement engine** of AtlasHub. It is a single Gradle module with four internal subpackages, each documented separately.

All subpackages share the root package `com.atlashub.logistics`.

---

## Subpackage Documentation

| Subpackage | Package | Design Doc |
|---|---|---|
| **Shipping & Fleet** | `com.atlashub.logistics.shipping` | [logistics-shipping-design.md](logistics-shipping-design.md) |
| **Logistics-as-a-Service** | `com.atlashub.logistics.laas` | [logistics-laas-design.md](logistics-laas-design.md) |
| **Warehousing** | `com.atlashub.logistics.warehousing` | [logistics-warehousing-design.md](logistics-warehousing-design.md) |
| **Returns** | `com.atlashub.logistics.returns` | [logistics-returns-design.md](logistics-returns-design.md) |

---

## Key Cross-Cutting Concerns

### Operating Modes
A single organization can use all three modes simultaneously: **Internal Fleet** (own vehicles/riders), **LaaS** (as a logistics company serving shipper clients), and **3PL** (routing through external carriers like GIG Logistics, DHL).

### WebSocket Events
`ShipmentLocationUpdatedEvent` → `/topic/shipment/{trackingNumber}` (public tracking board)  
`ShipmentAssignedEvent` → `/user/{riderId}/queue/notifications` (rider notification)  
Dispatched via `SelectiveWebSocketBroadcaster` in notifications module.

### Public Tracking
`GET /api/v1/tracking?trackingNumber=ATL-xxx` — `@PublicEndpoint`, no auth required.

### Saga Participation
Logistics is a participant in the **Stock Transfer Saga** (choreography). See [sagas-design.md](../architecture/sagas-design.md).

### Module Dependencies
- **Publishes to:** `logistics-events` topic consumed by `commerce`, `accounting`, `notifications`
- **Consumes from:** `commerce-events` (`StockTransferApprovedEvent`, `PurchaseOrderSentEvent`, `OnlineOrderCreatedEvent`)

---

## Gradle

```groovy
// settings.gradle
include 'atlashub-logistics'
```
