# Logistics Returns Design (`atlashub-logistics` / `com.atlashub.logistics.returns`)

## Role & Purpose

The `returns` subpackage manages reverse logistics: a rider collects goods from the customer's address and delivers them back to the designated outlet. The physical return shipment is linked to a `ReturnShipment` record here; the financial refund is handled by `atlashub-pay` after Commerce confirms the return.

Gradle module: `atlashub-logistics`  
Package: `com.atlashub.logistics.returns`

---

## Domain Layer

### `ReturnShipment` (Aggregate Root)

**Package:** `com.atlashub.logistics.returns.domain.entities`

```
ReturnShipment
├── id                   : Long
├── organizationId       : Long
├── originalSalesOrderId : Long
├── customerId           : Long
├── originAddress        : Address        ← customer's collection address
├── destinationOutletId  : Long
├── shipmentId           : Long           ← linked Shipment (from shipping subpackage) for rider tracking
├── trackingNumber       : String         ← same as linked Shipment's tracking number
├── status               : ReturnStatus  ← CREATED | IN_TRANSIT | RECEIVED | REJECTED
└── receivedAt           : ZonedDateTime  ← nullable
```

**Business methods (on entity):**
- `markInTransit()` → CREATED → IN_TRANSIT
- `markReceived()` → IN_TRANSIT → RECEIVED; registers `ReturnShipmentReceivedEvent`
- `reject(reason)` → CREATED or IN_TRANSIT → REJECTED

**Why on entity:** All transitions are pure state changes on own fields. The domain event triggers Commerce to approve the return and initiate the refund.

---

### Domain Events — `com.atlashub.logistics.returns.domain.events`

| Event | Published When | Consumed By |
|---|---|---|
| `ReturnShipmentCreatedEvent` | Return shipment created | `notifications` (notify customer with tracking number) |
| `ReturnShipmentReceivedEvent` | Rider delivers return to outlet | `commerce` (approve return → restore stock → initiate refund via `pay`) |

---

### Domain Exceptions — `com.atlashub.logistics.returns.domain.exceptions`

```java
public class ReturnShipmentNotFoundException extends NotFoundException {
    public ReturnShipmentNotFoundException(Long id) { super("Return shipment not found: " + id); }
}
public class InvalidReturnStateException extends BusinessRuleException {
    public InvalidReturnStateException(String message) { super(message); }
}
```

---

## Application Layer

### Commands — `com.atlashub.logistics.returns.application.commands`

#### `CreateReturnShipmentCommand`
```java
record CreateReturnShipmentCommand(
    Long organizationId, Long originalSalesOrderId, Long customerId,
    Address originAddress, Long destinationOutletId
)
```
**Handler:** `CreateReturnShipmentHandler` | **Response:** `CreateReturnShipmentResponse(Long returnShipmentId, String trackingNumber)`  
**Flow:**
1. Create `ReturnShipment` (status = CREATED)
2. Call shipping `CreateShipmentHandler` (type = RETURN) to get a `Shipment` for rider tracking → link `shipmentId` + `trackingNumber`
3. `repository.save(returnShipment)` → `ReturnShipmentCreatedEvent` published

---

#### `ReceiveReturnShipmentCommand`
```java
record ReceiveReturnShipmentCommand(Long returnShipmentId)
```
**Handler:** `ReceiveReturnShipmentHandler` | **Response:** `void`  
**Invocation source:** Called by rider (or outlet staff) when goods are physically received  
**Flow:** Load `ReturnShipment` → `returnShipment.markReceived()` → `repository.save()` → `ReturnShipmentReceivedEvent` published → Commerce `ApproveCustomerReturnHandler` fires

---

#### `RejectReturnShipmentCommand`
```java
record RejectReturnShipmentCommand(Long returnShipmentId, String reason)
```
**Handler:** `RejectReturnShipmentHandler` | **Response:** `void`  
**Flow:** Load `ReturnShipment` → `returnShipment.reject(reason)` → `repository.save()` → notify customer

---

### Queries — `com.atlashub.logistics.returns.application.queries`

#### `GetReturnShipmentQuery`
```java
record GetReturnShipmentQuery(Long returnShipmentId)
```
**Handler:** `GetReturnShipmentHandler` | **Result:** `ReturnShipmentResult`

