# HR Leave Design (`atlashub-hr` / `com.atlashub.hr.leave`)

## Role & Purpose

The `leave` subpackage manages paid and unpaid leave: type configuration, employee applications, manager approvals, and balance tracking. Leave balances use pessimistic locking during approval to prevent concurrent over-allocation.

Gradle module: `atlashub-hr`  
Package: `com.atlashub.hr.leave`

---

## Domain Layer

### `LeaveType` (Aggregate Root)

**Package:** `com.atlashub.hr.leave.domain.entities`

```
LeaveType
├── id               : Long
├── organizationId   : Long
├── name             : String           ← e.g., "Annual Leave", "Sick Leave"
├── maxDaysPerYear   : Integer
├── isPaid           : Boolean
├── requiresApproval : Boolean          ← if false, applications are auto-approved
├── carryOverAllowed : Boolean
└── maxCarryOverDays : Integer
```

---

### `LeaveApplication` (Aggregate Root)

```
LeaveApplication
├── id             : Long
├── employeeId     : Long
├── organizationId : Long
├── leaveTypeId    : Long
├── startDate      : LocalDate
├── endDate        : LocalDate
├── daysRequested  : Integer
├── reason         : String
├── status         : LeaveStatus      ← PENDING | APPROVED | REJECTED | CANCELLED
├── approvedBy     : Long             ← nullable
└── approvedAt     : ZonedDateTime    ← nullable
```

**Business methods (on entity — state transitions, interact only with own fields + passed LeaveBalance):**

| Method | Inputs | Guard | Events | Exceptions |
|---|---|---|---|---|
| `approve(approverId)` | Long | status == PENDING | `LeaveApprovedEvent` | `InsufficientLeaveBalanceException`, `InvalidLeaveStateException` |
| `reject(reason)` | String | status == PENDING | `LeaveRejectedEvent` | `InvalidLeaveStateException` |
| `cancel()` | — | status == PENDING or APPROVED | — | `InvalidLeaveStateException` |

**Why on entity:** State transitions operate solely on the `LeaveApplication`'s own data. The balance deduction is delegated to `LeaveBalance.deduct()` by the handler, keeping both aggregates' boundaries clean.

---

### `LeaveBalance` (Entity)

```
LeaveBalance
├── id          : Long
├── employeeId  : Long
├── leaveTypeId : Long
├── year        : Integer
├── entitlement : Integer
├── used        : Integer
└── remaining   : Integer
```

**Business methods (on entity — pure arithmetic on own fields):**
- `deduct(int days)` — guards: `remaining >= days`; throws `InsufficientLeaveBalanceException`; updates `used` and `remaining`
- `restore(int days)` — called on leave cancellation (if was APPROVED) or after rejection of previously deducted leave

---

### Value Objects — `com.atlashub.hr.leave.domain.valueobject`

None specific to leave — uses shared `Money` if needed for paid leave calculations.

---

### Domain Events — `com.atlashub.hr.leave.domain.events`

| Event | Published When | Consumed By |
|---|---|---|
| `LeaveApprovedEvent` | Leave approved | `notifications` (email employee), `attendance` (marks employee ON_LEAVE on start date) |
| `LeaveRejectedEvent` | Leave rejected | `notifications` (email employee with reason) |

---

### Domain Exceptions — `com.atlashub.hr.leave.domain.exceptions`

```java
public class LeaveTypeNotFoundException extends NotFoundException {
    public LeaveTypeNotFoundException(Long id) { super("Leave type not found: " + id); }
}
public class LeaveApplicationNotFoundException extends NotFoundException {
    public LeaveApplicationNotFoundException(Long id) { super("Leave application not found: " + id); }
}
public class InsufficientLeaveBalanceException extends BusinessRuleException {
    public InsufficientLeaveBalanceException() {
        super("Insufficient leave balance for the requested number of days");
    }
}
public class LeaveDatesConflictException extends BusinessRuleException {
    public LeaveDatesConflictException() {
        super("An approved or pending leave application already covers these dates");
    }
}
public class InvalidLeaveStateException extends BusinessRuleException {
    public InvalidLeaveStateException(String message) { super(message); }
}
```

---

## Application Layer

### Commands — `com.atlashub.hr.leave.application.commands`

#### `CreateLeaveTypeCommand`
```java
record CreateLeaveTypeCommand(Long organizationId, String name, int maxDaysPerYear,
                              boolean isPaid, boolean requiresApproval,
                              boolean carryOverAllowed, int maxCarryOverDays)
```
**Handler:** `CreateLeaveTypeHandler` | **Response:** `CreateLeaveTypeResponse(Long leaveTypeId)`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:employees:manage')")`  
**Flow:** Create `LeaveType` → `repository.save()`

---

