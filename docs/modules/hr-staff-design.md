# HR Staff Design (`atlashub-hr` / `com.atlashub.hr.staff`)

## Role & Purpose

The `staff` subpackage manages the full employee lifecycle: onboarding, bio-data, salary grades, allowances, deductions, qualifications, and disciplinary infractions. It is the foundation all other HR subpackages depend on — every leave application, loan, attendance record, and payslip is tied to an `Employee`.

Gradle module: `atlashub-hr`  
Package: `com.atlashub.hr.staff`

---

## Domain Layer

### Aggregate Root: `Employee`

**Package:** `com.atlashub.hr.staff.domain.entities`

```
Employee
├── id                  : Long
├── organizationId      : Long
├── userId              : Long
├── employeeNumber      : String              ← auto-generated "EMP-0042"
├── firstName           : String
├── lastName            : String
├── email               : EmailAddress        ← value object
├── phone               : PhoneNumber         ← value object
├── dateOfBirth         : LocalDate
├── address             : String
├── nationality         : String
├── hireDate            : LocalDate
├── terminationDate     : LocalDate           ← nullable
├── designation         : String
├── department          : String
├── reportingManagerId  : Long                ← nullable
├── salaryGradeId       : Long
├── baseSalary          : Money               ← overrides grade base if set
├── status              : EmployeeStatus      ← PROBATION|ACTIVE|ON_LEAVE|SUSPENDED|TERMINATED
├── createdAt           : ZonedDateTime
└── updatedAt           : ZonedDateTime
```

**Creation rules:** `employeeNumber` auto-generated as `"EMP-" + zero-padded nextId()`. Initial `status = PROBATION`. `hireDate` must not be in the future.

**State machine:**
```
PROBATION → ACTIVE → ON_LEAVE ⇄ ACTIVE
ACTIVE    → SUSPENDED → ACTIVE (reinstate)
ACTIVE    → TERMINATED
SUSPENDED → TERMINATED
```

**Business methods (all on entity — pure state transitions, no external repository access):**

| Method | Inputs | Guard | Events | Exceptions |
|---|---|---|---|---|
| `updateBioData(firstName, lastName, phone, address, nationality)` | Strings | — | `EmployeeProfileUpdatedEvent` | — |
| `promote(newDesignation, newGradeId, newBaseSalary)` | String, Long, Money | status != TERMINATED | `EmployeePromotedEvent` | `InvalidEmployeeStateException` |
| `suspend(reason)` | String | status == ACTIVE | `EmployeeSuspendedEvent` | `InvalidEmployeeStateException` |
| `reinstate()` | — | status == SUSPENDED | — | `InvalidEmployeeStateException` |
| `terminate(terminationDate)` | LocalDate | no active loan balance | `EmployeeTerminatedEvent` | `EmployeeHasOutstandingLoanException`, `InvalidEmployeeStateException` |
| `placeOnLeave()` | — | status == ACTIVE | — | — |
| `returnFromLeave()` | — | status == ON_LEAVE | — | — |

**Why on entity:** Each method transitions state on a single aggregate's own fields. No other repository access required.

---

### `EmployeeBank` (Aggregate Root)

```
EmployeeBank
├── id              : Long
├── employeeId      : Long
├── bankCode        : String
├── bankName        : String
├── accountNumber   : String
├── accountName     : String          ← resolved via Paystack name enquiry
├── isVerified      : Boolean
├── isPrimary       : Boolean
└── addedAt         : ZonedDateTime
```

**Business methods:**
- `verify(resolvedAccountName)` → sets `isVerified = true`, stores `accountName`
- `setPrimary()` → marks this bank as salary payout account

---

### `SalaryGrade` (Aggregate Root)

```
SalaryGrade
├── id              : Long
├── organizationId  : Long
├── name            : String           ← e.g., "Grade 7 — Senior Engineer"
├── baseSalary      : Money
├── allowances      : List<GradeAllowance>
└── deductions      : List<GradeDeduction>
```

