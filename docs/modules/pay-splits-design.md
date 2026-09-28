# Pay Splits Design (`atlashub-pay:splits`)

## Role & Purpose

The `splits` submodule manages **revenue split rules** that determine how incoming payments are distributed among multiple recipients. When a payment arrives via `pay:charges`, the associated split rule defines the exact distribution: AtlasHub's platform fee is deducted first, and the remainder is divided among the configured subaccounts (the business itself, marketplace vendors, partner organizations, or external bank accounts).

This submodule is **pure CRUD** from the user perspective — organizations configure split rules, and the `pay:charges` submodule consumes them synchronously at charge initialization time via the `SplitQueryPort`. There are no Kafka listeners in this submodule and no asynchronous workflows. Split rule lookups during charge processing are synchronous in-process calls.

---

## Domain Layer

### Aggregate Root: `SplitRule`

**Package**: `com.atlashub.pay.splits.domain.entities`

```
SplitRule
├── id: Long
├── organizationId: Long
├── name: String
├── type: SplitType                    ← PERCENTAGE | FLAT
├── platformFeePercentage: BigDecimal  ← AtlasHub's fee; always applied before subaccount splits
├── subaccounts: List<SplitSubaccount>
├── isActive: Boolean
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

**Invariants:**
- For `PERCENTAGE` rules: the sum of all `SplitSubaccount.share` values plus `platformFeePercentage` must equal 100. Validated at construction and on any `update()` call.
- A `SplitRule` must have at least one subaccount.
- A deactivated rule cannot be used in new charges — `pay:charges` checks `isActive` via `SplitQueryPort`.

**Business Methods:**

| Method | Inputs | Guard | Effect | Event Registered |
|---|---|---|---|---|
| `update(name, type, platformFee, subaccounts)` | Updated fields | Rule must be active; percentages must sum to 100 for PERCENTAGE type | Replaces name, type, platformFeePercentage, and subaccounts list | — |
| `deactivate()` | — | Rule must be active | Sets `isActive = false`, `updatedAt = now()` | — |
| `addSubaccount(subaccount)` | `SplitSubaccount` | Rule must be active; resulting percentages must still be valid | Appends to subaccounts | — |
| `removeSubaccount(subaccountId)` | `Long subaccountId` | Rule must be active; at least one subaccount must remain after removal | Removes the matching subaccount | — |

---

### Entity: `SplitSubaccount`

**Package**: `com.atlashub.pay.splits.domain.entities`

```
SplitSubaccount
├── id: Long
├── splitRuleId: Long
├── recipientType: RecipientType   ← ORGANIZATION | VENDOR | EXTERNAL_BANK_ACCOUNT
├── recipientId: String            ← orgId, vendorId, or bank account reference depending on recipientType
├── share: BigDecimal              ← percentage (for PERCENTAGE rules) or flat amount in minor currency units
└── description: String
```

---

### Value Objects

**Package**: `com.atlashub.pay.splits.domain.valueobject`

| Class | Fields | Description |
|---|---|---|
| `SplitType` | enum: `PERCENTAGE`, `FLAT` | Whether shares are expressed as percentages or fixed amounts |
| `RecipientType` | enum: `ORGANIZATION`, `VENDOR`, `EXTERNAL_BANK_ACCOUNT` | Who receives the split share |

---

### Domain Events

**Package**: `com.atlashub.pay.splits.domain.events`

This submodule does not publish Kafka events — it has no asynchronous side effects. All state changes are synchronous and self-contained.

---

### Domain Exceptions

**Package**: `com.atlashub.pay.splits.domain.exceptions`

```java
public class SplitRuleNotFoundException extends NotFoundException {
    public SplitRuleNotFoundException() { super("Split rule not found"); }
    public SplitRuleNotFoundException(String message) { super(message); }
}

public class InvalidSplitPercentagesException extends ValidationException {
    public InvalidSplitPercentagesException() {
        super("Split percentages including platform fee must sum to exactly 100%");
    }
    public InvalidSplitPercentagesException(String message) { super(message); }
}

