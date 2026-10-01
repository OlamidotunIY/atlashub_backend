# Pay Mandates Design (`atlashub-pay:mandates`)

## Role & Purpose

The `mandates` submodule manages **recurring payment authorizations from end-customers of organizations**. An organization using AtlasHub (e.g., a gym, a SaaS platform, a subscription box service) can create mandates on behalf of its customers. Each mandate stores a Paystack card authorization code that allows the platform to automatically charge the customer on a recurring schedule.

> [!IMPORTANT] This submodule manages B2B2C recurring charges — an organization charging its customers. It has no relation to billing subscriptions for the organization paying AtlasHub (that is managed by `atlashub-billing`).

The `MandateChargeScheduler` runs daily at 9 AM, finds all `ACTIVE` mandates whose `nextChargeDate` is today, and triggers a charge via `pay:charges` for each. After each successful or failed charge, the mandate's `nextChargeDate` is advanced to the next interval.

---

## Domain Layer

### Aggregate Root: `PaymentMandate`

**Package**: `com.atlashub.pay.mandates.domain.entities`

```
PaymentMandate
├── id: Long
├── organizationId: Long
├── customerId: String             ← external customer reference from the organization
├── email: EmailAddress            ← customer email (value object wrapping String)
├── amount: Money
├── frequency: MandateFrequency    ← DAILY | WEEKLY | MONTHLY | QUARTERLY | ANNUALLY
├── authorizationCode: String      ← Paystack's tokenized card authorization; stored securely
├── status: MandateStatus          ← ACTIVE | PAUSED | REVOKED | EXPIRED
├── nextChargeDate: LocalDate      ← date of next scheduled charge
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime       ← nullable; set on any status change
```

**State Machine:**
```
ACTIVE ──pause()──► PAUSED ──resume()──► ACTIVE
  │
  └──revoke()──► REVOKED  (terminal)
  │
  └──[nextChargeDate passes without valid card]──► EXPIRED  (terminal)
```

**Business Methods:**

| Method | Inputs | Guard | Effect | Event Registered |
|---|---|---|---|---|
| `pause()` | — | status must be `ACTIVE` | sets `status = PAUSED`, `updatedAt = now()` | `MandatePausedEvent` |
| `resume()` | — | status must be `PAUSED` | sets `status = ACTIVE`, `updatedAt = now()` | `MandateResumedEvent` |
| `revoke()` | — | status must be `ACTIVE` or `PAUSED` | sets `status = REVOKED`, `updatedAt = now()` | `MandateRevokedEvent` |
| `advanceNextChargeDate(frequency)` | `MandateFrequency frequency` | status must be `ACTIVE` | calculates and sets `nextChargeDate` to next interval based on frequency | — |
| `markExpired()` | — | status must be `ACTIVE` | sets `status = EXPIRED`, `updatedAt = now()` | `MandateExpiredEvent` |

**Exceptions thrown inside business methods:**

| Exception | When |
|---|---|
| `MandatePausedException` | `pause()` called on non-`ACTIVE` mandate |
| `MandateRevokedException` | Any operation attempted on a `REVOKED` mandate |
| `ExpiredMandateException` | Any operation attempted on an `EXPIRED` mandate |

---

### Value Objects

**Package**: `com.atlashub.pay.mandates.domain.valueobject`

| Class | Fields | Description |
|---|---|---|
| `MandateFrequency` | enum: `DAILY`, `WEEKLY`, `MONTHLY`, `QUARTERLY`, `ANNUALLY` | Charge cadence; drives `advanceNextChargeDate()` calculation |
| `MandateStatus` | enum: `ACTIVE`, `PAUSED`, `REVOKED`, `EXPIRED` | Full lifecycle |
| `EmailAddress` | `String value` (validated against email regex) | Type-safe email wrapper; validates format at construction |

**`advanceNextChargeDate()` interval mapping:**

| Frequency | Next Date Calculation |
|---|---|
| `DAILY` | `nextChargeDate.plusDays(1)` |
| `WEEKLY` | `nextChargeDate.plusWeeks(1)` |
| `MONTHLY` | `nextChargeDate.plusMonths(1)` |
| `QUARTERLY` | `nextChargeDate.plusMonths(3)` |
| `ANNUALLY` | `nextChargeDate.plusYears(1)` |

---

### Domain Events

**Package**: `com.atlashub.pay.mandates.domain.events`

