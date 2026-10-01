# HR Payroll Design (`atlashub-hr` / `com.atlashub.hr.payroll`)

## Role & Purpose

The `payroll` subpackage orchestrates the full monthly salary disbursement cycle with a mandatory **Maker-Checker** workflow. One authorized user initiates and submits the payroll (maker), and a **different** authorized user must approve it (checker) before any money moves. Even if authorization is bypassed, `PayrollRun.approve()` rejects self-approval at the domain level.

Gradle module: `atlashub-hr`  
Package: `com.atlashub.hr.payroll`

---

## Domain Layer

### `PayrollRun` (Aggregate Root)

**Package:** `com.atlashub.hr.payroll.domain.entities`

```
PayrollRun
├── id              : Long
├── organizationId  : Long
├── period          : YearMonth        ← e.g., 2026-09
├── payslips        : List<Payslip>
├── totalGross      : Money
├── totalDeductions : Money
├── totalNet        : Money
├── status          : PayrollStatus    ← DRAFT | PENDING_APPROVAL | APPROVED | PROCESSING | DISBURSED | FAILED
├── initiatedBy     : Long             ← userId (maker)
├── approvedBy      : Long             ← userId (checker); nullable
├── approvedAt      : ZonedDateTime    ← nullable
└── createdAt       : ZonedDateTime
```

**State machine:**
```
DRAFT → PENDING_APPROVAL → APPROVED → PROCESSING → DISBURSED
                                               ↓
                                           APPROVED (retry after failure)
PENDING_APPROVAL → DRAFT (expiry after 72 hours)
any → FAILED (unrecoverable)
```

**Business methods (on entity — state transitions on a single aggregate):**

| Method | Guard | Events | Exceptions |
|---|---|---|---|
| `submitForApproval(initiatorId)` | status == DRAFT | `PayrollPendingApprovalEvent` | `InvalidPayrollStateException` |
| `approve(approverId)` | status == PENDING_APPROVAL **AND** `approverId ≠ initiatedBy` | `PayrollApprovedEvent` | `SelfApprovalNotAllowedException`, `InvalidPayrollStateException` |
| `markProcessing()` | status == APPROVED | — | `InvalidPayrollStateException` |
| `markDisbursed()` | status == PROCESSING | `PayrollDisbursedEvent` | `InvalidPayrollStateException` |
| `revertToApproved(reason)` | status == PROCESSING | — | — |
| `markFailed(reason)` | any | — | — |
| `expire()` | status == PENDING_APPROVAL | `PayrollApprovalExpiredEvent` | — |

**Maker-Checker domain enforcement:**
```java
public void approve(Long approverId) {
    if (this.status != PayrollStatus.PENDING_APPROVAL) {
        throw new InvalidPayrollStateException(
            "Payroll is not pending approval — current status: " + this.status);
    }
    if (approverId.equals(this.initiatedBy)) {
        throw new SelfApprovalNotAllowedException(
            "The approver cannot be the same person who initiated the payroll run");
    }
    this.approvedBy = approverId;
    this.approvedAt = ZonedDateTime.now();
    this.status = PayrollStatus.APPROVED;
    registerEvent(new PayrollApprovedEvent(
        UUID.randomUUID().toString(), String.valueOf(this.id),
        ZonedDateTime.now(),
        new PayrollApprovedEvent.Payload(this.id, this.organizationId, approverId)
    ));
}
```

---

### `Payslip` (Entity)

```
Payslip
├── id              : Long
├── payrollRunId    : Long
├── employeeId      : Long
├── grossPay        : Money
├── totalAllowances : Money
├── totalDeductions : Money
├── netPay          : Money
├── breakdown       : List<PayslipLineItem>
└── status          : PayslipStatus    ← DRAFT | DISBURSED | FAILED
```

### `PayslipLineItem` (Entity)

```
PayslipLineItem
├── id        : Long
├── payslipId : Long
├── name      : String
├── type      : LineItemType   ← ALLOWANCE | DEDUCTION
└── amount    : Money
```

---

### Domain Service: `PayrollCalculationService`

**Package:** `com.atlashub.hr.payroll.domain.services`  
**Declared as:** `@Bean` in `ApplicationConfig` (not `@Component` — lives in domain, must not have Spring annotations)

**Responsibility:** Given `organizationId` and `YearMonth`, fetches all `ACTIVE`/`PROBATION` employees, resolves each employee's `SalaryGrade` (base salary, grade allowances, grade deductions) plus personal `EmployeeAllowance` and `EmployeeDeduction` overrides. Calculates gross, applies deductions, generates a `Payslip` with full `PayslipLineItem` breakdown for each employee.

