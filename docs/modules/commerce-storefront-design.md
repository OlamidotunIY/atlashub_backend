# Commerce Storefront Design (`atlashub-commerce` / `com.atlashub.commerce.storefront`)

## Role & Purpose

The `storefront` subpackage handles all transactional commerce flows: POS sales, hospitality (tables, kitchen order tickets), till management, online orders, credit sales, and layaway. It is the core revenue-generating engine and orchestrates the Checkout Saga.

Gradle module: `atlashub-commerce`  
Package: `com.atlashub.commerce.storefront`

---

## Domain Layer

### `SalesOrder` (Aggregate Root)

**Package:** `com.atlashub.commerce.storefront.domain.entities`

```
SalesOrder
├── id              : Long
├── organizationId  : Long
├── outletId        : Long
├── vendorId        : Long              ← nullable; set for marketplace orders
├── customerId      : Long              ← nullable
├── cashierId       : Long
├── tillId          : Long              ← nullable for online orders
├── type            : OrderType         ← POS_RETAIL | POS_WHOLESALE | POS_HOSPITALITY | ONLINE | LAYAWAY | PROFORMA
├── status          : OrderStatus       ← PENDING | PAYMENT_PENDING | COMPLETED | REFUNDED | FAILED | LAYAWAY | PROFORMA
├── items           : List<SalesOrderItem>
├── discountId      : Long              ← nullable
├── totalGross      : Money
├── totalDiscount   : Money
├── totalTax        : Money
├── totalNet        : Money
├── paymentMethod   : PaymentMethod     ← CASH | CARD | BANK_TRANSFER | USSD | CREDIT | LAYAWAY
├── chargeReference : String            ← pay module charge reference; nullable
├── saleDate        : ZonedDateTime
└── deliveryAddress : String            ← nullable; for online orders
```

**Business methods (on entity):**

| Method | Guard | Events | Exceptions |
|---|---|---|---|
| `addItem(productId, variantId, qty, unitPrice)` | status == PENDING | — | `InvalidOrderStateException` |
| `applyDiscount(Discount discount)` | status == PENDING | — | `DiscountExpiredException`, `DiscountMaxUsesReachedException` |
| `initiatePayment(chargeReference)` | status == PENDING | — | → transitions to PAYMENT_PENDING |
| `completePayment()` | status == PAYMENT_PENDING or PENDING (cash) | `PosSaleCompletedEvent` | `InvalidOrderStateException` |
| `failPayment(reason)` | status == PAYMENT_PENDING | `PosSaleFailedEvent` | `InvalidOrderStateException` |
| `refund(reason)` | status == COMPLETED | `PosSaleRefundedEvent` | `InvalidOrderStateException` |
| `convertToLayaway(depositAmount)` | status == PENDING | `LayawayCreatedEvent` | — |

---

### `SalesOrderItem` (Entity)

```
SalesOrderItem
├── id             : Long
├── salesOrderId   : Long
├── productId      : Long
├── variantId      : Long              ← nullable
├── quantity       : Integer
├── unitPrice      : Money
├── totalPrice     : Money
├── taxAmount      : Money
└── discountAmount : Money
```

---

### `HospitalityTable` (Aggregate Root)

```
HospitalityTable
├── id             : Long
├── organizationId : Long
├── outletId       : Long
├── tableNumber    : String
├── covers         : Integer
├── status         : TableStatus       ← AVAILABLE | OCCUPIED | RESERVED | BILL_REQUESTED
└── currentOrderId : Long              ← nullable
```

**Business methods:**
- `occupy(orderId, covers)` → guard status == AVAILABLE → `TableAlreadyOccupiedException`
- `requestBill()` → guard status == OCCUPIED
- `clear()` → status → AVAILABLE

---

### `KitchenOrderTicket` (Aggregate Root)

```
KitchenOrderTicket
├── id           : Long
├── salesOrderId : Long
├── tableId      : Long
├── outletId     : Long
├── items        : List<KotItem>
├── status       : KotStatus          ← PENDING | IN_PROGRESS | READY | SERVED
└── sentAt       : ZonedDateTime
```