#### `ApplyForLeaveCommand`
```java
record ApplyForLeaveCommand(Long employeeId, Long leaveTypeId,
                            LocalDate startDate, LocalDate endDate, String reason)
```
**Handler:** `ApplyForLeaveHandler` | **Response:** `ApplyForLeaveResponse(Long leaveApplicationId)`  
**Flow:**
1. Load `LeaveType` → `LeaveTypeNotFoundException`
2. Calculate `daysRequested = workingDaysBetween(startDate, endDate)`
3. Check for date conflicts against existing PENDING/APPROVED applications → `LeaveDatesConflictException`
4. Create `LeaveApplication` (status = PENDING)
5. If `leaveType.requiresApproval == false` → immediately call `ApproveLeaveHandler` with systemUserId
6. `repository.save()` → publishes `LeaveApprovedEvent` if auto-approved

---

#### `ApproveLeaveCommand`
```java
record ApproveLeaveCommand(Long leaveApplicationId, Long approverId)
```
**Handler:** `ApproveLeaveHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:leave:approve')")`  
**Flow:**
1. Load `LeaveApplication` (must be PENDING)
2. Load `LeaveBalance` with **pessimistic lock** (`PESSIMISTIC_WRITE`) for this employee + leaveType + year
3. `balance.deduct(daysRequested)` → `InsufficientLeaveBalanceException`
4. `application.approve(approverId)` → `LeaveApprovedEvent` registered
5. Save both application and balance → event published via outbox

---

#### `RejectLeaveCommand`
```java
record RejectLeaveCommand(Long leaveApplicationId, String reason)
```
**Handler:** `RejectLeaveHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:leave:approve')")`  
**Flow:** Load `LeaveApplication` → `application.reject(reason)` → `repository.save()`

---

#### `CancelLeaveCommand`
```java
record CancelLeaveCommand(Long leaveApplicationId)
```
**Handler:** `CancelLeaveHandler` | **Response:** `void`  
**Flow:** Load `LeaveApplication` → if was APPROVED, load `LeaveBalance` → `balance.restore(daysRequested)` → `application.cancel()` → save both

---

### Queries — `com.atlashub.hr.leave.application.queries`

#### `GetLeaveBalanceQuery`
```java
record GetLeaveBalanceQuery(Long employeeId, int year)
```
**Handler:** `GetLeaveBalanceHandler` | **Result:** `List<LeaveBalanceResult>`  
**Justification:** Always bounded — one record per leave type per employee per year.

`LeaveBalanceResult`: `leaveTypeId`, `leaveTypeName`, `year`, `entitlement`, `used`, `remaining`

---

#### `ListLeaveApplicationsQuery`
```java
record ListLeaveApplicationsQuery(Long organizationId, Long employeeId, LeaveStatus status)
```
**Handler:** `ListLeaveApplicationsHandler` | **Result:** `List<LeaveApplicationResult>`  
**Justification:** Bounded per org in a given period — typically small.

`LeaveApplicationResult`: `id`, `employeeId`, `employeeName`, `leaveTypeName`, `startDate`, `endDate`, `daysRequested`, `reason`, `status`, `approvedBy`, `approvedAt`

---

## Infrastructure Layer

### Persistence

**JPA Entities:**

| Entity | Table | Locking |
|---|---|---|
| `LeaveTypeJpaEntity` | `hr_leave_types` | — |
| `LeaveApplicationJpaEntity` | `hr_leave_applications` | `@Version` optimistic |
| `LeaveBalanceJpaEntity` | `hr_leave_balances` | `@Lock(PESSIMISTIC_WRITE)` during approval |

**Spring Data Repositories:**

```
LeaveTypeJpaRepository
  + findByOrganizationId(Long orgId): List<LeaveTypeJpaEntity>

LeaveApplicationJpaRepository
  + findByOrganizationIdAndStatus(Long orgId, LeaveStatus status): List<LeaveApplicationJpaEntity>
  + findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status): List<LeaveApplicationJpaEntity>
  + existsByEmployeeIdAndStatusInAndDateRange(Long empId, List<LeaveStatus> statuses,
      LocalDate start, LocalDate end): boolean

LeaveBalanceJpaRepository
  + findByEmployeeIdAndLeaveTypeIdAndYear(Long empId, Long typeId, int year):
      Optional<LeaveBalanceJpaEntity>  // use @Lock(PESSIMISTIC_WRITE) in adapter
  + findByEmployeeIdAndYear(Long employeeId, int year): List<LeaveBalanceJpaEntity>
```

**Mappers:**
- `LeaveApplicationMapper` — `LeaveApplication ↔ LeaveApplicationJpaEntity`
- `LeaveBalanceMapper` — `LeaveBalance ↔ LeaveBalanceJpaEntity`

**Repository Adapters:**
- `LeaveApplicationRepositoryAdapter` → sequence: `hr_leave_application_seq`
- `LeaveBalanceRepositoryAdapter` → sequence: `hr_leave_balance_seq`

---

## Presentation Layer

### Controller: `HrLeaveController`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/leave-types` | `hr:employees:manage` | `CreateLeaveTypeRequest` | `CreateLeaveTypeResponse` |
| `POST` | `/leave/apply` | — | `ApplyForLeaveRequest` | `ApplyForLeaveResponse` |
| `GET` | `/leave` | — | `?orgId&employeeId&status` | `List<LeaveApplicationResult>` |
| `POST` | `/leave/{id}/approve` | `hr:leave:approve` | — | `void` |
| `POST` | `/leave/{id}/reject` | `hr:leave:approve` | `RejectLeaveRequest` | `void` |
| `POST` | `/leave/{id}/cancel` | — | — | `void` |
| `GET` | `/leave/balance` | — | `?employeeId&year` | `List<LeaveBalanceResult>` |