**Why a domain service, not an entity method:** The calculation requires coordinated access to `Employee`, `SalaryGrade`, `EmployeeAllowance`, and `EmployeeDeduction` — four separate aggregates. Placing this on any one entity would force it to accept repositories or aggregate collections as arguments, violating aggregate isolation.

**Methods:**
```java
List<Payslip> calculatePayroll(Long organizationId, YearMonth period);
Money calculateGross(Employee employee, SalaryGrade grade, List<EmployeeAllowance> allowances);
Money calculateDeductions(Employee employee, SalaryGrade grade, List<EmployeeDeduction> deductions);
```

---

### Application Ports — `com.atlashub.hr.payroll.application.port`

| Port | Responsibility | Type |
|---|---|---|
| `PayrollReserveAccountPort` | Query balance of org's Payroll Reserve Account in `atlashub-pay:ledger` | **Read-only** sync query — acceptable as sync because it's a guard check before action |

> **Removed:** `PayBulkPayoutPort` was a synchronous cross-module **write** port that violated architecture rules. Payroll disbursement is now fully async via `PayrollApprovedEvent` → `pay:transfers` listener.

---

### Domain Events — `com.atlashub.hr.payroll.domain.events`

| Event | Published When | Consumed By |
|---|---|---|
| `PayrollPendingApprovalEvent` | Payroll submitted for approval | `notifications` (WS push + email to each approver) |
| `PayrollApprovedEvent` | Approved by checker | `pay:transfers` (listens → creates bulk payout for each payslip), `accounting` (Dr Salary Expense, Cr Payroll Payable) |
| `PayrollDisbursedEvent` | All salaries paid | `accounting` (Dr Payroll Payable, Cr Payroll Reserve Account), `notifications` (SMS/email each employee with payslip), `analytics` (update payroll cost projection) |
| `PayrollApprovalExpiredEvent` | 72-hour approval window expired | `notifications` (email maker to re-submit) |

**Event payload shapes:**
```
PayrollApprovedEvent.payload
├── payrollRunId    : Long
├── organizationId  : Long
├── period          : String      ← e.g. "2024-01"
├── totalAmount     : BigDecimal
├── currency        : String
├── approvedByUserId: Long
├── payslips        : List
│   └── (each) employeeId, recipientNuban, recipientBankCode, recipientName, netPay
└── approvedAt      : ZonedDateTime

PayrollDisbursedEvent.payload
├── payrollRunId    : Long
├── organizationId  : Long
├── period          : String
├── totalAmount     : BigDecimal
├── currency        : String
└── disbursedAt     : ZonedDateTime
```


---

### Domain Exceptions — `com.atlashub.hr.payroll.domain.exceptions`

```java
public class PayrollRunNotFoundException extends NotFoundException {
    public PayrollRunNotFoundException(Long id) { super("Payroll run not found: " + id); }
}
public class InvalidPayrollStateException extends BusinessRuleException {
    public InvalidPayrollStateException(String message) { super(message); }
}
public class SelfApprovalNotAllowedException extends AuthorizationException {
    public SelfApprovalNotAllowedException() {
        super("The approver cannot be the same person who initiated the payroll run");
    }
    public SelfApprovalNotAllowedException(String message) { super(message); }
}
public class InsufficientPayrollReserveException extends BusinessRuleException {
    public InsufficientPayrollReserveException() {
        super("Payroll reserve account has insufficient funds to cover the payroll run");
    }
}
public class InsufficientApproversException extends BusinessRuleException {
    public InsufficientApproversException() {
        super("At least 2 members with hr:payroll:approve permission are required before initiating payroll");
    }
}
public class DuplicatePayrollPeriodException extends ConflictException {
    public DuplicatePayrollPeriodException(String period) {
        super("A payroll run already exists for period: " + period);
    }
}
```

---

## Application Layer

### Commands — `com.atlashub.hr.payroll.application.commands`