**Business methods:**
- `markInProgress()` → PENDING → IN_PROGRESS
- `markReady()` → IN_PROGRESS → READY; registers `KotReadyEvent`
- `markServed()` → READY → SERVED

### `KotItem` (Entity)

```
KotItem
├── id        : Long
├── kotId     : Long
├── productId : Long
├── name      : String
└── quantity  : Integer
```

---

### `Till` (Aggregate Root)

```
Till
├── id                      : Long
├── organizationId          : Long
├── outletId                : Long
├── name                    : String
├── openingFloat            : Money
├── expectedClosingBalance  : Money
├── actualClosingBalance    : Money    ← nullable until closed
├── status                  : TillStatus  ← OPEN | CLOSED
├── openedAt                : ZonedDateTime
├── closedAt                : ZonedDateTime ← nullable
├── openedBy                : Long
└── closedBy                : Long     ← nullable
```

**Business methods:**
- `open(openingFloat)` → CLOSED → OPEN; registers `TillOpenedEvent`
- `close(closedBy, actualBalance)` → OPEN → CLOSED; registers `TillClosedEvent`

---

### `CustomerCredit` (Aggregate Root)

```
CustomerCredit
├── id              : Long
├── organizationId  : Long
├── customerId      : Long
├── creditLimit     : Money
├── outstandingDebt : Money
└── status          : CreditStatus    ← WITHIN_LIMIT | OVER_LIMIT | SETTLED | BLOCKED
```

**Business methods:**
- `extendCredit(amount)` — pessimistic lock; guards `outstandingDebt + amount <= creditLimit`; throws `CreditLimitExceededException`
- `settle(amount)` — reduces `outstandingDebt`
- `block()`, `unblock()`

---

### `CustomerDeposit` (Aggregate Root) — Layaway

```
CustomerDeposit
├── id               : Long
├── salesOrderId     : Long
├── amountPaid       : Money
├── balanceRemaining : Money
└── status           : DepositStatus  ← ACTIVE | RECALLED | FULFILLED
```

**Business methods:**
- `addPayment(amount)` — reduces `balanceRemaining`; if `balanceRemaining == 0` → FULFILLED
- `recall()` → ACTIVE → RECALLED (refund scenario)

---

### Domain Events — `com.atlashub.commerce.storefront.domain.events`

All events published to Kafka topic **`commerce-events`**.

| Event | Published When | Consumed By |
|---|---|---|
| `SalesOrderPaymentInitiatedEvent` | Non-cash checkout — order waiting for payment | `pay:charges` (listens → creates Charge record) |
| `PosSaleCompletedEvent` | Sale fully paid (cash or card) | `accounting-ap-ar` (Dr Cash/AR, Cr Revenue), `notifications` (send receipt), `analytics` (update daily sales) |
| `PosSaleFailedEvent` | Payment failed/timed out | `notifications` (notify cashier), `analytics` |
| `PosSaleRefundedEvent` | Sale marked refunded | `accounting-ap-ar` (reverse AR entry), `pay:transfers` (listens → initiate refund payout via `ChargeRefundInitiatedEvent` chain) |
| `CreditSaleCompletedEvent` | Credit sale completed (AR updated) | `accounting-ap-ar` (create receivable record), `notifications`, `analytics` |
| `TillOpenedEvent` | Till opened by cashier | `pay:ledger` (post Dr TILL → Cr Operating for opening float) |
| `TillClosedEvent` | Till closed at end of day | `pay:ledger` (sweep TILL → Operating), `accounting-cash` (record cash banking) |
| `LayawayCreatedEvent` | Layaway/deposit initiated | `notifications` |
| `KotReadyEvent` | Kitchen marks order ready | `SelectiveWebSocketBroadcaster` → `/topic/outlet/{outletId}/kitchen` |
| `KitchenOrderTicketCreatedEvent` | KOT sent to kitchen | `SelectiveWebSocketBroadcaster` → `/topic/outlet/{outletId}/kitchen` |
| `OnlineOrderCreatedEvent` | Online order placed | `logistics-shipping` (create shipment), `notifications` (confirm to customer) |

