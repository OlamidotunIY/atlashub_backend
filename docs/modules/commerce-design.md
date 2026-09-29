# Commerce Module Design (`atlashub-commerce`) — Index

## Overview

The `atlashub-commerce` module is the **retail and marketplace engine** of AtlasHub. It is a single Gradle module (`atlashub-commerce`) with three internal subpackages, each documented separately.

All subpackages share the root package `com.atlashub.commerce`.

---

## Subpackage Documentation

| Subpackage | Package | Design Doc |
|---|---|---|
| **Product Catalog** | `com.atlashub.commerce.catalog` | [commerce-catalog-design.md](commerce-catalog-design.md) |
| **Storefront & POS** | `com.atlashub.commerce.storefront` | [commerce-storefront-design.md](commerce-storefront-design.md) |
| **Inventory** | `com.atlashub.commerce.inventory` | [commerce-inventory-design.md](commerce-inventory-design.md) |

---

## Key Cross-Cutting Concerns

### Checkout Saga (Choreography)
POS/online checkout reserves stock → triggers pay charge → awaits `ChargeSuccessfulEvent` or `ChargeFailedEvent` → deducts or releases stock. Timeout handled by `StockReleaseScheduler`. See [commerce-storefront-design.md](commerce-storefront-design.md) and [sagas-design.md](../architecture/sagas-design.md).

### WebSocket Events
`KitchenOrderTicketCreatedEvent` and `KotReadyEvent` are broadcast via `SelectiveWebSocketBroadcaster` to `/topic/outlet/{outletId}/kitchen`.

### Module Dependencies
- **Reads from shared:** `EntitlementQueryPort`, `UserQueryPort`
- **Publishes events to:** `commerce-events` topic consumed by `inventory`, `accounting`, `pay`, `logistics`, `analytics`, `notifications`
- **Consumes events from:** `pay-events` (`ChargeSuccessfulEvent`, `ChargeFailedEvent`), `logistics-events` (`ShipmentDeliveredEvent`, `CustomerReturnShipmentReceivedEvent`)

---

## Gradle

```groovy
// settings.gradle
include 'atlashub-commerce'
```