#### `InitiatePayrollCommand`
```java
record InitiatePayrollCommand(Long organizationId, YearMonth period, Long initiatorUserId)
```
**Handler:** `InitiatePayrollHandler` | **Response:** `InitiatePayrollResponse(Long payrollRunId)`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:payroll:initiate')")`  
**Flow:**
1. Query `MembershipQueryPort.countMembersWithPermission(orgId, "hr:payroll:approve") >= 2` → `InsufficientApproversException`
2. Check no existing `DRAFT` or `PENDING_APPROVAL` run for the same period → `DuplicatePayrollPeriodException`
3. Delegate to `PayrollCalculationService.calculatePayroll(orgId, period)` → `List<Payslip>`
4. Create `PayrollRun` (status = DRAFT) with all payslips
5. `repository.save(payrollRun)` → return `payrollRunId`

---

#### `SubmitPayrollForApprovalCommand`
```java
record SubmitPayrollForApprovalCommand(Long payrollRunId, Long initiatorUserId)
```
**Handler:** `SubmitPayrollForApprovalHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:payroll:initiate')")`  
**Flow:** Load `PayrollRun` → `run.submitForApproval(initiatorUserId)` → `repository.save(run)` → `PayrollPendingApprovalEvent` published via outbox → `SelectiveWebSocketBroadcaster` pushes to each approver's `/user/{approverId}/queue/notifications`

---

#### `ApprovePayrollCommand`
```java
record ApprovePayrollCommand(Long payrollRunId, Long approverUserId)
```
**Handler:** `ApprovePayrollHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:payroll:approve')")`  
**Maker-Checker:** Domain enforces `approverUserId ≠ initiatedBy`  
**Flow:**
1. Load `PayrollRun`
2. `run.approve(approverUserId)` — throws `SelfApprovalNotAllowedException` if same user
3. `repository.save(run)` → `PayrollApprovedEvent` published
4. Immediately invoke `DisbursePayrollHandler`

---

#### `DisbursePayrollCommand`
```java
record DisbursePayrollCommand(Long payrollRunId)
```
**Handler:** `DisbursePayrollHandler` | **Response:** `void`  
**Invocation source:** Called by `ApprovePayrollHandler` after approval, or manually by admin after investigation  
**Flow:**
1. Load `PayrollRun` → validate status is APPROVED
2. `PayrollReserveAccountPort.getBalance(orgId)` — sync read-only query against `pay:ledger` balance endpoint → throws `InsufficientPayrollReserveException` if insufficient
3. `run.markProcessing()` → `repository.save(run)`
4. `PayrollApprovedEvent` is published via outbox → `pay:transfers` listens and creates the bulk payout entries for each payslip. **No sync cross-module write call here.**
5. Success/failure handled asynchronously via Kafka (`BulkPayoutCompletedEvent` / `BulkPayoutFailedEvent`)

> **Architecture Note:** `PayBulkPayoutPort` was a synchronous cross-module write port and has been removed. `PayrollReserveAccountPort` (a read-only balance query) is retained as a sync read port — querying a balance before committing to disbursement is acceptable as a synchronous read.



---

#### `MarkPayrollDisbursedCommand`
```java
record MarkPayrollDisbursedCommand(Long payrollRunId)
```
**Handler:** `MarkPayrollDisbursedHandler` | **Response:** `void`  
**Invocation source:** `BulkPayoutCompletedListener`  
**Flow:** Load `PayrollRun` → `run.markDisbursed()` → `repository.save(run)` → `PayrollDisbursedEvent` published

---

#### `RevertPayrollToApprovedCommand`
```java
record RevertPayrollToApprovedCommand(Long payrollRunId, String failureReason)
```
**Handler:** `RevertPayrollToApprovedHandler` | **Response:** `void`  
**Invocation source:** `BulkPayoutFailedListener`  
**Flow:** Load `PayrollRun` → `run.revertToApproved(failureReason)` → `repository.save(run)` → admin investigates and manually retries

---

#### `ExpirePayrollApprovalCommand`
```java
record ExpirePayrollApprovalCommand(Long payrollRunId)
```
**Handler:** `ExpirePayrollApprovalHandler` | **Response:** `void`  
**Invocation source:** `PayrollApprovalExpiryScheduler`  
**Flow:** Load `PayrollRun` → `run.expire()` → `repository.save(run)` → `PayrollApprovalExpiredEvent` published → run reverts to DRAFT

---

### Queries — `com.atlashub.hr.payroll.application.queries`

#### `ListPayrollRunsQuery`
```java
record ListPayrollRunsQuery(Long organizationId, PayrollStatus status)
```
**Handler:** `ListPayrollRunsHandler` | **Result:** `List<PayrollRunResult>`  
**Justification:** Bounded — typically 12 runs per year per org.