**Key event payload shapes:**
```
SalesOrderPaymentInitiatedEvent.payload
├── salesOrderId      : Long
├── organizationId    : Long
├── outletId          : Long
├── chargeReference   : String       ← UUID generated by commerce; correlates charge back to order
├── amount            : BigDecimal
├── currency          : String
├── paymentMethod     : String       ← CARD | BANK_TRANSFER | USSD | POS_TERMINAL
├── cashierId         : Long         ← nullable; for WS push to cashier
└── initiatedAt       : ZonedDateTime

PosSaleCompletedEvent.payload
├── salesOrderId      : Long
├── organizationId    : Long
├── outletId          : Long
├── totalNet          : BigDecimal
├── currency          : String
├── paymentMethod     : String
├── cashierId         : Long         ← nullable
└── completedAt       : ZonedDateTime

PosSaleRefundedEvent.payload
├── salesOrderId      : Long
├── organizationId    : Long
├── chargeReference   : String       ← needed by pay:transfers to find the original charge
├── refundAmount      : BigDecimal
├── currency          : String
├── refundReason      : String
├── customerNuban     : String       ← bank account to refund to; nullable for cash refunds
├── customerBankCode  : String       ← nullable
└── refundedAt        : ZonedDateTime

CreditSaleCompletedEvent.payload
├── salesOrderId      : Long
├── organizationId    : Long
├── customerId        : Long
├── totalNet          : BigDecimal
├── currency          : String
└── completedAt       : ZonedDateTime

TillOpenedEvent.payload
├── tillSessionId     : Long         ← the Till aggregate id for this session
├── organizationId    : Long
├── outletId          : Long
├── cashierId         : Long
├── openingFloat      : BigDecimal
├── currency          : String
└── openedAt          : ZonedDateTime

TillClosedEvent.payload
├── tillSessionId     : Long
├── organizationId    : Long
├── outletId          : Long
├── cashierId         : Long
├── closingCash       : BigDecimal   ← actual closing cash count
├── totalSales        : BigDecimal
├── currency          : String
└── closedAt          : ZonedDateTime
```

---

### Domain Exceptions — `com.atlashub.commerce.storefront.domain.exceptions`

```java
public class SalesOrderNotFoundException extends NotFoundException {
    public SalesOrderNotFoundException(Long id) { super("Sales order not found: " + id); }
}
public class InvalidOrderStateException extends BusinessRuleException {
    public InvalidOrderStateException(String message) { super(message); }
}
public class TillNotFoundException extends NotFoundException {
    public TillNotFoundException(Long id) { super("Till not found: " + id); }
}
public class TillAlreadyOpenException extends ConflictException {
    public TillAlreadyOpenException() { super("A till is already open for this outlet"); }
}
public class TillNotOpenException extends BusinessRuleException {
    public TillNotOpenException() { super("Till is not open for transactions"); }
}
public class TableNotFoundException extends NotFoundException {
    public TableNotFoundException(Long id) { super("Table not found: " + id); }
}
public class TableAlreadyOccupiedException extends ConflictException {
    public TableAlreadyOccupiedException() { super("This table is already occupied"); }
}
public class CreditLimitExceededException extends BusinessRuleException {
    public CreditLimitExceededException() { super("This sale would exceed the customer's credit limit"); }
}
public class CustomerCreditBlockedException extends BusinessRuleException {
    public CustomerCreditBlockedException() { super("Customer credit account is blocked"); }
}
public class KitchenOrderTicketNotFoundException extends NotFoundException {
    public KitchenOrderTicketNotFoundException(Long id) { super("Kitchen order ticket not found: " + id); }
}
```

---

## Application Layer

### Commands — `com.atlashub.commerce.storefront.application.commands`