`ReturnShipmentResult`: `id`, `originalSalesOrderId`, `customerId`, `originAddress`, `destinationOutletId`, `trackingNumber`, `status`, `receivedAt`

---

#### `ListReturnShipmentsQuery`
```java
record ListReturnShipmentsQuery(Long organizationId, ReturnStatus status, int page, int size)
```
**Handler:** `ListReturnShipmentsHandler` | **Result:** `PageResult<ReturnShipmentResult>`  
**Justification:** Returns can accumulate over time — pagination required.

---

## Infrastructure Layer

### Persistence

| JPA Entity | Table | Locking |
|---|---|---|
| `ReturnShipmentJpaEntity` | `logistics_return_shipments` | `@Version` optimistic |

**Spring Data:**
```
SpringDataReturnShipmentRepository
  + findByOriginalSalesOrderId(Long salesOrderId): Optional<ReturnShipmentJpaEntity>
  + findByOrganizationIdAndStatus(Long orgId, ReturnStatus status, Pageable p): Page<ReturnShipmentJpaEntity>
```

**Repository Adapter:**
- `ReturnShipmentRepositoryAdapter` → `logistics_return_shipment_seq`

---

## Presentation Layer

### Controller: `ReturnsController` — `/api/v1/logistics`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/returns` | — | `CreateReturnShipmentRequest` | `CreateReturnShipmentResponse` |
| `POST` | `/returns/{id}/receive` | — | — | `void` |
| `POST` | `/returns/{id}/reject` | — | `RejectReturnShipmentRequest` | `void` |
| `GET` | `/returns/{id}` | — | — | `ReturnShipmentResult` |
| `GET` | `/returns` | `logistics:shipments:read` | `?orgId&status&page&size` | `PageResult<ReturnShipmentResult>` |

---

## Distributed Architecture

### Outbox
- `ReturnShipmentReceivedEvent` — triggers Commerce to approve the return; must not be lost (stock restoration + refund depend on it)

---

## Complete File List

```
atlashub-logistics/src/main/java/com/atlashub/logistics/returns/
├── domain/
│   ├── entities/
│   │   └── ReturnShipment.java
│   ├── events/
│   │   ├── ReturnShipmentCreatedEvent.java
│   │   └── ReturnShipmentReceivedEvent.java
│   ├── exceptions/
│   │   ├── ReturnShipmentNotFoundException.java
│   │   └── InvalidReturnStateException.java
│   ├── repositories/
│   │   └── ReturnShipmentRepository.java
│   └── valueobject/
│       └── ReturnStatus.java
├── application/
│   ├── commands/
│   │   ├── CreateReturnShipment/
│   │   │   ├── CreateReturnShipmentCommand.java
│   │   │   ├── CreateReturnShipmentHandler.java
│   │   │   └── CreateReturnShipmentResponse.java
│   │   ├── ReceiveReturnShipment/
│   │   │   ├── ReceiveReturnShipmentCommand.java
│   │   │   └── ReceiveReturnShipmentHandler.java
│   │   └── RejectReturnShipment/
│   │       ├── RejectReturnShipmentCommand.java
│   │       └── RejectReturnShipmentHandler.java
│   └── queries/
│       ├── GetReturnShipment/
│       │   ├── GetReturnShipmentQuery.java
│       │   ├── GetReturnShipmentHandler.java
│       │   └── ReturnShipmentResult.java
│       └── ListReturnShipments/
│           ├── ListReturnShipmentsQuery.java
│           └── ListReturnShipmentsHandler.java
├── infrastructure/
│   └── persistence/
│       ├── adapters/
│       │   └── ReturnShipmentRepositoryAdapter.java
│       ├── entities/
│       │   └── ReturnShipmentJpaEntity.java
│       ├── mappers/
│       │   └── ReturnShipmentMapper.java
│       └── repositories/
│           └── SpringDataReturnShipmentRepository.java
└── presentation/
    ├── dto/
    │   ├── CreateReturnShipmentRequest.java
    │   ├── CreateReturnShipmentResponse.java
    │   ├── RejectReturnShipmentRequest.java
    │   └── ReturnShipmentResult.java
    └── rest/
        └── ReturnsController.java
```