All events are published to Kafka topic **`pay-events`**.

| Event | Published When | Consumed By |
|---|---|---|
| `MandateChargeDueEvent` | `MandateChargeScheduler` identifies a mandate due for charge | `pay:charges` (listens → initiates charge using `authorizationCode`) |
| `MandatePausedEvent` | Customer-initiated pause | `notifications`, `pay:webhooks` |
| `MandateResumedEvent` | Customer or org resumes paused mandate | `notifications`, `pay:webhooks` |
| `MandateRevokedEvent` | Mandate permanently revoked | `notifications`, `pay:webhooks` |
| `MandateExpiredEvent` | Card expired / repeated failures | `notifications`, `pay:webhooks` |
| `MandateChargedEvent` | Charge confirmed successful | `notifications` (receipt to customer), `pay:webhooks`, `pay:tx-query` |

**Event payload shapes:**

```
MandateChargeDueEvent.payload
├── mandateId          : Long
├── organizationId     : Long
├── customerId         : String
├── email              : String
├── chargeReference    : String       ← UUID; used by pay:charges as idempotency key
├── amount             : BigDecimal
├── currency           : String
├── authorizationCode  : String       ← Paystack card auth code
├── paymentChannel     : String       ← CARD (always for mandates)
├── provider           : String       ← PAYSTACK
├── sourceReferenceId  : String       ← mandateId as string
└── dueAt              : ZonedDateTime

MandateRevokedEvent.payload
├── mandateId      : Long
├── organizationId : Long
├── customerId     : String
├── email          : String
├── amount         : BigDecimal
├── currency       : String
└── frequency      : String

MandateChargedEvent.payload
├── mandateId      : Long
├── organizationId : Long
├── customerId     : String
├── email          : String
├── amount         : BigDecimal
├── currency       : String
├── chargeReference: String
└── chargedAt      : ZonedDateTime
```

---

### Domain Exceptions

**Package**: `com.atlashub.pay.mandates.domain.exceptions`

```java
public class MandateNotFoundException extends NotFoundException {
    public MandateNotFoundException() { super("Payment mandate not found"); }
    public MandateNotFoundException(String message) { super(message); }
}

public class MandateRevokedException extends BusinessRuleException {
    public MandateRevokedException() { super("Payment mandate has been revoked"); }
}

public class MandatePausedException extends BusinessRuleException {
    public MandatePausedException() { super("Payment mandate is currently paused"); }
}

public class ExpiredMandateException extends BusinessRuleException {
    public ExpiredMandateException() { super("Payment mandate has expired"); }
}
```

---

### Domain Repository

**Package**: `com.atlashub.pay.mandates.domain.repositories`

```java
public interface PaymentMandateRepository {
    PaymentMandate save(PaymentMandate mandate);
    Optional<PaymentMandate> findById(Long id);
    Optional<PaymentMandate> findByIdAndOrganizationId(Long id, Long organizationId);
    List<PaymentMandate> findByOrganizationIdAndCustomerId(Long organizationId, String customerId);
    List<PaymentMandate> findByOrganizationIdAndStatus(Long organizationId, MandateStatus status);
    List<PaymentMandate> findByOrganizationIdAndCustomerIdAndStatus(Long organizationId, String customerId, MandateStatus status);
    /**
     * Used by MandateChargeScheduler: returns all ACTIVE mandates where nextChargeDate <= today.
     */
    List<PaymentMandate> findActiveMandatesDueForCharge(LocalDate today);
}
```

---

## Application Layer

### Commands

#### `CreateMandateCommand`

**Package**: `com.atlashub.pay.mandates.application.commands.CreateMandate`

```java
public record CreateMandateCommand(
    Long organizationId,
    String customerId,
    String email,
    BigDecimal amount,
    String currency,
    MandateFrequency frequency,
    String authorizationCode,
    LocalDate firstChargeDate
) {}
```

**Handler**: `CreateMandateHandler extends Command<CreateMandateCommand, CreateMandateResponse>`

**RBAC**: `pay:mandates:create`

**Processing steps:**
1. Validate `email` format and construct `EmailAddress` value object.
2. Construct `PaymentMandate` with `status = ACTIVE`, `nextChargeDate = firstChargeDate`.
3. Save via `PaymentMandateRepository`.
4. Return `CreateMandateResponse` with mandate ID.