`PayrollRunResult`: `id`, `period`, `totalGross`, `totalDeductions`, `totalNet`, `status`, `initiatedBy`, `approvedBy`, `approvedAt`, `createdAt`

---

#### `GetPayslipQuery`
```java
record GetPayslipQuery(Long payslipId)
```
**Handler:** `GetPayslipHandler` | **Result:** `PayslipResult`

`PayslipResult`: `id`, `employeeId`, `employeeName`, `period`, `grossPay`, `totalAllowances`, `totalDeductions`, `netPay`, `breakdown: List<PayslipLineItemResult>`, `status`

---

#### `ListPayslipsByEmployeeQuery`
```java
record ListPayslipsByEmployeeQuery(Long employeeId, int year)
```
**Handler:** `ListPayslipsByEmployeeHandler` | **Result:** `List<PayslipResult>`  
**Justification:** Bounded — at most 12 payslips per employee per year.

---

## Infrastructure Layer

### Persistence

**JPA Entities:**

| Entity | Table | Locking |
|---|---|---|
| `PayrollRunJpaEntity` | `hr_payroll_runs` | `@Version` optimistic |
| `PayslipJpaEntity` | `hr_payslips` | — |
| `PayslipLineItemJpaEntity` | `hr_payslip_line_items` | — |

**Spring Data Repositories:**

```
PayrollRunJpaRepository
  + findByOrganizationIdAndStatus(Long orgId, PayrollStatus status): List<PayrollRunJpaEntity>
  + findByOrganizationIdAndPeriod(Long orgId, YearMonth period): Optional<PayrollRunJpaEntity>
  + findByStatusAndCreatedAtBefore(PayrollStatus status, ZonedDateTime threshold): List<PayrollRunJpaEntity>

PayslipJpaRepository
  + findByPayrollRunId(Long payrollRunId): List<PayslipJpaEntity>
  + findByEmployeeIdAndYear(Long employeeId, int year): List<PayslipJpaEntity>
```

**Mappers:**
- `PayrollRunMapper` — `PayrollRun ↔ PayrollRunJpaEntity`
- `PayslipMapper` — `Payslip ↔ PayslipJpaEntity` (includes line items)

**Repository Adapter:**
- `PayrollRunRepositoryAdapter` → sequence: `hr_payroll_run_seq`

### Listeners — `infrastructure/messaging/listeners/`

#### `BulkPayoutCompletedListener`
| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `hr-payroll-group` |
| **Event consumed** | `BulkPayoutCompletedEvent` |
| **Payload** | `payrollRunId`, `organizationId`, `totalAmountDisbursed`, `disbursedAt` |
| **Command called** | `MarkPayrollDisbursedCommand` |
| **Flow** | `PayrollRun.markDisbursed()` → `PayrollDisbursedEvent` published |
| **Idempotency** | Inbox keyed on `(eventId, "hr-payroll-group")` — duplicate must not mark same run disbursed twice |

#### `BulkPayoutFailedListener`
| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `hr-payroll-group` |
| **Event consumed** | `BulkPayoutFailedEvent` |
| **Payload** | `payrollRunId`, `organizationId`, `failureReason` |
| **Command called** | `RevertPayrollToApprovedCommand` |
| **Flow** | `PayrollRun.revertToApproved(reason)` → admin investigates and retries disbursement |

### Scheduler — `infrastructure/schedulers/`

#### `PayrollApprovalExpiryScheduler`
| Attribute | Value |
|---|---|
| **Cron** | `0 0 * * * *` (every hour) |
| **Finds** | `PayrollRun` records with status `PENDING_APPROVAL` older than 72 hours |
| **Action** | Calls `ExpirePayrollApprovalHandler` → `run.expire()` → reverts to DRAFT |
| **Response** | `void` |

### Infrastructure Services — `infrastructure/services/`

| Adapter | Implements | External |
|---|---|---|
| `PayBulkPayoutAdapter` | `PayBulkPayoutPort` | `atlashub-pay` bulk payout API |
| `PayReserveAccountAdapter` | `PayrollReserveAccountPort` | `atlashub-pay` account balance query |

---

## Presentation Layer

### Controller: `HrPayrollController`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/payroll/initiate` | `hr:payroll:initiate` | `InitiatePayrollRequest` | `InitiatePayrollResponse` |
| `POST` | `/payroll/{id}/submit` | `hr:payroll:initiate` | — | `void` |
| `POST` | `/payroll/{id}/approve` | `hr:payroll:approve` | — | `void` |
| `GET` | `/payroll` | — | `?orgId&status` | `List<PayrollRunResult>` |
| `GET` | `/payslips/{id}` | — | — | `PayslipResult` |
| `GET` | `/payslips` | — | `?employeeId&year` | `List<PayslipResult>` |