### `GradeAllowance` (Entity)

```
GradeAllowance
├── id      : Long
├── gradeId : Long
├── name    : String                 ← e.g., "Housing Allowance"
├── type    : AllowanceType          ← FIXED | PERCENTAGE_OF_GROSS
└── value   : BigDecimal
```

### `GradeDeduction` (Entity)

```
GradeDeduction
├── id          : Long
├── gradeId     : Long
├── name        : String             ← e.g., "PAYE Tax"
├── type        : DeductionType      ← FIXED | PERCENTAGE_OF_GROSS
├── value       : BigDecimal
└── isStatutory : Boolean
```

### `EmployeeAllowance` (Entity)

```
EmployeeAllowance
├── id            : Long
├── employeeId    : Long
├── name          : String
├── type          : AllowanceType
├── value         : BigDecimal
├── effectiveFrom : LocalDate
└── effectiveTo   : LocalDate
```

### `EmployeeDeduction` (Entity)

```
EmployeeDeduction
├── id            : Long
├── employeeId    : Long
├── name          : String
├── type          : DeductionType
├── value         : BigDecimal
├── effectiveFrom : LocalDate
├── effectiveTo   : LocalDate
└── loanId        : Long             ← nullable — links deduction to loan repayment
```

### `Qualification` (Entity)

```
Qualification
├── id          : Long
├── employeeId  : Long
├── degree      : String
├── institution : String
└── yearAwarded : Integer
```

### `Infraction` (Aggregate Root)

```
Infraction
├── id              : Long
├── employeeId      : Long
├── organizationId  : Long
├── description     : String
├── severity        : InfractionSeverity   ← LOW | MEDIUM | HIGH
├── reportedAt      : ZonedDateTime
├── reportedBy      : Long
├── status          : InfractionStatus     ← OPEN | RESOLVED
└── resolutionNotes : String               ← nullable
```

**Business methods:**
- `resolve(notes)` → OPEN → RESOLVED; registers `InfractionResolvedEvent`

---

### Value Objects — `com.atlashub.hr.staff.domain.valueobject`

| Value Object | Fields | Notes |
|---|---|---|
| `EmailAddress` | `value: String` | Validated format |
| `PhoneNumber` | `value: String`, `countryCode: String` | Validated |
| `Money` | `amount: BigDecimal`, `currency: String` | Immutable |
| `EmployeeNumber` | `value: String` | e.g., `"EMP-0042"` |

---

### Domain Events — `com.atlashub.hr.staff.domain.events`

| Event | Published When | Consumed By |
|---|---|---|
| `EmployeeOnboardedEvent` | Employee created | `notifications` (welcome + onboarding checklist) |
| `EmployeeProfileUpdatedEvent` | Bio-data updated | `audit` |
| `EmployeePromotedEvent` | Promotion processed | `notifications` |
| `EmployeeSuspendedEvent` | Suspended | `iam` (revoke org access), `notifications` |
| `EmployeeTerminatedEvent` | Terminated | `pay` (close mandates), `accounting`, `notifications` |
| `InfractionLoggedEvent` | Infraction recorded | `notifications` |
| `InfractionResolvedEvent` | Infraction resolved | `notifications` |

---

### Domain Exceptions — `com.atlashub.hr.staff.domain.exceptions`

```java
public class EmployeeNotFoundException extends NotFoundException {
    public EmployeeNotFoundException(Long id) { super("Employee not found: " + id); }
}
public class UserAlreadyEmployedException extends ConflictException {
    public UserAlreadyEmployedException() { super("User is already employed in this organization"); }
}
public class EmployeeBankNotFoundException extends NotFoundException {
    public EmployeeBankNotFoundException(Long id) { super("Employee bank not found: " + id); }
}
public class EmployeeBankUnverifiedException extends BusinessRuleException {
    public EmployeeBankUnverifiedException() { super("Employee bank account has not been verified"); }
}
public class SalaryGradeNotFoundException extends NotFoundException {
    public SalaryGradeNotFoundException(Long id) { super("Salary grade not found: " + id); }
}
public class InvalidEmployeeStateException extends BusinessRuleException {
    public InvalidEmployeeStateException(String message) { super(message); }
}
public class EmployeeHasOutstandingLoanException extends BusinessRuleException {
    public EmployeeHasOutstandingLoanException() {
        super("Employee has an outstanding loan balance and cannot be terminated");
    }
}
public class InfractionNotFoundException extends NotFoundException {
    public InfractionNotFoundException(Long id) { super("Infraction not found: " + id); }
}
```