#### `ProcessPosCheckoutCommand`
```java
record ProcessPosCheckoutCommand(Long organizationId, Long outletId, Long tillId,
                                 Long customerId, OrderType type, List<OrderItemDto> items,
                                 Long discountId, PaymentMethod paymentMethod, Long cashierId)
```
**Handler:** `ProcessPosCheckoutHandler` | **Response:** `ProcessPosCheckoutResponse(Long orderId, String chargeReference)`  
**RBAC:** `@PreAuthorize("hasAuthority('commerce:orders:create')")`  
**Flow (Checkout Saga Step 1–3):**
1. Create `SalesOrder` (status = RESERVING_STOCK / PENDING)
2. `repository.save()` publishes `SalesOrderCreatedEvent` via transactional Outbox.
3. `commerce:inventory` listens to `SalesOrderCreatedEvent`, locks inventory rows (`PESSIMISTIC_WRITE`), and asynchronously publishes `StockReservedEvent` or `StockReservationFailedEvent`.
4. On `StockReservedEvent`: if paymentMethod == CASH, order completes and publishes `PosSaleCompletedEvent`. Else, order transitions to `PAYMENT_PENDING` and publishes `SalesOrderPaymentInitiatedEvent`.
5. On `StockReservationFailedEvent`: order marks `FAILED` and notifies cashier via WebSocket.

> **Architecture note:** Synchronous cross-module writes between `storefront` and `inventory` are prohibited. Stock reservation is orchestrated asynchronously through Kafka domain events (`SalesOrderCreatedEvent`, `StockReservedEvent`, `StockReservationFailedEvent`, `PosSaleCompletedEvent`, `PosSaleFailedEvent`).

---

#### `CompletePaymentCommand`
```java
record CompletePaymentCommand(Long salesOrderId)
```
**Handler:** `CompletePaymentHandler` | **Response:** `void`  
**Invocation source:** `ChargeSuccessfulListener`  
**Flow:** Load `SalesOrder` → `order.completePayment()` → `repository.save()` → `PosSaleCompletedEvent` published → `commerce:inventory` listens to `PosSaleCompletedEvent` and deducts reserved stock asynchronously.

---

#### `FailPaymentCommand`
```java
record FailPaymentCommand(Long salesOrderId, String reason)
```
**Handler:** `FailPaymentHandler` | **Response:** `void`  
**Invocation source:** `ChargeFailedListener` or `StockReleaseScheduler` (timeout)  
**Flow:** Load `SalesOrder` → `order.failPayment(reason)` → `repository.save()` → `PosSaleFailedEvent` published → `commerce:inventory` listens to `PosSaleFailedEvent` and releases reserved stock asynchronously.

---

#### `RefundPosSaleCommand`
```java
record RefundPosSaleCommand(Long salesOrderId, String reason, RefundMethod refundMethod)
```
**Handler:** `RefundPosSaleHandler` | **Response:** `void`  
**Flow:** Load `SalesOrder` → `order.refund(reason)` → restore inventory stock → `repository.save()` → `PosSaleRefundedEvent` published → pay issues refund payout

---

#### `OpenTillCommand`
```java
record OpenTillCommand(Long organizationId, Long outletId, Long userId, Money openingFloat)
```
**Handler:** `OpenTillHandler` | **Response:** `OpenTillResponse(Long tillId)`  
**RBAC:** `@PreAuthorize("hasAuthority('commerce:till:open')")`  
**Flow:** Check no OPEN till for outlet → `TillAlreadyOpenException`; create `Till` → `till.open(openingFloat)` → `repository.save()` → `TillOpenedEvent` published

---

#### `CloseTillCommand`
```java
record CloseTillCommand(Long tillId, Long closedBy, Money actualClosingBalance)
```
**Handler:** `CloseTillHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('commerce:till:open')")`  
**Flow:** Load `Till` → `till.close(closedBy, actualBalance)` → `repository.save()` → `TillClosedEvent` published

---