### DTOs

- `InitiatePayrollRequest`: `organizationId`, `period` (YYYY-MM string)
- `InitiatePayrollResponse`: `payrollRunId`
- `PayrollRunResult`: `id`, `period`, `totalGross`, `totalDeductions`, `totalNet`, `status`, `initiatedBy`, `approvedBy`, `approvedAt`, `createdAt`
- `PayslipResult`: `id`, `employeeId`, `employeeName`, `period`, `grossPay`, `totalAllowances`, `totalDeductions`, `netPay`, `breakdown`, `status`
- `PayslipLineItemResult`: `name`, `type`, `amount`

---

## RBAC Table

| Permission | Granted To | Commands |
|---|---|---|
| `hr:payroll:initiate` | Payroll Officer (Maker) | `InitiatePayrollHandler`, `SubmitPayrollForApprovalHandler` |
| `hr:payroll:approve` | Senior Finance (Checker, MUST be different from maker) | `ApprovePayrollHandler` |

---

## Maker-Checker

| Operation | Maker Permission | Checker Permission | Domain Enforcement |
|---|---|---|---|
| Payroll Run | `hr:payroll:initiate` | `hr:payroll:approve` | `PayrollRun.approve()` throws `SelfApprovalNotAllowedException` if `approverId.equals(initiatedBy)` |

**Pre-validation:** `InitiatePayrollHandler` queries `MembershipQueryPort.countMembersWithPermission(orgId, "hr:payroll:approve")`. If count < 2, throws `InsufficientApproversException`.

**Expiry:** `PayrollApprovalExpiryScheduler` hourly → finds `PENDING_APPROVAL` older than 72h → `run.expire()` → DRAFT → maker notified.

---

## Socket Events

| Event | WS Channel | Payload |
|---|---|---|
| `PayrollPendingApprovalEvent` | `/user/{approverId}/queue/notifications` per approver | `type: "APPROVAL_REQUIRED"`, `message: "Payroll for {period} needs your approval"`, `data: { entityType: "PAYROLL_RUN", entityId: payrollRunId }` |

---

## Payroll Disbursement Saga (Choreography)

```
InitiatePayrollHandler (hr:payroll:initiate)
  └─ PayrollRun created in DRAFT; PayrollCalculationService generates Payslips

SubmitPayrollForApprovalHandler
  └─ PayrollRun → PENDING_APPROVAL
  └─ PayrollPendingApprovalEvent → notifications → WS push + email to each approver

ApprovePayrollHandler (hr:payroll:approve, DIFFERENT user)
  └─ PayrollRun.approve(approverId) [domain validates maker ≠ checker]
  └─ PayrollRun → APPROVED → PayrollApprovedEvent
       ──► pay: executes bulk payouts
       ──► accounting: Dr Salary Expense, Cr Payroll Payable

  ┌─ SUCCESS: BulkPayoutCompletedEvent (pay-events Kafka topic)
  │  → BulkPayoutCompletedListener → MarkPayrollDisbursedHandler
  │  → PayrollRun.markDisbursed() → PayrollDisbursedEvent
  │       ──► accounting: Dr Payroll Payable, Cr Payroll Reserve Account
  │       ──► notifications: SMS each employee
  └─

  ┌─ FAILURE: BulkPayoutFailedEvent (pay-events Kafka topic)
  │  → BulkPayoutFailedListener → RevertPayrollToApprovedHandler
  │  → PayrollRun.revertToApproved(reason) → admin investigates, retries
  └─
```

---

## Distributed Architecture

### Locking
- `PayrollRun` — Optimistic (`@Version`) for concurrent status transitions

### Outbox
- `PayrollApprovedEvent` — triggers bulk payout in pay; must not be lost
- `LoanApprovedEvent` (from loan subpackage) — triggers loan disbursement in pay
- `PayrollDisbursedEvent` — triggers accounting journal entries

### Inbox
- `BulkPayoutCompletedEvent` — keyed on `(eventId, "hr-payroll-group")`; retry must not mark same run disbursed twice
- `BulkPayoutFailedEvent` — keyed on `(eventId, "hr-payroll-group")`

---

## Complete File List