### Application Ports — `com.atlashub.hr.staff.application.port`

| Port | Responsibility |
|---|---|
| `BankVerificationPort` | Verify bank account via Paystack name enquiry |
| `EntitlementQueryPort` | Check if org has HR subscribed |
| `MembershipQueryPort` | Count members with a given permission |

---

## Application Layer

### Commands — `com.atlashub.hr.staff.application.commands`

#### `OnboardEmployeeCommand`
```java
record OnboardEmployeeCommand(
    Long organizationId, Long userId, String firstName, String lastName,
    String email, String phone, LocalDate dateOfBirth, String address,
    String nationality, LocalDate hireDate, String designation,
    String department, Long reportingManagerId, Long salaryGradeId, Money baseSalary
)
```
**Handler:** `OnboardEmployeeHandler` | **Response:** `OnboardEmployeeResponse(Long employeeId, String employeeNumber)`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:employees:onboard')")`  
**Flow:**
1. Check `existsByOrganizationIdAndUserId(orgId, userId)` → `UserAlreadyEmployedException`
2. Load `SalaryGrade` → `SalaryGradeNotFoundException`
3. Generate `employeeNumber` via sequence
4. Create `Employee` (status = PROBATION)
5. `repository.save(employee)` → publishes `EmployeeOnboardedEvent`

---

#### `UpdateEmployeeProfileCommand`
```java
record UpdateEmployeeProfileCommand(
    Long employeeId, String firstName, String lastName,
    String phone, String address, String nationality
)
```
**Handler:** `UpdateEmployeeProfileHandler` | **Response:** `void`  
**Flow:** Load `Employee` → `employee.updateBioData(...)` → `repository.save()`

---

#### `PromoteEmployeeCommand`
```java
record PromoteEmployeeCommand(Long employeeId, String newDesignation, Long newGradeId, Money newBaseSalary)
```
**Handler:** `PromoteEmployeeHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:employees:manage')")`  
**Flow:** Load `Employee` + `SalaryGrade` → `employee.promote(...)` → `repository.save()`

---

#### `SuspendEmployeeCommand`
```java
record SuspendEmployeeCommand(Long employeeId, String reason)
```
**Handler:** `SuspendEmployeeHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:employees:manage')")`

---

#### `ReinstateEmployeeCommand`
```java
record ReinstateEmployeeCommand(Long employeeId)
```
**Handler:** `ReinstateEmployeeHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:employees:manage')")`

---

#### `TerminateEmployeeCommand`
```java
record TerminateEmployeeCommand(Long employeeId, LocalDate terminationDate)
```
**Handler:** `TerminateEmployeeHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:employees:manage')")`  
**Flow:** Load employee → check no active loan → `employee.terminate(date)` → `repository.save()`

---

#### `AddEmployeeBankCommand`
```java
record AddEmployeeBankCommand(Long employeeId, String bankCode, String accountNumber, boolean setPrimary)
```
**Handler:** `AddEmployeeBankHandler` | **Response:** `AddEmployeeBankResponse(Long bankId, String accountName)`  
**Flow:** Call `BankVerificationPort.verify(bankCode, accountNumber)` → create `EmployeeBank` (isVerified=true) → if `setPrimary`, mark existing primary as non-primary → `repository.save()`

---