**Response:**
```java
public record CreateMandateResponse(
    Long mandateId,
    String status,
    LocalDate nextChargeDate
) {}
```

---

#### `PauseMandateCommand`

**Package**: `com.atlashub.pay.mandates.application.commands.PauseMandate`

```java
public record PauseMandateCommand(
    Long mandateId,
    Long organizationId
) {}
```

**Handler**: `PauseMandateHandler extends Command<PauseMandateCommand, Void>`

**RBAC**: `pay:mandates:create` (org-level mandate management permission)

**Processing steps:**
1. Load `PaymentMandate` by `(mandateId, organizationId)`. Throw `MandateNotFoundException` if not found.
2. Call `mandate.pause()` — throws `MandateRevokedException` or `ExpiredMandateException` if terminal, `MandatePausedException` if already paused.
3. Save and publish `MandatePausedEvent` via outbox.

**Response**: `void`

---

#### `ResumeMandateCommand`

**Package**: `com.atlashub.pay.mandates.application.commands.ResumeMandate`

```java
public record ResumeMandateCommand(
    Long mandateId,
    Long organizationId
) {}
```

**Handler**: `ResumeMandateHandler extends Command<ResumeMandateCommand, Void>`

**RBAC**: `pay:mandates:create`

**Processing steps:**
1. Load `PaymentMandate` by `(mandateId, organizationId)`. Throw `MandateNotFoundException` if not found.
2. Call `mandate.resume()`.
3. Save and publish `MandateResumedEvent` via outbox.

**Response**: `void`

---

#### `RevokeMandateCommand`

**Package**: `com.atlashub.pay.mandates.application.commands.RevokeMandate`

```java
public record RevokeMandateCommand(
    Long mandateId,
    Long organizationId
) {}
```

**Handler**: `RevokeMandateHandler extends Command<RevokeMandateCommand, Void>`

**RBAC**: `pay:mandates:revoke`

**Processing steps:**
1. Load `PaymentMandate` by `(mandateId, organizationId)`. Throw `MandateNotFoundException` if not found.
2. Call `mandate.revoke()`.
3. Save and publish `MandateRevokedEvent` via outbox.

**Response**: `void`

---

#### `ChargeMandateCommand`

**Package**: `com.atlashub.pay.mandates.application.commands.ChargeMandate`

```java
public record ChargeMandateCommand(
    Long mandateId
) {}
```

**Handler**: `ChargeMandateHandler extends Command<ChargeMandateCommand, Void>`

**RBAC**: System-internal only (triggered by `MandateChargeScheduler` — no HTTP endpoint).

**Processing steps:**
1. Load `PaymentMandate` by ID. Throw `MandateNotFoundException` if not found.
2. Verify `mandate.status == ACTIVE`. If `PAUSED`, `REVOKED`, or `EXPIRED`, log and skip.
3. Generate a unique `chargeReference` (UUID prefixed with `mandate-{mandateId}-`).
4. Publish `MandateChargeDueEvent` to Kafka. `pay:charges` listens and initiates the actual charge using `mandate.authorizationCode`. **No sync cross-module write call.**
5. `mandate.advanceNextChargeDate(mandate.frequency)` — pre-advance the date to prevent double-charging if scheduler runs twice in the same day.
6. Save updated mandate.

> **`ChargePort` was a synchronous cross-module write port and has been removed.** Charge outcome (`ChargeSuccessfulEvent` / `ChargeFailedEvent`) is received by `ChargeSuccessfulListener` / `ChargeFailedListener` in this module.

**Response**: `void`

---

#### `AdvanceMandateDateCommand`

**Package**: `com.atlashub.pay.mandates.application.commands.AdvanceMandateDate`

```java
public record AdvanceMandateDateCommand(Long mandateId)
```

**Handler**: `AdvanceMandateDateHandler extends Command<AdvanceMandateDateCommand, Void>`

**RBAC**: System-internal only (triggered by `ChargeSuccessfulListener`).

**Processing steps:**
1. Load `PaymentMandate`. Confirm `status == ACTIVE`.
2. Publish `MandateChargedEvent`.

---

### Queries

#### `ListMandatesQuery`

**Package**: `com.atlashub.pay.mandates.application.queries.ListMandates`

```java
public record ListMandatesQuery(
    Long organizationId,
    String customerId,      // nullable filter
    MandateStatus status    // nullable filter
) {}
```

**Handler**: `ListMandatesHandler extends Query<ListMandatesQuery, List<MandateResult>>`