```
atlashub-hr/src/main/java/com/atlashub/hr/payroll/
├── domain/
│   ├── entities/
│   │   ├── PayrollRun.java
│   │   ├── Payslip.java
│   │   └── PayslipLineItem.java
│   ├── events/
│   │   ├── PayrollPendingApprovalEvent.java
│   │   ├── PayrollApprovedEvent.java
│   │   ├── PayrollDisbursedEvent.java
│   │   └── PayrollApprovalExpiredEvent.java
│   ├── exceptions/
│   │   ├── PayrollRunNotFoundException.java
│   │   ├── InvalidPayrollStateException.java
│   │   ├── SelfApprovalNotAllowedException.java
│   │   ├── InsufficientPayrollReserveException.java
│   │   ├── InsufficientApproversException.java
│   │   └── DuplicatePayrollPeriodException.java
│   ├── repositories/
│   │   └── PayrollRunRepository.java
│   ├── services/
│   │   └── PayrollCalculationService.java
│   └── valueobject/
│       ├── PayrollStatus.java
│       └── PayslipStatus.java
├── application/
│   ├── commands/
│   │   ├── InitiatePayroll/
│   │   │   ├── InitiatePayrollCommand.java
│   │   │   ├── InitiatePayrollHandler.java
│   │   │   └── InitiatePayrollResponse.java
│   │   ├── SubmitPayrollForApproval/
│   │   │   ├── SubmitPayrollForApprovalCommand.java
│   │   │   └── SubmitPayrollForApprovalHandler.java
│   │   ├── ApprovePayroll/
│   │   │   ├── ApprovePayrollCommand.java
│   │   │   └── ApprovePayrollHandler.java
│   │   ├── DisbursePayroll/
│   │   │   ├── DisbursePayrollCommand.java
│   │   │   └── DisbursePayrollHandler.java
│   │   ├── MarkPayrollDisbursed/
│   │   │   ├── MarkPayrollDisbursedCommand.java
│   │   │   └── MarkPayrollDisbursedHandler.java
│   │   ├── RevertPayrollToApproved/
│   │   │   ├── RevertPayrollToApprovedCommand.java
│   │   │   └── RevertPayrollToApprovedHandler.java
│   │   └── ExpirePayrollApproval/
│   │       ├── ExpirePayrollApprovalCommand.java
│   │       └── ExpirePayrollApprovalHandler.java
│   ├── queries/
│   │   ├── ListPayrollRuns/
│   │   │   ├── ListPayrollRunsQuery.java
│   │   │   ├── ListPayrollRunsHandler.java
│   │   │   └── PayrollRunResult.java
│   │   ├── GetPayslip/
│   │   │   ├── GetPayslipQuery.java
│   │   │   ├── GetPayslipHandler.java
│   │   │   ├── PayslipResult.java
│   │   │   └── PayslipLineItemResult.java
│   │   └── ListPayslipsByEmployee/
│   │       ├── ListPayslipsByEmployeeQuery.java
│   │       └── ListPayslipsByEmployeeHandler.java
│   └── port/
│       ├── PayBulkPayoutPort.java
│       └── PayrollReserveAccountPort.java
├── infrastructure/
│   ├── messaging/
│   │   ├── events/
│   │   │   ├── BulkPayoutCompletedPayload.java
│   │   │   └── BulkPayoutFailedPayload.java
│   │   └── listeners/
│   │       ├── BulkPayoutCompletedListener.java
│   │       └── BulkPayoutFailedListener.java
│   ├── persistence/
│   │   ├── adapters/
│   │   │   └── PayrollRunRepositoryAdapter.java
│   │   ├── entities/
│   │   │   ├── PayrollRunJpaEntity.java
│   │   │   ├── PayslipJpaEntity.java
│   │   │   └── PayslipLineItemJpaEntity.java
│   │   ├── mappers/
│   │   │   ├── PayrollRunMapper.java
│   │   │   └── PayslipMapper.java
│   │   └── repositories/
│   │       ├── PayrollRunJpaRepository.java
│   │       └── PayslipJpaRepository.java
│   ├── schedulers/
│   │   └── PayrollApprovalExpiryScheduler.java
│   └── services/
│       ├── PayBulkPayoutAdapter.java
│       └── PayReserveAccountAdapter.java
└── presentation/
    ├── dto/
    │   ├── InitiatePayrollRequest.java
    │   ├── InitiatePayrollResponse.java
    │   ├── PayrollRunResult.java
    │   ├── PayslipResult.java
    │   └── PayslipLineItemResult.java
    └── rest/
        └── HrPayrollController.java
```