#### `LogInfractionCommand`
```java
record LogInfractionCommand(Long employeeId, Long organizationId, String description,
                            InfractionSeverity severity, Long reportedBy)
```
**Handler:** `LogInfractionHandler` | **Response:** `LogInfractionResponse(Long infractionId)`  
**Flow:** Create `Infraction` → `repository.save()` → publishes `InfractionLoggedEvent`; if severity == `HIGH`, also invoke `SuspendEmployeeHandler`

---

#### `ResolveInfractionCommand`
```java
record ResolveInfractionCommand(Long infractionId, String resolutionNotes)
```
**Handler:** `ResolveInfractionHandler` | **Response:** `void`  
**Flow:** Load `Infraction` → `infraction.resolve(notes)` → `repository.save()`

---

#### `CreateSalaryGradeCommand`
```java
record CreateSalaryGradeCommand(Long organizationId, String name, Money baseSalary)
```
**Handler:** `CreateSalaryGradeHandler` | **Response:** `CreateSalaryGradeResponse(Long gradeId)`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:employees:manage')")`

---

#### `AddGradeAllowanceCommand`
```java
record AddGradeAllowanceCommand(Long gradeId, String name, AllowanceType type, BigDecimal value)
```
**Handler:** `AddGradeAllowanceHandler` | **Response:** `void`

---

#### `AddGradeDeductionCommand`
```java
record AddGradeDeductionCommand(Long gradeId, String name, DeductionType type, BigDecimal value, boolean isStatutory)
```
**Handler:** `AddGradeDeductionHandler` | **Response:** `void`

---

#### `AddEmployeeAllowanceCommand`
```java
record AddEmployeeAllowanceCommand(Long employeeId, String name, AllowanceType type,
                                   BigDecimal value, LocalDate effectiveFrom, LocalDate effectiveTo)
```
**Handler:** `AddEmployeeAllowanceHandler` | **Response:** `void`

---

#### `AddEmployeeDeductionCommand`
```java
record AddEmployeeDeductionCommand(Long employeeId, String name, DeductionType type,
                                   BigDecimal value, LocalDate effectiveFrom, LocalDate effectiveTo)
```
**Handler:** `AddEmployeeDeductionHandler` | **Response:** `void`

---

### Queries — `com.atlashub.hr.staff.application.queries`

#### `ListEmployeesQuery`
```java
record ListEmployeesQuery(Long organizationId, EmployeeStatus status, String department, int page, int size)
```
**Handler:** `ListEmployeesHandler` | **Result:** `PageResult<EmployeeResult>`  
**Justification:** Orgs can have thousands of employees — pagination required.

`EmployeeResult`: `id`, `employeeNumber`, `firstName`, `lastName`, `email`, `designation`, `department`, `status`, `hireDate`

---

#### `GetEmployeeDetailsQuery`
```java
record GetEmployeeDetailsQuery(Long employeeId)
```
**Handler:** `GetEmployeeDetailsHandler` | **Result:** `EmployeeDetailsResult`

`EmployeeDetailsResult`: all `Employee` fields + `List<QualificationResult>` + `List<EmployeeBankResult>` + `List<EmployeeAllowanceResult>` + `List<EmployeeDeductionResult>`

---

#### `ListSalaryGradesQuery`
```java
record ListSalaryGradesQuery(Long organizationId)
```
**Handler:** `ListSalaryGradesHandler` | **Result:** `List<SalaryGradeResult>`  
**Justification:** Bounded — few dozen grades per org max; no pagination needed.

---

## Infrastructure Layer

### Persistence

**JPA Entities** (`infrastructure/persistence/entities/`):

| Entity | Table | Locking |
|---|---|---|
| `EmployeeJpaEntity` | `hr_employees` | `@Version` optimistic |
| `EmployeeBankJpaEntity` | `hr_employee_banks` | — |
| `SalaryGradeJpaEntity` | `hr_salary_grades` | — |
| `GradeAllowanceJpaEntity` | `hr_grade_allowances` | — |
| `GradeDeductionJpaEntity` | `hr_grade_deductions` | — |
| `EmployeeAllowanceJpaEntity` | `hr_employee_allowances` | — |
| `EmployeeDeductionJpaEntity` | `hr_employee_deductions` | `@Lock(PESSIMISTIC_WRITE)` during payroll initiation |
| `QualificationJpaEntity` | `hr_employee_qualifications` | — |
| `InfractionJpaEntity` | `hr_infractions` | `@Version` optimistic |