**Returns**: `List<MandateResult>` — bounded by organization and optionally customer; not a user-facing pageable list requiring `PageResult`.

---

#### `GetMandateQuery`

**Package**: `com.atlashub.pay.mandates.application.queries.GetMandate`

```java
public record GetMandateQuery(
    Long mandateId,
    Long organizationId
) {}
```

**Handler**: `GetMandateHandler extends Query<GetMandateQuery, MandateResult>`

**Result:**
```java
public record MandateResult(
    Long id,
    Long organizationId,
    String customerId,
    String email,
    BigDecimal amount,
    String currency,
    String frequency,
    String status,
    LocalDate nextChargeDate,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {}
```

---

## Infrastructure Layer

### Persistence

**JPA Entity**: `PaymentMandateJpa`
**Package**: `com.atlashub.pay.mandates.infrastructure.persistence.entities`

```java
@Entity
@Table(name = "payment_mandates")
public class PaymentMandateJpa {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mandate_seq")
    @SequenceGenerator(name = "mandate_seq", sequenceName = "mandate_id_seq", allocationSize = 1)
    Long id;
    Long organizationId;
    String customerId;
    String email;
    BigDecimal amount;
    String currency;
    @Enumerated(EnumType.STRING) MandateFrequency frequency;
    String authorizationCode;     // stored encrypted at-rest via column encryption
    @Enumerated(EnumType.STRING) MandateStatus status;
    LocalDate nextChargeDate;
    ZonedDateTime createdAt;
    ZonedDateTime updatedAt;

    @Version long version;   // optimistic locking
}
```

**Spring Data Repository**: `SpringDataPaymentMandateRepository`
**Package**: `com.atlashub.pay.mandates.infrastructure.persistence.repositories`

```java
public interface SpringDataPaymentMandateRepository extends JpaRepository<PaymentMandateJpa, Long> {
    Optional<PaymentMandateJpa> findByIdAndOrganizationId(Long id, Long organizationId);
    List<PaymentMandateJpa> findByOrganizationIdAndCustomerId(Long organizationId, String customerId);
    List<PaymentMandateJpa> findByOrganizationIdAndStatus(Long organizationId, MandateStatus status);
    List<PaymentMandateJpa> findByOrganizationIdAndCustomerIdAndStatus(Long organizationId, String customerId, MandateStatus status);

    @Query("SELECT m FROM PaymentMandateJpa m WHERE m.status = 'ACTIVE' AND m.nextChargeDate <= :today")
    List<PaymentMandateJpa> findActiveMandatesDueForCharge(@Param("today") LocalDate today);
}
```

**Mapper**: `PaymentMandateMapper`
**Package**: `com.atlashub.pay.mandates.infrastructure.persistence.mappers`

**Adapter**: `PaymentMandateRepositoryAdapter implements PaymentMandateRepository`
**Package**: `com.atlashub.pay.mandates.infrastructure.persistence.adapters`

---

### Scheduler

#### `MandateChargeScheduler`

**Package**: `com.atlashub.pay.mandates.infrastructure.messaging`

```java
@Component
public class MandateChargeScheduler {

    @Scheduled(cron = "0 0 9 * * *")
    public void chargeDueMandates() {
        List<PaymentMandate> dueMandates = mandateRepository.findActiveMandatesDueForCharge(LocalDate.now());
        for (PaymentMandate mandate : dueMandates) {
            try {
                chargeMandateHandler.execute(new ChargeMandateCommand(mandate.getId()));
            } catch (Exception e) {
                log.error("Failed to charge mandate {}: {}", mandate.getId(), e.getMessage());
                // Continue processing remaining mandates — one failure does not abort the batch
            }
        }
    }
}
```

**Cron**: `0 0 9 * * *` — daily at 09:00 server time
**Command Called**: `ChargeMandateCommand` → `ChargeMandateHandler`
**Behavior**: Processes each mandate independently; failures are logged and do not abort the batch.

---

### Kafka Listeners — `infrastructure/messaging/listeners/`

#### `MandateChargeSuccessfulListener`

| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `pay-mandates-charge-successful` |
| **Event** | `ChargeSuccessfulEvent` |
| **Filter** | Only processes events where `sourceSystem == "MANDATE_DEBIT"` |
| **Payload fields** | `chargeId`, `organizationId`, `reference` (= chargeReference), `sourceSystem`, `sourceReferenceId` (= mandateId), `succeededAt` |
| **Command called** | `AdvanceMandateDateHandler` |
| **Flow** | Extracts `mandateId` from `sourceReferenceId`. Loads mandate. Publishes `MandateChargedEvent`. Idempotent: checks if `nextChargeDate` already advanced for this reference. |

---

#### `MandateChargeFailedListener`

| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `pay-mandates-charge-failed` |
| **Event** | `ChargeFailedEvent` |
| **Filter** | Only processes events where `sourceSystem == "MANDATE_DEBIT"` |
| **Payload fields** | `chargeId`, `organizationId`, `reference`, `sourceSystem`, `sourceReferenceId` (= mandateId), `failureReason`, `failedAt` |
| **Command called** | `ExpireMandateHandler` (if failure is card-level) or logs and skips (if transient failure) |
| **Flow** | Extracts `mandateId` from `sourceReferenceId`. If `failureReason` indicates expired/invalid card → calls `mandate.markExpired()` → publishes `MandateExpiredEvent`. Otherwise logs for retry on next scheduled day. |

---

#### `EmployeeTerminatedListener`