### DTOs

- `CreateLeaveTypeRequest`: `organizationId`, `name`, `maxDaysPerYear`, `isPaid`, `requiresApproval`, `carryOverAllowed`, `maxCarryOverDays`
- `CreateLeaveTypeResponse`: `leaveTypeId`
- `ApplyForLeaveRequest`: `employeeId`, `leaveTypeId`, `startDate`, `endDate`, `reason`
- `ApplyForLeaveResponse`: `leaveApplicationId`
- `RejectLeaveRequest`: `reason`
- `LeaveApplicationResult`: `id`, `employeeId`, `employeeName`, `leaveTypeName`, `startDate`, `endDate`, `daysRequested`, `reason`, `status`, `approvedBy`, `approvedAt`
- `LeaveBalanceResult`: `leaveTypeId`, `leaveTypeName`, `year`, `entitlement`, `used`, `remaining`

---

## RBAC Table

| Permission | Commands |
|---|---|
| `hr:employees:manage` | `CreateLeaveTypeHandler` |
| `hr:leave:approve` | `ApproveLeaveHandler`, `RejectLeaveHandler` |

---

## Distributed Architecture

### Locking
- `LeaveBalance` — **Pessimistic Write** during `ApproveLeaveHandler` to prevent concurrent approvals over-drawing the balance
- `LeaveApplication` — Optimistic (`@Version`)

### Outbox
- `LeaveApprovedEvent` — triggers attendance module to mark employee ON_LEAVE

---

## Complete File List

```
atlashub-hr/src/main/java/com/atlashub/hr/leave/
├── domain/
│   ├── entities/
│   │   ├── LeaveType.java
│   │   ├── LeaveApplication.java
│   │   └── LeaveBalance.java
│   ├── events/
│   │   ├── LeaveApprovedEvent.java
│   │   └── LeaveRejectedEvent.java
│   ├── exceptions/
│   │   ├── LeaveTypeNotFoundException.java
│   │   ├── LeaveApplicationNotFoundException.java
│   │   ├── InsufficientLeaveBalanceException.java
│   │   ├── LeaveDatesConflictException.java
│   │   └── InvalidLeaveStateException.java
│   └── repositories/
│       ├── LeaveTypeRepository.java
│       ├── LeaveApplicationRepository.java
│       └── LeaveBalanceRepository.java
├── application/
│   ├── commands/
│   │   ├── CreateLeaveType/
│   │   │   ├── CreateLeaveTypeCommand.java
│   │   │   ├── CreateLeaveTypeHandler.java
│   │   │   └── CreateLeaveTypeResponse.java
│   │   ├── ApplyForLeave/
│   │   │   ├── ApplyForLeaveCommand.java
│   │   │   ├── ApplyForLeaveHandler.java
│   │   │   └── ApplyForLeaveResponse.java
│   │   ├── ApproveLeave/
│   │   │   ├── ApproveLeaveCommand.java
│   │   │   └── ApproveLeaveHandler.java
│   │   ├── RejectLeave/
│   │   │   ├── RejectLeaveCommand.java
│   │   │   └── RejectLeaveHandler.java
│   │   └── CancelLeave/
│   │       ├── CancelLeaveCommand.java
│   │       └── CancelLeaveHandler.java
│   └── queries/
│       ├── GetLeaveBalance/
│       │   ├── GetLeaveBalanceQuery.java
│       │   ├── GetLeaveBalanceHandler.java
│       │   └── LeaveBalanceResult.java
│       └── ListLeaveApplications/
│           ├── ListLeaveApplicationsQuery.java
│           ├── ListLeaveApplicationsHandler.java
│           └── LeaveApplicationResult.java
├── infrastructure/
│   ├── persistence/
│   │   ├── adapters/
│   │   │   ├── LeaveApplicationRepositoryAdapter.java
│   │   │   └── LeaveBalanceRepositoryAdapter.java
│   │   ├── entities/
│   │   │   ├── LeaveTypeJpaEntity.java
│   │   │   ├── LeaveApplicationJpaEntity.java
│   │   │   └── LeaveBalanceJpaEntity.java
│   │   ├── mappers/
│   │   │   ├── LeaveApplicationMapper.java
│   │   │   └── LeaveBalanceMapper.java
│   │   └── repositories/
│   │       ├── LeaveTypeJpaRepository.java
│   │       ├── LeaveApplicationJpaRepository.java
│   │       └── LeaveBalanceJpaRepository.java
└── presentation/
    ├── dto/
    │   ├── CreateLeaveTypeRequest.java
    │   ├── CreateLeaveTypeResponse.java
    │   ├── ApplyForLeaveRequest.java
    │   ├── ApplyForLeaveResponse.java
    │   ├── RejectLeaveRequest.java
    │   ├── LeaveApplicationResult.java
    │   └── LeaveBalanceResult.java
    └── rest/
        └── HrLeaveController.java
```