**Spring Data Repositories** (`infrastructure/persistence/repositories/`):

```
EmployeeJpaRepository
  + findByOrganizationIdAndStatus(Long orgId, EmployeeStatus status): List<EmployeeJpaEntity>
  + findByOrganizationIdAndStatusIn(Long orgId, List<EmployeeStatus> statuses): List<EmployeeJpaEntity>
  + existsByOrganizationIdAndUserId(Long orgId, Long userId): boolean
  + findByOrganizationId(Long orgId, Pageable p): Page<EmployeeJpaEntity>

EmployeeBankJpaRepository
  + findByEmployeeId(Long employeeId): List<EmployeeBankJpaEntity>
  + findByEmployeeIdAndIsPrimaryTrue(Long employeeId): Optional<EmployeeBankJpaEntity>

SalaryGradeJpaRepository
  + findByOrganizationId(Long orgId): List<SalaryGradeJpaEntity>

InfractionJpaRepository
  + findByEmployeeId(Long employeeId): List<InfractionJpaEntity>
```

**Mappers** (`infrastructure/persistence/mappers/`):
- `EmployeeMapper` — `Employee ↔ EmployeeJpaEntity`
- `SalaryGradeMapper` — `SalaryGrade ↔ SalaryGradeJpaEntity` (includes allowances, deductions)
- `InfractionMapper` — `Infraction ↔ InfractionJpaEntity`

**Repository Adapters** (`infrastructure/persistence/adapters/`):
- `EmployeeRepositoryAdapter` → sequence: `hr_employee_seq`
- `SalaryGradeRepositoryAdapter` → sequence: `hr_salary_grade_seq`
- `InfractionRepositoryAdapter` → sequence: `hr_infraction_seq`

### Listener — `infrastructure/messaging/listeners/`

#### `UserInvitationAcceptedListener`
| Attribute | Value |
|---|---|
| **Topic** | `iam-events` |
| **Group ID** | `hr-staff-group` |
| **Event consumed** | `UserInvitationAcceptedEvent` |
| **Payload** | `userId`, `organizationId`, `email`, `firstName`, `lastName` |
| **Command called** | `OnboardEmployeeCommand` |
| **Flow** | Checks `EntitlementQueryPort.isHrSubscribed(orgId)` → creates Employee in PROBATION |
| **Idempotency** | `existsByOrganizationIdAndUserId(orgId, userId)` check before creating |

### Infrastructure Services — `infrastructure/services/`

| Adapter | Implements | External |
|---|---|---|
| `PaystackBankVerificationAdapter` | `BankVerificationPort` | Paystack name enquiry API |
| `BillingEntitlementAdapter` | `EntitlementQueryPort` | `atlashub-billing` |
| `IamMembershipAdapter` | `MembershipQueryPort` | `atlashub-iam` |

---

## Presentation Layer

### Controller: `HrStaffController`

