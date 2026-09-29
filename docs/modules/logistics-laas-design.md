# Logistics LaaS Design (`atlashub-logistics` / `com.atlashub.logistics.laas`)

## Role & Purpose

The `laas` (Logistics-as-a-Service) subpackage supports organizations that **operate as logistics companies**, offering delivery services to external shipper clients. A shipper client is a business that submits delivery requests via API. Pricing rules (flat, zone-based, or weight-based) are configured per client or org.

Gradle module: `atlashub-logistics`  
Package: `com.atlashub.logistics.laas`

---

## Domain Layer

### `ShipperClient` (Aggregate Root)

**Package:** `com.atlashub.logistics.laas.domain.entities`

```
ShipperClient
├── id                   : Long
├── logisticsOrgId       : Long        ← the logistics company (organization)
├── clientName           : String
├── contactEmail         : EmailAddress
├── contactPhone         : PhoneNumber
├── apiPublicKey         : String      ← for programmatic API access (HMAC auth)
├── status               : ClientStatus ← ACTIVE | SUSPENDED | TERMINATED
├── defaultPricingRuleId : Long
└── createdAt            : ZonedDateTime
```

**Business methods (on entity):**
- `suspend(reason)` → ACTIVE → SUSPENDED; throws `InvalidClientStateException` if not ACTIVE
- `terminate()` → any → TERMINATED
- `reactivate()` → SUSPENDED → ACTIVE

---

### `PricingRule` (Aggregate Root)

```
PricingRule
├── id             : Long
├── organizationId : Long
├── name           : String
├── type           : PricingType  ← FLAT | ZONE_BASED | WEIGHT_BASED
├── currency       : String
├── config         : PricingConfig  ← polymorphic value object (see below)
└── isActive       : Boolean
```

**Value Objects (Pricing Configs):**

`FlatPricingConfig`: `flatFee: Money`

`ZoneBasedPricingConfig`: `List<ZoneRate>` where `ZoneRate` has `originZone: String`, `destinationZone: String`, `baseFee: Money`, `additionalKgFee: Money`

`WeightBasedPricingConfig`: `List<WeightBand>` where `WeightBand` has `minWeightKg: BigDecimal`, `maxWeightKg: BigDecimal`, `fee: Money`

**Business methods:** `deactivate()`, `activate()`

---

### Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `ShipperClientRegisteredEvent` | New shipper client registered | `notifications` (welcome email with API key) |
| `ShipperClientSuspendedEvent` | Client suspended | `notifications` |

---

### Domain Exceptions — `com.atlashub.logistics.laas.domain.exceptions`

```java
public class ShipperClientNotFoundException extends NotFoundException {
    public ShipperClientNotFoundException(Long id) { super("Shipper client not found: " + id); }
}
public class InvalidClientStateException extends BusinessRuleException {
    public InvalidClientStateException(String message) { super(message); }
}
public class PricingRuleNotFoundException extends NotFoundException {
    public PricingRuleNotFoundException(Long id) { super("Pricing rule not found: " + id); }
}
public class InvalidPricingConfigException extends ValidationException {
    public InvalidPricingConfigException(String message) { super(message); }
}
```

---

## Application Layer

### Commands — `com.atlashub.logistics.laas.application.commands`

#### `RegisterShipperClientCommand`
```java
record RegisterShipperClientCommand(
    Long logisticsOrgId, String clientName, String contactEmail,
    String contactPhone, Long defaultPricingRuleId
)
```
**Handler:** `RegisterShipperClientHandler` | **Response:** `RegisterShipperClientResponse(Long clientId, String apiPublicKey)`  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:shipments:create')")`  
**Flow:** Generate `apiPublicKey` → create `ShipperClient` → `repository.save()` → `ShipperClientRegisteredEvent` published

---

#### `CreatePricingRuleCommand`
```java
record CreatePricingRuleCommand(Long organizationId, String name, PricingType type, PricingConfig config)
```
**Handler:** `CreatePricingRuleHandler` | **Response:** `Long pricingRuleId`  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:shipments:create')")`  
**Flow:** Validate `config` matches `type` → `InvalidPricingConfigException`; create `PricingRule` → save