#### `OccupyTableCommand`
```java
record OccupyTableCommand(Long tableId, int covers, Long waiterId)
```
**Handler:** `OccupyTableHandler` | **Response:** `void`

---

#### `SendKitchenOrderCommand`
```java
record SendKitchenOrderCommand(Long salesOrderId, Long tableId, List<KotItemDto> items)
```
**Handler:** `SendKitchenOrderHandler` | **Response:** `SendKitchenOrderResponse(Long kotId)`  
**Flow:** Create `KitchenOrderTicket` → `repository.save()` → `KitchenOrderTicketCreatedEvent` published → WS broadcaster pushes to kitchen display

---

#### `MarkKotReadyCommand`
```java
record MarkKotReadyCommand(Long kotId)
```
**Handler:** `MarkKotReadyHandler` | **Response:** `void`  
**Flow:** Load `KitchenOrderTicket` → `kot.markReady()` → `repository.save()` → `KotReadyEvent` published → WS broadcaster pushes to waiter

---

#### `CreateOnlineOrderCommand`
```java
record CreateOnlineOrderCommand(Long organizationId, Long customerId, List<OrderItemDto> items,
                                String deliveryAddress, PaymentMethod paymentMethod)
```
**Handler:** `CreateOnlineOrderHandler` | **Response:** `CreateOnlineOrderResponse(Long orderId, String checkoutUrl)`  
**Flow:** Same as `ProcessPosCheckoutHandler` but type = ONLINE; after payment → `OnlineOrderCreatedEvent` published → logistics creates shipment

---

#### `ProcessCreditSaleCommand`
```java
record ProcessCreditSaleCommand(Long organizationId, Long outletId, Long customerId,
                                List<OrderItemDto> items, Long cashierId)
```
**Handler:** `ProcessCreditSaleHandler` | **Response:** `ProcessCreditSaleResponse(Long orderId)`  
**Flow:** Load `CustomerCredit` with pessimistic lock → `credit.extendCredit(totalNet)` → create `SalesOrder` (PAYMENT_METHOD = CREDIT) → `order.completePayment()` → `repository.save()`

---

### Queries — `com.atlashub.commerce.storefront.application.queries`

#### `ListPosTransactionsQuery`
```java
record ListPosTransactionsQuery(Long outletId, ZonedDateTime from, ZonedDateTime to,
                                Long tillId, Long cashierId, int page, int size)
```
**Handler:** `ListPosTransactionsHandler` | **Result:** `PageResult<SalesOrderResult>`

---

#### `GetTillSummaryQuery`
```java
record GetTillSummaryQuery(Long tillId)
```
**Handler:** `GetTillSummaryHandler` | **Result:** `TillSummaryResult(Long tillId, Money openingFloat, Money expectedClosingBalance, Money actualClosingBalance, TillStatus status)`

---

#### `ListActiveTablesQuery`
```java
record ListActiveTablesQuery(Long outletId)
```
**Handler:** `ListActiveTablesHandler` | **Result:** `List<TableResult>` (bounded per outlet)

---

## Infrastructure Layer

### Persistence

**JPA Entities:**

| Entity | Table | Locking |
|---|---|---|
| `SalesOrderJpaEntity` | `commerce_sales_orders` | `@Version` optimistic |
| `SalesOrderItemJpaEntity` | `commerce_sales_order_items` | — |
| `HospitalityTableJpaEntity` | `commerce_hospitality_tables` | `@Version` optimistic |
| `KitchenOrderTicketJpaEntity` | `commerce_kitchen_order_tickets` | `@Version` optimistic |
| `KotItemJpaEntity` | `commerce_kot_items` | — |
| `TillJpaEntity` | `commerce_tills` | `@Version` optimistic |
| `CustomerCreditJpaEntity` | `commerce_customer_credits` | `@Lock(PESSIMISTIC_WRITE)` during credit extension |
| `CustomerDepositJpaEntity` | `commerce_customer_deposits` | — |

### Listeners — `infrastructure/messaging/listeners/`