public class SplitRuleInactiveException extends BusinessRuleException {
    public SplitRuleInactiveException() { super("Split rule is inactive and cannot be modified or used"); }
}
```

---

### Domain Ports

**Package**: `com.atlashub.pay.splits.domain.ports`

```java
/**
 * Read-only port exposed to the pay:charges submodule.
 * Allows charges to look up a split rule synchronously at charge initialization time.
 * Implemented by SplitQueryPortAdapter in infrastructure/services/.
 */
public interface SplitQueryPort {
    /**
     * Returns the active split rule for the given ID.
     * Throws SplitRuleNotFoundException if the rule does not exist.
     * Throws SplitRuleInactiveException if the rule exists but is inactive.
     */
    SplitRule findActiveSplitRule(Long splitRuleId);
}
```

---

### Domain Repository

**Package**: `com.atlashub.pay.splits.domain.repositories`

```java
public interface SplitRuleRepository {
    SplitRule save(SplitRule splitRule);
    Optional<SplitRule> findById(Long id);
    List<SplitRule> findAllByOrganizationId(Long organizationId);
    Optional<SplitRule> findByIdAndOrganizationId(Long id, Long organizationId);
}
```

---

## Application Layer

### Commands

#### `CreateSplitRuleCommand`

**Package**: `com.atlashub.pay.splits.application.commands.CreateSplitRule`

```java
public record CreateSplitRuleCommand(
    Long organizationId,
    String name,
    SplitType type,
    BigDecimal platformFeePercentage,
    List<SplitSubaccountItem> subaccounts
) {
    public record SplitSubaccountItem(
        RecipientType recipientType,
        String recipientId,
        BigDecimal share,
        String description
    ) {}
}
```

**Handler**: `CreateSplitRuleHandler extends Command<CreateSplitRuleCommand, CreateSplitRuleResponse>`

**RBAC**: `pay:splits:manage`

**Processing steps:**
1. If `type == PERCENTAGE`, validate that `platformFeePercentage + sum(subaccounts.share) == 100`. Throw `InvalidSplitPercentagesException` if not.
2. Construct `SplitRule` with `isActive = true` and the provided subaccounts.
3. Save via `SplitRuleRepository`.
4. Return `CreateSplitRuleResponse` with the new split rule ID.

**Response:**
```java
public record CreateSplitRuleResponse(
    Long splitRuleId
) {}
```

---

#### `UpdateSplitRuleCommand`

**Package**: `com.atlashub.pay.splits.application.commands.UpdateSplitRule`

```java
public record UpdateSplitRuleCommand(
    Long splitRuleId,
    Long organizationId,
    String name,
    SplitType type,
    BigDecimal platformFeePercentage,
    List<CreateSplitRuleCommand.SplitSubaccountItem> subaccounts
) {}
```

**Handler**: `UpdateSplitRuleHandler extends Command<UpdateSplitRuleCommand, Void>`

**RBAC**: `pay:splits:manage`

**Processing steps:**
1. Load `SplitRule` by `(splitRuleId, organizationId)`. Throw `SplitRuleNotFoundException` if not found.
2. Throw `SplitRuleInactiveException` if `!splitRule.isActive()`.
3. If `type == PERCENTAGE`, validate percentages sum to 100. Throw `InvalidSplitPercentagesException` if not.
4. Call `splitRule.update(name, type, platformFeePercentage, subaccounts)`.
5. Save via repository.

**Response**: `void`

---

#### `DeactivateSplitRuleCommand`

**Package**: `com.atlashub.pay.splits.application.commands.DeactivateSplitRule`

```java
public record DeactivateSplitRuleCommand(
    Long splitRuleId,
    Long organizationId
) {}
```

**Handler**: `DeactivateSplitRuleHandler extends Command<DeactivateSplitRuleCommand, Void>`

**RBAC**: `pay:splits:manage`

**Processing steps:**
1. Load `SplitRule` by `(splitRuleId, organizationId)`. Throw `SplitRuleNotFoundException` if not found.
2. Call `splitRule.deactivate()`. Throw `SplitRuleInactiveException` if already inactive.
3. Save via repository.

**Response**: `void`

---

#### `AddSplitSubaccountCommand`

**Package**: `com.atlashub.pay.splits.application.commands.AddSplitSubaccount`

```java
public record AddSplitSubaccountCommand(
    Long splitRuleId,
    Long organizationId,
    RecipientType recipientType,
    String recipientId,
    BigDecimal share,
    String description
) {}
```

**Handler**: `AddSplitSubaccountHandler extends Command<AddSplitSubaccountCommand, Void>`

**RBAC**: `pay:splits:manage`

**Processing steps:**
1. Load `SplitRule` by `(splitRuleId, organizationId)`. Throw `SplitRuleNotFoundException` if not found.
2. Throw `SplitRuleInactiveException` if rule is inactive.
3. Construct a `SplitSubaccount` from the command fields.
4. Call `splitRule.addSubaccount(subaccount)` — domain method re-validates percentages if type is PERCENTAGE.
5. Save via repository.

**Response**: `void`

---

#### `RemoveSplitSubaccountCommand`

**Package**: `com.atlashub.pay.splits.application.commands.RemoveSplitSubaccount`

```java
public record RemoveSplitSubaccountCommand(
    Long splitRuleId,
    Long organizationId,
    Long subaccountId
) {}
```

**Handler**: `RemoveSplitSubaccountHandler extends Command<RemoveSplitSubaccountCommand, Void>`

**RBAC**: `pay:splits:manage`

**Processing steps:**
1. Load `SplitRule` by `(splitRuleId, organizationId)`. Throw `SplitRuleNotFoundException` if not found.
2. Throw `SplitRuleInactiveException` if rule is inactive.
3. Call `splitRule.removeSubaccount(subaccountId)` — domain method validates at least one subaccount remains.
4. Save via repository.

**Response**: `void`

---

### Queries

#### `GetSplitRuleQuery`

**Package**: `com.atlashub.pay.splits.application.queries.GetSplitRule`

```java
public record GetSplitRuleQuery(
    Long splitRuleId,
    Long organizationId
) {}
```

**Handler**: `GetSplitRuleHandler extends Query<GetSplitRuleQuery, SplitRuleResult>`

**Result:**
```java
public record SplitRuleResult(
    Long id,
    Long organizationId,
    String name,
    String type,
    BigDecimal platformFeePercentage,
    List<SplitSubaccountResult> subaccounts,
    boolean isActive,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
    public record SplitSubaccountResult(
        Long id,
        String recipientType,
        String recipientId,
        BigDecimal share,
        String description
    ) {}
}
```

---

#### `ListSplitRulesQuery`

**Package**: `com.atlashub.pay.splits.application.queries.ListSplitRules`

```java
public record ListSplitRulesQuery(
    Long organizationId
) {}
```

**Handler**: `ListSplitRulesHandler extends Query<ListSplitRulesQuery, List<SplitRuleResult>>`

**Returns**: `List<SplitRuleResult>` — organizations have a bounded number of split rules (typically fewer than 100); `PageResult` is not warranted.

---

## Infrastructure Layer

### Persistence

**JPA Entity**: `SplitRuleJpa`
**Package**: `com.atlashub.pay.splits.infrastructure.persistence.entities`

```java
@Entity
@Table(name = "split_rules")
public class SplitRuleJpa {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "split_rule_seq")
    @SequenceGenerator(name = "split_rule_seq", sequenceName = "split_rule_id_seq", allocationSize = 1)
    Long id;
    Long organizationId;
    String name;
    @Enumerated(EnumType.STRING) SplitType type;
    BigDecimal platformFeePercentage;
    boolean isActive;
    ZonedDateTime createdAt;
    ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "splitRuleId", cascade = CascadeType.ALL, orphanRemoval = true)
    List<SplitSubaccountJpa> subaccounts;

    @Version long version;  // optimistic locking
}
```

**JPA Entity**: `SplitSubaccountJpa`
**Package**: `com.atlashub.pay.splits.infrastructure.persistence.entities`

```java
@Entity
@Table(name = "split_subaccounts")
public class SplitSubaccountJpa {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "split_subaccount_seq")
    @SequenceGenerator(name = "split_subaccount_seq", sequenceName = "split_subaccount_id_seq", allocationSize = 1)
    Long id;
    Long splitRuleId;
    @Enumerated(EnumType.STRING) RecipientType recipientType;
    String recipientId;
    BigDecimal share;
    String description;
}
```

**Spring Data Repository**: `SpringDataSplitRuleRepository`
**Package**: `com.atlashub.pay.splits.infrastructure.persistence.repositories`

```java
public interface SpringDataSplitRuleRepository extends JpaRepository<SplitRuleJpa, Long> {
    List<SplitRuleJpa> findAllByOrganizationId(Long organizationId);
    Optional<SplitRuleJpa> findByIdAndOrganizationId(Long id, Long organizationId);
}
```

**Mapper**: `SplitRuleMapper`
**Package**: `com.atlashub.pay.splits.infrastructure.persistence.mappers`

**Adapter**: `SplitRuleRepositoryAdapter implements SplitRuleRepository`
**Package**: `com.atlashub.pay.splits.infrastructure.persistence.adapters`

---

### Infrastructure Service Adapter

#### `SplitQueryPortAdapter implements SplitQueryPort`

**Package**: `com.atlashub.pay.splits.infrastructure.services`

Implements the `SplitQueryPort` domain port. Used in-process by `pay:charges` to fetch a split rule synchronously during charge initialization.

```java
@Component
public class SplitQueryPortAdapter implements SplitQueryPort {

