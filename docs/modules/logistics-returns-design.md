# Logistics Returns Design (`:atlashub-logistics:returns` / `com.atlashub.logistics.returns`)

## Role & Purpose

The `:atlashub-logistics:returns` submodule manages reverse logistics and enforces AtlasHub's **3-Part Failed Delivery & Return Policy**:
1. **Failed Delivery Return-to-Origin (RTO)** (`ReturnReasonType.FAILED_DELIVERY_RTO` after mandatory 2 delivery attempts).
2. **Post-Delivery Customer Returns (RMA)** (`ReturnReasonType.CUSTOMER_RETURN`).
3. **3-Part Fault Attribution & Financial Resolution**:
   - **`CUSTOMER_FAULT`** (customer unreachable after 2 attempts, wrong address, refuses valid item):
     - Outbound shipping fee is **not refunded**.
     - Return-to-Origin (`returnShippingFee`) is deducted from the customer's item refund (`refundableItemAmount = max(0, originalItemAmount - returnShippingFee)`).
     - Customer refund is held until the item is physically returned and received (`status == RECEIVED`) at the merchant's outlet/warehouse.
   - **`MERCHANT_FAULT`** (wrong/damaged item packaged by merchant, or merchant cancels after dispatch):
     - Customer receives **100% item refund + 100% outbound shipping refund**.
     - Merchant is charged the shipping/return cost (`merchantPenaltyAmount`) and excluded from the monthly shipping incentive for that order.
   - **`CARRIER_FAULT`** (package lost or damaged in transit by 3PL or internal fleet):
     - Customer receives **100% item refund + 100% outbound shipping refund**.
     - Merchant is compensated up to `declaredValue` (`merchantCompensationAmount`), and AtlasHub records a carrier claim.

Gradle module: `:atlashub-logistics:returns`  
Package: `com.atlashub.logistics.returns`

---

## Domain Layer

### `ReturnShipment` (Aggregate Root)

**Package:** `com.atlashub.logistics.returns.domain.entities`

```
ReturnShipment
├── id                         : Long
├── organizationId             : Long
├── originalSalesOrderId       : Long
├── originalShipmentId         : Long
├── customerId                 : Long
├── originAddress              : Address
├── destinationOutletId        : Long
├── returnTrackingNumber       : String
├── reasonType                 : ReturnReasonType    ← FAILED_DELIVERY_RTO | CUSTOMER_RETURN
├── faultType                  : ReturnFaultType     ← CUSTOMER_FAULT | MERCHANT_FAULT | CARRIER_FAULT
├── deliveryAttemptsConfirmed  : int                 ← must be >= 2 for FAILED_DELIVERY_RTO with CUSTOMER_FAULT
├── originalItemAmount         : Money
├── originalShippingFee        : Money
├── returnShippingFee          : Money
├── declaredValue              : Money
├── customerRefundAmount       : Money               ← computed by 3-part policy
├── merchantPenaltyAmount      : Money               ← non-zero when MERCHANT_FAULT
├── merchantCompensationAmount : Money               ← non-zero when CARRIER_FAULT
├── refundCustomerShipping     : boolean
├── status                     : ReturnStatus        ← CREATED | IN_TRANSIT | RECEIVED | INSPECTED_APPROVED | REJECTED
├── inspectionNotes            : String              ← nullable
├── createdAt                  : ZonedDateTime
└── receivedAt                 : ZonedDateTime       ← nullable
```

**Domain Rules & Behavior Methods:**
- `ReturnShipment.create(...)`:
  - Enforces the 3-part policy invariants:
    - If `reasonType == FAILED_DELIVERY_RTO` and `faultType == CUSTOMER_FAULT`, requires `deliveryAttemptsConfirmed >= 2` (throws `InsufficientDeliveryAttemptsForRtoException` if $< 2$).
  - Automatically calculates financial resolution fields (`customerRefundAmount`, `merchantPenaltyAmount`, `merchantCompensationAmount`, `refundCustomerShipping`):
    - **`CUSTOMER_FAULT`**:
      - `refundCustomerShipping = false`
      - `customerRefundAmount = originalItemAmount.isGreaterThan(returnShippingFee) ? originalItemAmount.subtract(returnShippingFee) : Money.zero(originalItemAmount.currency())`
      - `merchantPenaltyAmount = Money.zero(...)`
      - `merchantCompensationAmount = Money.zero(...)`
    - **`MERCHANT_FAULT`**:
      - `refundCustomerShipping = true`
      - `customerRefundAmount = originalItemAmount.add(originalShippingFee)`
      - `merchantPenaltyAmount = originalShippingFee.add(returnShippingFee)`
      - `merchantCompensationAmount = Money.zero(...)`
    - **`CARRIER_FAULT`**:
      - `refundCustomerShipping = true`
      - `customerRefundAmount = originalItemAmount.add(originalShippingFee)`
      - `merchantPenaltyAmount = Money.zero(...)`
      - `merchantCompensationAmount = declaredValue`
  - Registers `ReturnShipmentCreatedEvent`.
- `markInTransit()` → `CREATED` → `IN_TRANSIT`
- `markReceived(String inspectionNotes, ZonedDateTime now)` → `IN_TRANSIT` or `CREATED` → `RECEIVED`; sets `receivedAt` and `inspectionNotes`, registers `ReturnShipmentReceivedEvent` (which triggers refund release in `commerce`/`pay`).
- `reject(String reason)` → `CREATED` or `IN_TRANSIT` or `RECEIVED` → `REJECTED`; registers `ReturnShipmentRejectedEvent`.

---

## Application Layer

### Commands (`com.atlashub.logistics.returns.application.command`)
- `CreateReturnShipment` (`CreateReturnShipmentCommand`, `CreateReturnShipmentHandler`, `CreateReturnShipmentResult`)
- `MarkReturnInTransit` (`MarkReturnInTransitCommand`, `MarkReturnInTransitHandler`)
- `ReceiveReturnShipment` (`ReceiveReturnShipmentCommand`, `ReceiveReturnShipmentHandler`)
- `RejectReturnShipment` (`RejectReturnShipmentCommand`, `RejectReturnShipmentHandler`)

### Queries (`com.atlashub.logistics.returns.application.query`)
- `GetReturnShipment` (`GetReturnShipmentQuery`, `GetReturnShipmentHandler`, `ReturnShipmentResult`)
- `ListReturnShipments` (`ListReturnShipmentsQuery`, `ListReturnShipmentsHandler`, `ReturnShipmentResult`)

---

## Presentation Layer

### Controller: `ReturnsController` (`/api/v1/logistics/returns`)
- `POST /api/v1/logistics/returns` — create return / RTO shipment with 3-part policy resolution
- `POST /api/v1/logistics/returns/{id}/in-transit` — mark return shipment in transit
- `POST /api/v1/logistics/returns/{id}/receive` — confirm return received at merchant outlet and release eligible refund
- `POST /api/v1/logistics/returns/{id}/reject` — reject return after inspection
- `GET /api/v1/logistics/returns/details?id={id}` — get return shipment details and financial breakdown
- `GET /api/v1/logistics/returns` — list return shipments for organization