#### `ChargeSuccessfulListener`
| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `commerce-payment-group` |
| **Event consumed** | `ChargeSuccessfulEvent` |
| **Payload** | `chargeReference`, `orderId`, `amount`, `paidAt` |
| **Command called** | `CompletePaymentCommand` |
| **Flow** | Deducts stock, completes order, publishes `PosSaleCompletedEvent` |
| **Idempotency** | Inbox keyed on `(eventId, "commerce-payment-group")` — duplicate must not deduct stock twice |

#### `ChargeFailedListener`
| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `commerce-payment-group` |
| **Event consumed** | `ChargeFailedEvent` |
| **Payload** | `chargeReference`, `orderId`, `failureReason` |
| **Command called** | `FailPaymentCommand` |
| **Flow** | Releases reserved stock, marks order FAILED |

#### `PosFeatureToggledListener`
| Attribute | Value |
|---|---|
| **Topic** | `accounts-events` |
| **Group ID** | `commerce-pos-feature-group` |
| **Event consumed** | `PosFeatureToggledEvent` |
| **Payload** | `organizationId`, `posEnabled`, `toggledAt` |
| **Flow** | Updates the org's POS status in Redis cache (`pos:enabled:{orgId}` → `true`/`false`). POS endpoint guards read this key to return `403` when POS is disabled |
| **Idempotency** | Redis `SET` is idempotent — safe to replay |

### Scheduler — `infrastructure/schedulers/`

#### `StockReleaseScheduler`
| Attribute | Value |
|---|---|
| **Cron** | `0 * * * * *` (every minute) |
| **Finds** | `SalesOrder` records with status `PAYMENT_PENDING` older than 15 minutes |
| **Action** | Calls `FailPaymentHandler` for each → releases reserved stock |
| **Response** | `void` |

### Application Ports

Storefront performs no synchronous cross-module writes. Cross-module orchestration is handled asynchronously via Kafka domain events through the transactional outbox. Cross-module reads go through shared query port interfaces in `atlashub-shared/src/main/java/com/atlashub/shared/application/port`.

---

## Presentation Layer

### Controllers

- `CommercePosController` — POS checkout, till management
- `CommerceHospitalityController` — tables, KOT
- `CommerceOnlineController` — online orders, credit sales, layaway

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/pos/checkout` | `commerce:orders:create` | `ProcessPosCheckoutRequest` | `ProcessPosCheckoutResponse` |
| `POST` | `/pos/credit-sale` | `commerce:orders:create` | `ProcessCreditSaleRequest` | `ProcessCreditSaleResponse` |
| `POST` | `/pos/refund` | `commerce:orders:create` | `RefundPosSaleRequest` | `void` |
| `GET` | `/pos/transactions` | — | `?outletId&from&to&tillId&cashierId&page&size` | `PageResult<SalesOrderResult>` |
| `POST` | `/tills` | `commerce:till:open` | `OpenTillRequest` | `OpenTillResponse` |
| `POST` | `/tills/{id}/close` | `commerce:till:open` | `CloseTillRequest` | `void` |
| `GET` | `/tills/{id}/summary` | — | — | `TillSummaryResult` |
| `POST` | `/tables/{id}/occupy` | — | `OccupyTableRequest` | `void` |
| `GET` | `/tables` | — | `?outletId` | `List<TableResult>` |
| `POST` | `/kitchen-orders` | — | `SendKitchenOrderRequest` | `SendKitchenOrderResponse` |
| `POST` | `/kitchen-orders/{id}/ready` | — | — | `void` |
| `POST` | `/orders/online` | — | `CreateOnlineOrderRequest` | `CreateOnlineOrderResponse` |

---

## RBAC Table

| Permission | Commands |
|---|---|
| `commerce:orders:create` | `ProcessPosCheckoutHandler`, `ProcessCreditSaleHandler`, `RefundPosSaleHandler` |
| `commerce:till:open` | `OpenTillHandler`, `CloseTillHandler` |

---

## Socket Events

| Event | WS Channel | Payload |
|---|---|---|
| `KitchenOrderTicketCreatedEvent` | `/topic/outlet/{outletId}/kitchen` | `type: "NEW_KOT"`, `data: { kotId, tableNumber, items }` |
| `KotReadyEvent` | `/topic/outlet/{outletId}/kitchen` | `type: "KOT_READY"`, `data: { kotId, tableNumber }` |

---

## Checkout Saga

```
ProcessPosCheckoutHandler
  1. Create SalesOrder (PENDING)
  2. repository.save() emits SalesOrderCreatedEvent (commerce-events topic)

