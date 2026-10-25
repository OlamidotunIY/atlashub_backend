# Commerce Module Design (`atlashub-commerce`) — Index

## Overview

The `atlashub-commerce` module is the **retail and marketplace engine** of AtlasHub. It is a single Gradle module (`atlashub-commerce`) with three internal subpackages, each documented separately.

All subpackages share the root package `com.atlashub.commerce`.

> **Subscription:** All organizations automatically get access to Atlas Commerce as part of the universal subscription. No separate registration or opt-in is needed.

> **POS Feature Toggle:** The Point-of-Sale (POS) sub-feature is **opt-in**. It is controlled by the `posEnabled` flag on the `Organization` aggregate in the `accounts` module. When `posEnabled = false`, all POS endpoints (`/pos/*`, `/tills/*`, `/tables/*`, `/kitchen-orders/*`) must return `403 Forbidden`. The `PosFeatureToggledEvent` from `accounts` drives this — `commerce-storefront` caches the org's POS status in Redis. All other Commerce features (online orders, inventory, marketplace) remain available regardless.

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

Checkout is environment-bound. `TEST` commerce orders publish `CheckoutPaymentRequestedEvent` with `environment=TEST` and may be completed only by a matching TEST charge event; LIVE follows the same rule. Commerce never selects Paystack or another online gateway and never calls a payment write port synchronously. It requests card, USSD, bank transfer, or an AtlasHub terminal assignment. The charge module owns provider routing; only POS setup exposes the supported terminal choices (Paystack, Moniepoint, and OPay initially).

### WebSocket Events
`KitchenOrderTicketCreatedEvent` and `KotReadyEvent` are broadcast via `SelectiveWebSocketBroadcaster` to `/topic/outlet/{outletId}/kitchen`.

### Module Dependencies
- **Reads from shared:** `EntitlementQueryPort`, `UserQueryPort`
- **Publishes events to:** `commerce-events` topic consumed by `inventory`, `accounting`, `pay`, `logistics`, `analytics`, `notifications`
- **Consumes events from:** `pay-events` (`ChargeSuccessfulEvent`, `ChargeFailedEvent`), `logistics-events` (`ShipmentDeliveredEvent`, `CustomerReturnShipmentReceivedEvent`), `accounts-events` (`PosFeatureToggledEvent`)

---

## Gradle

```groovy
// settings.gradle
include 'atlashub-commerce'
include 'atlashub-commerce:catalog'
```



---

## Proposed architecture boundary — third-party React storefront hosting

**Migration note (2026-10-25; not implemented):** This Java `commerce.storefront` domain remains responsible for POS, sales orders, checkout saga, reservations, transactions, and order state. **It is not** the website theme engine, marketplace creator dashboard, domain/HTTPS manager or host for uploaded third-party React code.

The new [atlashub_storefront TypeScript monorepo](https://github.com/OlamidotunIY/atlashub_storefront) owns the dedicated Merchant+Creator Storefront Portal, Vite/React Router theme SDK/scaffold, CLI/publishing, versioned theme releases, per-storefront config/assets, Cloudflare Workers for Platforms isolated rendering and domain infrastructure. Java exports controlled public commerce APIs and platform theme entitlements; no extra backend repository.

The documented Paystack/Anchor routing and specific charge event names above describe existing/legacy design; target global provider integration uses merchant-connected capabilities and normalized payment events. Preserve Java stock/order/financial invariants and checkout authority throughout migration. Read [full Java integration proposal](../architecture/storefront-platform-integration.md) and [Storefront platform proposal](../architecture/storefront-theme-platform-proposal.md).