| Attribute | Value |
|---|---|
| **Topic** | `hr-events` |
| **Group ID** | `pay-mandates-employee-terminated` |
| **Event** | `EmployeeTerminatedEvent` |
| **Payload fields** | `employeeId`, `organizationId`, `userId`, `terminatedAt` |
| **Command called** | `RevokeMandateHandler` (for all active mandates linked to the employee's `customerId`) |
| **Flow** | Finds all `ACTIVE` mandates for `organizationId` where `customerId` matches the terminated employee's userId. Calls `RevokeMandateHandler` on each. Prevents deduction from terminated staff. |

---

## Presentation Layer

### Controller: `PaymentMandateController`

**Package**: `com.atlashub.pay.mandates.presentation.rest`

```java
@RestController
@RequestMapping("/api/v1/pay/mandates")
@Tag(name = "Payment Mandates")
public class PaymentMandateController { ... }
```

| Method | Path | Auth | RBAC | Request DTO | Response DTO |
|---|---|---|---|---|---|
| `POST` | `/api/v1/pay/mandates` | Bearer JWT | `pay:mandates:create` | `CreateMandateRequest` | `ApiResponse<CreateMandateResponse>` |
| `POST` | `/api/v1/pay/mandates/{id}/pause` | Bearer JWT | `pay:mandates:create` | — | `ApiResponse<Void>` |
| `POST` | `/api/v1/pay/mandates/{id}/resume` | Bearer JWT | `pay:mandates:create` | — | `ApiResponse<Void>` |
| `POST` | `/api/v1/pay/mandates/{id}/revoke` | Bearer JWT | `pay:mandates:revoke` | — | `ApiResponse<Void>` |
| `GET` | `/api/v1/pay/mandates` | Bearer JWT | `pay:mandates:read` | Query params: `customerId`, `status` | `ApiResponse<List<MandateResponse>>` |
| `GET` | `/api/v1/pay/mandates/{id}` | Bearer JWT | `pay:mandates:read` | — | `ApiResponse<MandateResponse>` |

### DTOs

**Package**: `com.atlashub.pay.mandates.presentation.dto`

**`CreateMandateRequest`**
```java
public record CreateMandateRequest(
    @NotBlank String customerId,
    @Email @NotBlank String email,
    @NotNull BigDecimal amount,
    @NotBlank String currency,
    @NotNull MandateFrequency frequency,
    @NotBlank String authorizationCode,
    @NotNull LocalDate firstChargeDate
) {}
```

**`MandateResponse`**
```java
public record MandateResponse(
    Long id,
    Long organizationId,
    String customerId,
    String email,
    BigDecimal amount,
    String currency,
    String frequency,
    String status,
    LocalDate nextChargeDate,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {}
```

---

## RBAC Table

| Permission | Granted To | Operation |
|---|---|---|
| `pay:mandates:create` | `OWNER`, `ADMIN`, `DEVELOPER` | Create mandates, pause/resume |
| `pay:mandates:revoke` | `OWNER`, `ADMIN` | Permanently revoke a mandate |
| `pay:mandates:read` | `OWNER`, `ADMIN`, `FINANCE`, `DEVELOPER` | List and view mandates |

---

## Maker-Checker

Not applicable for this submodule. Mandate management (create, pause, resume, revoke) is a standard single-user operation. The scheduler-triggered charge is a system action requiring no approval.

---

## Socket Events

Mandate events are not pushed via WebSocket. Outcome notifications (charge successful, mandate revoked) are delivered via the `pay:webhooks` outbound webhook to the organization's registered endpoint.

---

## Domain Events Table

| Event | Kafka Topic | Published When | Consumed By |
|---|---|---|---|
| `MandatePausedEvent` | `pay-events` | Mandate paused | `notifications`, `pay:webhooks` |
| `MandateResumedEvent` | `pay-events` | Mandate resumed | `notifications`, `pay:webhooks` |
| `MandateRevokedEvent` | `pay-events` | Mandate revoked | `notifications`, `pay:webhooks` |
| `MandateExpiredEvent` | `pay-events` | Mandate marked expired | `notifications` |
| `MandateChargedEvent` | `pay-events` | Scheduler charge succeeds | `notifications`, `pay:webhooks`, `pay:tx-query` |

---

## Distributed Architecture

### Locking
- **Optimistic locking** (`@Version`) on `PaymentMandateJpa`. Suitable for mandate lifecycle operations (pause, resume, revoke) which have very low contention.
- The scheduler processes each mandate independently; there is no batch-level locking.

### Idempotency
- `MandateChargeScheduler` runs once per day. If it crashes mid-run, it restarts from the top and re-processes any mandates that were not yet charged for the day. `ChargeMandateHandler` is idempotent: it checks whether the mandate's `nextChargeDate` has already been advanced before initiating a charge.

### Outbox
- `MandatePausedEvent`, `MandateResumedEvent`, `MandateRevokedEvent`, `MandateExpiredEvent`, `MandateChargedEvent` are written to the outbox in the same DB transaction as the state change.

---

## Complete File List

```
com.atlashub.pay.mandates/
├── application/
│   ├── commands/
│   │   ├── ChargeMandate/
│   │   │   ├── ChargeMandateCommand.java
│   │   │   └── ChargeMandateHandler.java
│   │   ├── CreateMandate/
│   │   │   ├── CreateMandateCommand.java
│   │   │   ├── CreateMandateHandler.java
│   │   │   └── CreateMandateResponse.java
│   │   ├── PauseMandate/
│   │   │   ├── PauseMandateCommand.java
│   │   │   └── PauseMandateHandler.java
│   │   ├── ResumeMandate/
│   │   │   ├── ResumeMandateCommand.java
│   │   │   └── ResumeMandateHandler.java
│   │   └── RevokeMandate/
│   │       ├── RevokeMandateCommand.java
│   │       └── RevokeMandateHandler.java
│   └── queries/
│       ├── GetMandate/
│       │   ├── GetMandateQuery.java
│       │   ├── GetMandateHandler.java
│       │   └── MandateResult.java
│       └── ListMandates/
│           ├── ListMandatesQuery.java
│           └── ListMandatesHandler.java
├── domain/
│   ├── entities/
│   │   └── PaymentMandate.java
│   ├── events/
│   │   ├── MandateChargedEvent.java
│   │   ├── MandateExpiredEvent.java
│   │   ├── MandatePausedEvent.java
│   │   ├── MandateResumedEvent.java
│   │   └── MandateRevokedEvent.java
│   ├── exceptions/
│   │   ├── ExpiredMandateException.java
│   │   ├── MandateNotFoundException.java
│   │   ├── MandatePausedException.java
│   │   └── MandateRevokedException.java
│   ├── repositories/
│   │   └── PaymentMandateRepository.java
│   └── valueobject/
│       ├── EmailAddress.java
│       ├── MandateFrequency.java
│       └── MandateStatus.java
├── infrastructure/
│   ├── messaging/
│   │   └── MandateChargeScheduler.java
│   ├── persistence/
│   │   ├── adapters/
│   │   │   └── PaymentMandateRepositoryAdapter.java
│   │   ├── entities/
│   │   │   └── PaymentMandateJpa.java
│   │   ├── mappers/
│   │   │   └── PaymentMandateMapper.java
│   │   └── repositories/
│   │       └── SpringDataPaymentMandateRepository.java
└── presentation/
    ├── dto/
    │   ├── CreateMandateRequest.java
    │   └── MandateResponse.java
    └── rest/
        └── PaymentMandateController.java
```