All endpoints under `/api/v1`. JWT required.

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/employees` | `hr:employees:onboard` | `OnboardEmployeeRequest` | `OnboardEmployeeResponse` |
| `GET` | `/employees` | — | `?orgId&status&department&page&size` | `PageResult<EmployeeResult>` |
| `GET` | `/employees/{id}` | — | — | `EmployeeDetailsResult` |
| `PUT` | `/employees/{id}` | — | `UpdateEmployeeProfileRequest` | `void` |
| `POST` | `/employees/{id}/promote` | `hr:employees:manage` | `PromoteEmployeeRequest` | `void` |
| `POST` | `/employees/{id}/suspend` | `hr:employees:manage` | `SuspendEmployeeRequest` | `void` |
| `POST` | `/employees/{id}/reinstate` | `hr:employees:manage` | — | `void` |
| `POST` | `/employees/{id}/terminate` | `hr:employees:manage` | `TerminateEmployeeRequest` | `void` |
| `POST` | `/employees/{id}/banks` | — | `AddEmployeeBankRequest` | `AddEmployeeBankResponse` |
| `POST` | `/employees/{id}/infractions` | — | `LogInfractionRequest` | `LogInfractionResponse` |
| `PUT` | `/infractions/{id}/resolve` | — | `ResolveInfractionRequest` | `void` |
| `POST` | `/salary-grades` | `hr:employees:manage` | `CreateSalaryGradeRequest` | `CreateSalaryGradeResponse` |
| `GET` | `/salary-grades` | — | `?orgId` | `List<SalaryGradeResult>` |
| `POST` | `/salary-grades/{id}/allowances` | `hr:employees:manage` | `AddGradeAllowanceRequest` | `void` |
| `POST` | `/salary-grades/{id}/deductions` | `hr:employees:manage` | `AddGradeDeductionRequest` | `void` |

### DTOs — `presentation/dto/`

**Request DTOs:**
- `OnboardEmployeeRequest`: `organizationId`, `userId`, `firstName`, `lastName`, `email`, `phone`, `dateOfBirth`, `address`, `nationality`, `hireDate`, `designation`, `department`, `reportingManagerId`, `salaryGradeId`, `baseSalary`
- `UpdateEmployeeProfileRequest`: `firstName`, `lastName`, `phone`, `address`, `nationality`
- `PromoteEmployeeRequest`: `newDesignation`, `newGradeId`, `newBaseSalary`
- `SuspendEmployeeRequest`: `reason`
- `TerminateEmployeeRequest`: `terminationDate`
- `AddEmployeeBankRequest`: `bankCode`, `accountNumber`, `setPrimary`
- `LogInfractionRequest`: `description`, `severity`
- `ResolveInfractionRequest`: `resolutionNotes`
- `CreateSalaryGradeRequest`: `organizationId`, `name`, `baseSalary`
- `AddGradeAllowanceRequest`: `name`, `type`, `value`
- `AddGradeDeductionRequest`: `name`, `type`, `value`, `isStatutory`

**Response DTOs:**
- `OnboardEmployeeResponse`: `employeeId`, `employeeNumber`
- `AddEmployeeBankResponse`: `bankId`, `accountName`
- `LogInfractionResponse`: `infractionId`
- `CreateSalaryGradeResponse`: `gradeId`
- `EmployeeResult`: `id`, `employeeNumber`, `firstName`, `lastName`, `email`, `designation`, `department`, `status`, `hireDate`
- `EmployeeDetailsResult`: full Employee + qualifications + banks + allowances + deductions
- `SalaryGradeResult`: `id`, `name`, `baseSalary`, `allowances`, `deductions`

---

## RBAC Table

| Permission | Granted To | Commands |
|---|---|---|
| `hr:employees:onboard` | HR Admin | `OnboardEmployeeHandler` |
| `hr:employees:manage` | HR Admin | `PromoteEmployeeHandler`, `SuspendEmployeeHandler`, `ReinstateEmployeeHandler`, `TerminateEmployeeHandler`, `CreateSalaryGradeHandler`, `CreateLeaveTypeHandler` |

---

## Distributed Architecture

### Locking
- `Employee` — Optimistic (`@Version`) for concurrent bio-data updates
- `EmployeeDeduction` list — Pessimistic Write during `InitiatePayrollHandler` (prevents concurrent additions changing payslip mid-calculation)
- `InfractionJpaEntity` — Optimistic (`@Version`)

### Outbox
- `EmployeeOnboardedEvent` — triggers welcome notification
- `EmployeeTerminatedEvent` — triggers IAM access revocation + accounting

### Inbox
- `UserInvitationAcceptedEvent` — guarded by `existsByOrganizationIdAndUserId` check

---

## Complete File List

```
atlashub-hr/src/main/java/com/atlashub/hr/staff/
├── domain/
│   ├── entities/
│   │   ├── Employee.java
│   │   ├── EmployeeBank.java
│   │   ├── SalaryGrade.java
│   │   ├── GradeAllowance.java
│   │   ├── GradeDeduction.java
│   │   ├── EmployeeAllowance.java
│   │   ├── EmployeeDeduction.java
│   │   ├── Qualification.java
│   │   └── Infraction.java
│   ├── events/
│   │   ├── EmployeeOnboardedEvent.java
│   │   ├── EmployeeProfileUpdatedEvent.java
│   │   ├── EmployeePromotedEvent.java
│   │   ├── EmployeeSuspendedEvent.java
│   │   ├── EmployeeTerminatedEvent.java
│   │   ├── InfractionLoggedEvent.java
│   │   └── InfractionResolvedEvent.java
│   ├── exceptions/
│   │   ├── EmployeeNotFoundException.java
│   │   ├── UserAlreadyEmployedException.java
│   │   ├── EmployeeBankNotFoundException.java
│   │   ├── EmployeeBankUnverifiedException.java
│   │   ├── SalaryGradeNotFoundException.java
│   │   ├── InvalidEmployeeStateException.java
│   │   ├── EmployeeHasOutstandingLoanException.java
│   │   └── InfractionNotFoundException.java
│   ├── repositories/
│   │   ├── EmployeeRepository.java
│   │   ├── EmployeeBankRepository.java
│   │   ├── SalaryGradeRepository.java
│   │   ├── EmployeeAllowanceRepository.java
│   │   ├── EmployeeDeductionRepository.java
│   │   └── InfractionRepository.java
│   └── valueobject/
│       ├── EmailAddress.java
│       ├── PhoneNumber.java
│       ├── Money.java
│       └── EmployeeNumber.java
├── application/
│   ├── commands/
│   │   ├── OnboardEmployee/
│   │   │   ├── OnboardEmployeeCommand.java
│   │   │   ├── OnboardEmployeeHandler.java
│   │   │   └── OnboardEmployeeResponse.java
│   │   ├── UpdateEmployeeProfile/
│   │   │   ├── UpdateEmployeeProfileCommand.java
│   │   │   └── UpdateEmployeeProfileHandler.java
│   │   ├── PromoteEmployee/
│   │   │   ├── PromoteEmployeeCommand.java
│   │   │   └── PromoteEmployeeHandler.java
│   │   ├── SuspendEmployee/
│   │   │   ├── SuspendEmployeeCommand.java
│   │   │   └── SuspendEmployeeHandler.java
│   │   ├── ReinstateEmployee/
│   │   │   ├── ReinstateEmployeeCommand.java
│   │   │   └── ReinstateEmployeeHandler.java
│   │   ├── TerminateEmployee/
│   │   │   ├── TerminateEmployeeCommand.java
│   │   │   └── TerminateEmployeeHandler.java
│   │   ├── AddEmployeeBank/
│   │   │   ├── AddEmployeeBankCommand.java
│   │   │   ├── AddEmployeeBankHandler.java
│   │   │   └── AddEmployeeBankResponse.java
│   │   ├── LogInfraction/
│   │   │   ├── LogInfractionCommand.java
│   │   │   ├── LogInfractionHandler.java
│   │   │   └── LogInfractionResponse.java
│   │   ├── ResolveInfraction/
│   │   │   ├── ResolveInfractionCommand.java
│   │   │   └── ResolveInfractionHandler.java
│   │   ├── CreateSalaryGrade/
│   │   │   ├── CreateSalaryGradeCommand.java
│   │   │   ├── CreateSalaryGradeHandler.java
│   │   │   └── CreateSalaryGradeResponse.java
│   │   ├── AddGradeAllowance/
│   │   │   ├── AddGradeAllowanceCommand.java
│   │   │   └── AddGradeAllowanceHandler.java
│   │   ├── AddGradeDeduction/
│   │   │   ├── AddGradeDeductionCommand.java
│   │   │   └── AddGradeDeductionHandler.java
│   │   ├── AddEmployeeAllowance/
│   │   │   ├── AddEmployeeAllowanceCommand.java
│   │   │   └── AddEmployeeAllowanceHandler.java
│   │   └── AddEmployeeDeduction/
│   │       ├── AddEmployeeDeductionCommand.java
│   │       └── AddEmployeeDeductionHandler.java
│   ├── queries/
│   │   ├── ListEmployees/
│   │   │   ├── ListEmployeesQuery.java
│   │   │   ├── ListEmployeesHandler.java
│   │   │   └── EmployeeResult.java
│   │   ├── GetEmployeeDetails/
│   │   │   ├── GetEmployeeDetailsQuery.java
│   │   │   ├── GetEmployeeDetailsHandler.java
│   │   │   └── EmployeeDetailsResult.java
│   │   └── ListSalaryGrades/
│   │       ├── ListSalaryGradesQuery.java
│   │       ├── ListSalaryGradesHandler.java
│   │       └── SalaryGradeResult.java
│   └── port/
│       ├── BankVerificationPort.java
│       ├── EntitlementQueryPort.java
│       └── MembershipQueryPort.java
├── infrastructure/
│   ├── messaging/
│   │   ├── events/
│   │   │   └── UserInvitationAcceptedPayload.java
│   │   └── listeners/
│   │       └── UserInvitationAcceptedListener.java
│   ├── persistence/
│   │   ├── adapters/
│   │   │   ├── EmployeeRepositoryAdapter.java
│   │   │   ├── SalaryGradeRepositoryAdapter.java
│   │   │   └── InfractionRepositoryAdapter.java
│   │   ├── entities/
│   │   │   ├── EmployeeJpaEntity.java
│   │   │   ├── EmployeeBankJpaEntity.java
│   │   │   ├── SalaryGradeJpaEntity.java
│   │   │   ├── GradeAllowanceJpaEntity.java
│   │   │   ├── GradeDeductionJpaEntity.java
│   │   │   ├── EmployeeAllowanceJpaEntity.java
│   │   │   ├── EmployeeDeductionJpaEntity.java
│   │   │   ├── QualificationJpaEntity.java
│   │   │   └── InfractionJpaEntity.java
│   │   ├── mappers/
│   │   │   ├── EmployeeMapper.java
│   │   │   ├── SalaryGradeMapper.java
│   │   │   └── InfractionMapper.java
│   │   └── repositories/
│   │       ├── EmployeeJpaRepository.java
│   │       ├── EmployeeBankJpaRepository.java
│   │       ├── SalaryGradeJpaRepository.java
│   │       ├── EmployeeAllowanceJpaRepository.java
│   │       ├── EmployeeDeductionJpaRepository.java
│   │       └── InfractionJpaRepository.java
│   └── services/
│       ├── PaystackBankVerificationAdapter.java
│       ├── BillingEntitlementAdapter.java
│       └── IamMembershipAdapter.java
└── presentation/
    ├── dto/
    │   ├── OnboardEmployeeRequest.java
    │   ├── OnboardEmployeeResponse.java
    │   ├── UpdateEmployeeProfileRequest.java
    │   ├── PromoteEmployeeRequest.java
    │   ├── SuspendEmployeeRequest.java
    │   ├── TerminateEmployeeRequest.java
    │   ├── AddEmployeeBankRequest.java
    │   ├── AddEmployeeBankResponse.java
    │   ├── LogInfractionRequest.java
    │   ├── LogInfractionResponse.java
    │   ├── ResolveInfractionRequest.java
    │   ├── CreateSalaryGradeRequest.java
    │   ├── CreateSalaryGradeResponse.java
    │   ├── AddGradeAllowanceRequest.java
    │   ├── AddGradeDeductionRequest.java
    │   ├── EmployeeResult.java
    │   ├── EmployeeDetailsResult.java
    │   └── SalaryGradeResult.java
    └── rest/
        └── HrStaffController.java
```