    @Override
    public SplitRule findActiveSplitRule(Long splitRuleId) {
        SplitRule rule = splitRuleRepository.findById(splitRuleId)
            .orElseThrow(SplitRuleNotFoundException::new);
        if (!rule.isActive()) throw new SplitRuleInactiveException();
        return rule;
    }
}
```

---

## Presentation Layer

### Controller: `SplitRuleController`

**Package**: `com.atlashub.pay.splits.presentation.rest`

```java
@RestController
@RequestMapping("/api/v1/pay/splits")
@Tag(name = "Split Rules")
public class SplitRuleController { ... }
```

| Method | Path | Auth | RBAC | Request DTO | Response DTO |
|---|---|---|---|---|---|
| `POST` | `/api/v1/pay/splits` | Bearer JWT | `pay:splits:manage` | `CreateSplitRuleRequest` | `ApiResponse<CreateSplitRuleResponse>` |
| `PUT` | `/api/v1/pay/splits/{id}` | Bearer JWT | `pay:splits:manage` | `UpdateSplitRuleRequest` | `ApiResponse<Void>` |
| `DELETE` | `/api/v1/pay/splits/{id}` | Bearer JWT | `pay:splits:manage` | — | `ApiResponse<Void>` |
| `POST` | `/api/v1/pay/splits/{id}/subaccounts` | Bearer JWT | `pay:splits:manage` | `AddSplitSubaccountRequest` | `ApiResponse<Void>` |
| `DELETE` | `/api/v1/pay/splits/{id}/subaccounts/{subaccountId}` | Bearer JWT | `pay:splits:manage` | — | `ApiResponse<Void>` |
| `GET` | `/api/v1/pay/splits` | Bearer JWT | `pay:splits:read` | — | `ApiResponse<List<SplitRuleResponse>>` |
| `GET` | `/api/v1/pay/splits/{id}` | Bearer JWT | `pay:splits:read` | — | `ApiResponse<SplitRuleResponse>` |

### DTOs

**Package**: `com.atlashub.pay.splits.presentation.dto`

**`CreateSplitRuleRequest`**
```java
public record CreateSplitRuleRequest(
    @NotBlank String name,
    @NotNull SplitType type,
    @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal platformFeePercentage,
    @NotEmpty List<SplitSubaccountRequest> subaccounts
) {}
```

**`SplitSubaccountRequest`**
```java
public record SplitSubaccountRequest(
    @NotNull RecipientType recipientType,
    @NotBlank String recipientId,
    @NotNull @DecimalMin("0") BigDecimal share,
    String description
) {}
```

**`UpdateSplitRuleRequest`**
```java
public record UpdateSplitRuleRequest(
    @NotBlank String name,
    @NotNull SplitType type,
    @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal platformFeePercentage,
    @NotEmpty List<SplitSubaccountRequest> subaccounts
) {}
```

**`AddSplitSubaccountRequest`**
```java
public record AddSplitSubaccountRequest(
    @NotNull RecipientType recipientType,
    @NotBlank String recipientId,
    @NotNull @DecimalMin("0") BigDecimal share,
    String description
) {}
```

**`SplitRuleResponse`**
```java
public record SplitRuleResponse(
    Long id,
    Long organizationId,
    String name,
    String type,
    BigDecimal platformFeePercentage,
    List<SplitSubaccountResponse> subaccounts,
    boolean isActive,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
    public record SplitSubaccountResponse(
        Long id,
        String recipientType,
        String recipientId,
        BigDecimal share,
        String description
    ) {}
}
```

---

## RBAC Table

| Permission | Granted To | Operation |
|---|---|---|
| `pay:splits:manage` | `OWNER`, `ADMIN`, `FINANCE` | Create, update, deactivate split rules; add/remove subaccounts |
| `pay:splits:read` | `OWNER`, `ADMIN`, `FINANCE`, `DEVELOPER` | List and view split rules |

---

## Maker-Checker

Not applicable for this submodule. Split rule configuration is a standard single-user operation. No dual authorization is required.

---

## Socket Events

This submodule does not publish any events that trigger WebSocket push. Split rule changes are configuration operations with no real-time notification requirement.

---

## Domain Events Table

This submodule publishes no domain events to Kafka. All operations are synchronous, self-contained CRUD actions.

---

## Distributed Architecture

### Locking
- **Optimistic locking** (`@Version`) on `SplitRuleJpa` — sufficient because split rule configuration is low-frequency and contention is minimal.

### Idempotency
- No client-supplied idempotency key needed — create and update operations are standard REST semantics. The unique constraint on `(organizationId, name)` prevents duplicate rule names per org.

### Outbox
- No outbox entries. This submodule publishes no domain events.

---

## Complete File List

```
com.atlashub.pay.splits/
├── application/
│   ├── commands/
│   │   ├── AddSplitSubaccount/
│   │   │   ├── AddSplitSubaccountCommand.java
│   │   │   └── AddSplitSubaccountHandler.java
│   │   ├── CreateSplitRule/
│   │   │   ├── CreateSplitRuleCommand.java
│   │   │   ├── CreateSplitRuleHandler.java
│   │   │   └── CreateSplitRuleResponse.java
│   │   ├── DeactivateSplitRule/
│   │   │   ├── DeactivateSplitRuleCommand.java
│   │   │   └── DeactivateSplitRuleHandler.java
│   │   ├── RemoveSplitSubaccount/
│   │   │   ├── RemoveSplitSubaccountCommand.java
│   │   │   └── RemoveSplitSubaccountHandler.java
│   │   └── UpdateSplitRule/
│   │       ├── UpdateSplitRuleCommand.java
│   │       └── UpdateSplitRuleHandler.java
│   └── queries/
│       ├── GetSplitRule/
│       │   ├── GetSplitRuleQuery.java
│       │   ├── GetSplitRuleHandler.java
│       │   └── SplitRuleResult.java
│       └── ListSplitRules/
│           ├── ListSplitRulesQuery.java
│           └── ListSplitRulesHandler.java
├── domain/
│   ├── entities/
│   │   ├── SplitRule.java
│   │   └── SplitSubaccount.java
│   ├── exceptions/
│   │   ├── InvalidSplitPercentagesException.java
│   │   ├── SplitRuleInactiveException.java
│   │   └── SplitRuleNotFoundException.java
│   ├── ports/
│   │   └── SplitQueryPort.java
│   ├── repositories/
│   │   └── SplitRuleRepository.java
│   └── valueobject/
│       ├── RecipientType.java
│       └── SplitType.java
├── infrastructure/
│   ├── persistence/
│   │   ├── adapters/
│   │   │   └── SplitRuleRepositoryAdapter.java
│   │   ├── entities/
│   │   │   ├── SplitRuleJpa.java
│   │   │   └── SplitSubaccountJpa.java
│   │   ├── mappers/
│   │   │   └── SplitRuleMapper.java
│   │   └── repositories/
│   │       └── SpringDataSplitRuleRepository.java
│   └── services/
│       └── SplitQueryPortAdapter.java
└── presentation/
    ├── dto/
    │   ├── AddSplitSubaccountRequest.java
    │   ├── CreateSplitRuleRequest.java
    │   ├── SplitRuleResponse.java
    │   ├── SplitSubaccountRequest.java
    │   └── UpdateSplitRuleRequest.java
    └── rest/
        └── SplitRuleController.java
```