Asynchronous Choreography Saga:
  Step 1: inventory: SalesOrderCreatedListener
    → ReserveStockForOrderHandler (locks rows PESSIMISTIC_WRITE)
    → Success: StockReservedEvent
    → Failure: StockReservationFailedEvent

  Step 2: storefront: StockReservedListener
    → Cash payment: CompletePaymentHandler → PosSaleCompletedEvent
    → Non-cash: InitiatePaymentHandler → SalesOrderPaymentInitiatedEvent

  Step 3: pay:charges: SalesOrderPaymentInitiatedListener
    → Initialize charge and listen for customer payment
    → Success: ChargeSuccessfulEvent
    → Failure: ChargeFailedEvent

  Step 4: storefront: ChargeSuccessfulListener / ChargeFailedListener
    → ChargeSuccessfulEvent → CompletePaymentHandler → PosSaleCompletedEvent
    → ChargeFailedEvent (or StockReleaseScheduler timeout) → FailPaymentHandler → PosSaleFailedEvent

  Step 5: inventory: PosSaleCompletedListener / PosSaleFailedListener
    → PosSaleCompletedEvent → DeductReservedStockHandler (reserved → sold)
    → PosSaleFailedEvent → ReleaseReservedStockHandler (release reservation)
```

---

## Complete File List

```
atlashub-commerce/src/main/java/com/atlashub/commerce/storefront/
├── domain/entities/ [SalesOrder, SalesOrderItem, HospitalityTable, KitchenOrderTicket, KotItem, Till, CustomerCredit, CustomerDeposit]
├── domain/events/ [PosSaleCompletedEvent, PosSaleFailedEvent, PosSaleRefundedEvent, TillOpenedEvent, TillClosedEvent, LayawayCreatedEvent, KotReadyEvent, KitchenOrderTicketCreatedEvent, OnlineOrderCreatedEvent]
├── domain/exceptions/ [SalesOrderNotFoundException, InvalidOrderStateException, TillNotFoundException, TillAlreadyOpenException, TillNotOpenException, TableNotFoundException, TableAlreadyOccupiedException, CreditLimitExceededException, CustomerCreditBlockedException, KitchenOrderTicketNotFoundException]
├── domain/repositories/ [SalesOrderRepository, HospitalityTableRepository, KitchenOrderTicketRepository, TillRepository, CustomerCreditRepository, CustomerDepositRepository]
├── domain/valueobject/ [OrderType, OrderStatus, PaymentMethod, TableStatus, KotStatus, TillStatus, CreditStatus, DepositStatus, RefundMethod]
├── application/
│   ├── commands/ [ProcessPosCheckout, CompletePayment, FailPayment, RefundPosSale, OpenTill, CloseTill, OccupyTable, SendKitchenOrder, MarkKotReady, CreateOnlineOrder, ProcessCreditSale]
│   ├── queries/ [ListPosTransactions, GetTillSummary, ListActiveTables]
│   └── port/ [PayInitializeChargePort]
├── infrastructure/
│   ├── messaging/events/ [ChargeSuccessfulPayload, ChargeFailedPayload]
│   ├── messaging/listeners/ [ChargeSuccessfulListener, ChargeFailedListener]
│   ├── persistence/ [adapters, entities, mappers, repositories]
│   └── schedulers/ [StockReleaseScheduler]
└── presentation/ [dto/, rest/CommercePosController, rest/CommerceHospitalityController, rest/CommerceOnlineController]
```