---

#### `SuspendShipperClientCommand`
```java
record SuspendShipperClientCommand(Long clientId, String reason)
```
**Handler:** `SuspendShipperClientHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('logistics:shipments:create')")`

---

### Queries — `com.atlashub.logistics.laas.application.queries`

#### `ListShipperClientsQuery`
```java
record ListShipperClientsQuery(Long organizationId, ClientStatus status)
```
**Handler:** `ListShipperClientsHandler` | **Result:** `List<ShipperClientResult>` (bounded per logistics org)

---

#### `CalculateDeliveryFeeQuery`
```java
record CalculateDeliveryFeeQuery(
    Long organizationId, Long pricingRuleId, BigDecimal weightKg,
    String originZone, String destinationZone
)
```
**Handler:** `CalculateDeliveryFeeHandler` | **Result:** `DeliveryFeeResult(Money fee, String pricingRuleName)`  
**Justification:** Single result — fee calculation for a single delivery request.

---

## Infrastructure Layer

### Persistence

| JPA Entity | Table | Locking |
|---|---|---|
| `ShipperClientJpaEntity` | `logistics_shipper_clients` | `@Version` optimistic |
| `PricingRuleJpaEntity` | `logistics_pricing_rules` | — |

**Spring Data:**
```
SpringDataShipperClientRepository
  + findByLogisticsOrgId(Long orgId): List<ShipperClientJpaEntity>
  + findByApiPublicKey(String key): Optional<ShipperClientJpaEntity>

SpringDataPricingRuleRepository
  + findByOrganizationIdAndIsActiveTrue(Long orgId): List<PricingRuleJpaEntity>
```

**Repository Adapters:**
- `ShipperClientRepositoryAdapter` → `logistics_shipper_client_seq`
- `PricingRuleRepositoryAdapter` → `logistics_pricing_rule_seq`

---

## Presentation Layer

### Controller: `LaasController` — `/api/v1/logistics/laas`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/laas/clients` | `logistics:shipments:create` | `RegisterShipperClientRequest` | `RegisterShipperClientResponse` |
| `POST` | `/laas/clients/{id}/suspend` | `logistics:shipments:create` | `SuspendShipperClientRequest` | `void` |
| `GET` | `/laas/clients` | `logistics:shipments:create` | `?orgId&status` | `List<ShipperClientResult>` |
| `POST` | `/laas/pricing-rules` | `logistics:shipments:create` | `CreatePricingRuleRequest` | `Long` |
| `GET` | `/laas/delivery-fee` | — | `?orgId&pricingRuleId&weightKg&originZone&destZone` | `DeliveryFeeResult` |

---

## Complete File List

```
atlashub-logistics/src/main/java/com/atlashub/logistics/laas/
├── domain/
│   ├── entities/ [ShipperClient, PricingRule]
│   ├── events/ [ShipperClientRegisteredEvent, ShipperClientSuspendedEvent]
│   ├── exceptions/ [ShipperClientNotFoundException, InvalidClientStateException, PricingRuleNotFoundException, InvalidPricingConfigException]
│   ├── repositories/ [ShipperClientRepository, PricingRuleRepository]
│   └── valueobject/ [ClientStatus, PricingType, PricingConfig, FlatPricingConfig, ZoneBasedPricingConfig, WeightBasedPricingConfig, ZoneRate, WeightBand]
├── application/
│   ├── commands/ [RegisterShipperClient, CreatePricingRule, SuspendShipperClient]
│   └── queries/ [ListShipperClients, CalculateDeliveryFee]
├── infrastructure/
│   └── persistence/ [adapters, entities, mappers, repositories]
└── presentation/
    ├── dto/ [RegisterShipperClientRequest, RegisterShipperClientResponse, SuspendShipperClientRequest, CreatePricingRuleRequest, ShipperClientResult, DeliveryFeeResult]
    └── rest/
        └── LaasController.java
```
