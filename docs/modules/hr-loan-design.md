# HR Loan Design (`atlashub-hr` / `com.atlashub.hr.loan`)

## Role & Purpose

The `loan` subpackage manages employee salary advances and loans. Approved loans are disbursed via `atlashub-pay`. Repayments are automatically deducted from subsequent payroll runs via `EmployeeDeduction` entries in the staff subpackage. Self-approval is not allowed — the approver must be different from the applicant (enforced at the application level, not domain — loans are single-aggregate approve, no maker-checker needed beyond basic RBAC).

Gradle module: `atlashub-hr`  
Package: `com.atlashub.hr.loan`

---

## Domain Layer

### `EmployeeLoan` (Aggregate Root)

**Package:** `com.atlashub.hr.loan.domain.entities`

```
EmployeeLoan
├── id                 : Long
├── employeeId         : Long
├── organizationId     : Long
├── principalAmount    : Money
├── outstandingBalance : Money
├── monthlyDeduction   : Money
├── disbursedAt        : ZonedDateTime   ← nullable
├── status             : LoanStatus      ← PENDING_APPROVAL | APPROVED | ACTIVE | FULLY_REPAID | CANCELLED
├── approvedBy         : Long            ← nullable
└── startDate          : LocalDate       ← first repayment month; nullable until approved
```

**Creation rules:** `outstandingBalance = principalAmount` on creation. Initial status = `PENDING_APPROVAL`.

**State machine:**
```
PENDING_APPROVAL → APPROVED → ACTIVE → FULLY_REPAID
PENDING_APPROVAL → CANCELLED
```

**Business methods (on entity):**

| Method | Inputs | Guard | Events | Exceptions |
|---|---|---|---|---|
| `approve(approverId, startDate)` | Long, LocalDate | status == PENDING_APPROVAL | `LoanApprovedEvent` | `InvalidLoanStateException` |
| `disburse()` | — | status == APPROVED | — | `InvalidLoanStateException` |
| `recordRepayment(amount)` | Money | status == ACTIVE | — (if balance → 0: status = FULLY_REPAID) | `InvalidLoanStateException` |
| `cancel()` | — | status == PENDING_APPROVAL | — | `InvalidLoanStateException` |

**Why on entity:** All transitions are pure state changes on a single aggregate. `recordRepayment()` uses pessimistic locking at the DB level (not a domain concern) to prevent concurrent payroll runs corrupting the balance.

---

### Domain Events — `com.atlashub.hr.loan.domain.events`

| Event | Published When | Consumed By |
|---|---|---|
| `LoanApprovedEvent` | Loan approved | `pay` (disburse loan to employee bank), `accounting` (Dr Employee Loan Receivable, Cr Cash) |

---

### Domain Exceptions — `com.atlashub.hr.loan.domain.exceptions`

```java
public class LoanNotFoundException extends NotFoundException {
    public LoanNotFoundException(Long id) { super("Loan not found: " + id); }
}
public class LoanAlreadyActiveException extends ConflictException {
    public LoanAlreadyActiveException() { super("Employee already has an active loan"); }
}
public class EmployeeHasOutstandingLoanException extends BusinessRuleException {
    public EmployeeHasOutstandingLoanException() {
        super("Employee has an outstanding loan balance");
    }
}
public class InvalidLoanStateException extends BusinessRuleException {
    public InvalidLoanStateException(String message) { super(message); }
}
```

---

## Application Layer

### Commands — `com.atlashub.hr.loan.application.commands`

#### `ApplyForLoanCommand`
```java
record ApplyForLoanCommand(
    Long employeeId, Long organizationId,
    Money principalAmount, Money monthlyDeduction
)
```
**Handler:** `ApplyForLoanHandler` | **Response:** `ApplyForLoanResponse(Long loanId)`  
**Flow:**
1. Check employee has no existing `ACTIVE` or `APPROVED` loan → `LoanAlreadyActiveException`
2. Create `EmployeeLoan` (status = `PENDING_APPROVAL`, `outstandingBalance = principalAmount`)
3. `repository.save(loan)`

---

#### `ApproveLoanCommand`
```java
record ApproveLoanCommand(Long loanId, Long approverId, LocalDate startDate)
```
**Handler:** `ApproveLoanHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('hr:loans:approve')")`  
**Flow:**
1. Load `EmployeeLoan` → `LoanNotFoundException`
2. `loan.approve(approverId, startDate)` → `InvalidLoanStateException`
3. `repository.save(loan)` → publishes `LoanApprovedEvent`
4. `LoanApprovedEvent` triggers `pay` to disburse funds + creates `EmployeeDeduction` for monthly repayment

---

#### `CancelLoanCommand`
```java
record CancelLoanCommand(Long loanId)
```
**Handler:** `CancelLoanHandler` | **Response:** `void`  
**Flow:** Load `EmployeeLoan` → `loan.cancel()` → `repository.save(loan)`

---

#### `RecordLoanRepaymentCommand`
```java
record RecordLoanRepaymentCommand(Long loanId, Money amount)
```
**Handler:** `RecordLoanRepaymentHandler` | **Response:** `void`  
**Invocation source:** Called by `InitiatePayrollHandler` during payroll computation for each employee with an active loan  
**Flow:**
1. Load `EmployeeLoan` with **pessimistic lock** (`PESSIMISTIC_WRITE`) on `outstandingBalance`
2. `loan.recordRepayment(amount)` — reduces balance; if balance reaches zero → `FULLY_REPAID`
3. `repository.save(loan)` — if `FULLY_REPAID`, removes corresponding `EmployeeDeduction` entry

---

### Queries — `com.atlashub.hr.loan.application.queries`

#### `ListLoansQuery`
```java
record ListLoansQuery(Long organizationId, Long employeeId, LoanStatus status)
```
**Handler:** `ListLoansHandler` | **Result:** `List<LoanResult>`  
**Justification:** Bounded per org — employees rarely hold many loans simultaneously; no pagination needed.

`LoanResult`:
```java
record LoanResult(
    Long id, Long employeeId, Money principalAmount,
    Money outstandingBalance, Money monthlyDeduction,
    LoanStatus status, Long approvedBy, LocalDate startDate, ZonedDateTime disbursedAt
)
```

---

## Infrastructure Layer

### Persistence

**JPA Entity:**

| Entity | Table | Locking |
|---|---|---|
| `EmployeeLoanJpaEntity` | `hr_employee_loans` | `@Lock(PESSIMISTIC_WRITE)` on `recordRepayment()` |

**Spring Data Repository:**

```
EmployeeLoanJpaRepository
  + findByEmployeeIdAndStatus(Long employeeId, LoanStatus status): List<EmployeeLoanJpaEntity>
  + findByEmployeeIdAndStatusIn(Long employeeId, List<LoanStatus> statuses): List<EmployeeLoanJpaEntity>
  + findByIdWithPessimisticLock(Long loanId): Optional<EmployeeLoanJpaEntity>   // @Lock(PESSIMISTIC_WRITE)
```

**Mapper:**
- `EmployeeLoanMapper` — `EmployeeLoan ↔ EmployeeLoanJpaEntity`

**Repository Adapter:**
- `EmployeeLoanRepositoryAdapter` → sequence: `hr_employee_loan_seq`

---

### Kafka Listeners — `infrastructure/messaging/listeners/`

#### `LoanDisbursementCompletedListener`

| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `hr-loan-payout-completed` |
| **Event** | `PayoutCompletedEvent` |
| **Filter** | Only processes events where `sourceSystem == "LOAN_DISBURSEMENT"` |
| **Payload fields** | `payoutId`, `organizationId`, `sourceSystem`, `sourceReferenceId` (= loanId), `amount`, `currency`, `completedAt` |
| **Command called** | `MarkLoanDisbursedHandler` |
| **Flow** | Extracts `loanId` from `sourceReferenceId`. Loads `EmployeeLoan`. Calls `loan.markDisbursed()`. Saves. Idempotent — checks if loan already in `ACTIVE` status before acting. |

---

#### `EmployeeTerminatedListener`

| Attribute | Value |
|---|---|
| **Topic** | `hr-events` |
| **Group ID** | `hr-loan-employee-terminated` |
| **Event** | `EmployeeTerminatedEvent` |
| **Payload fields** | `employeeId`, `organizationId`, `terminatedAt` |
| **Command called** | None directly — queries active loans for the employee and flags them as `TERMINATION_PENDING` |
| **Flow** | Marks all `ACTIVE` loans for the terminated employee as `TERMINATION_PENDING`. HR admin is notified to decide: final salary deduction in full, or write-off. This does not auto-close the loan. |

---


### Controller: `HrLoanController`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/loans/apply` | — | `ApplyForLoanRequest` | `ApplyForLoanResponse` |
| `GET` | `/loans` | — | `?orgId&employeeId&status` | `List<LoanResult>` |
| `POST` | `/loans/{id}/approve` | `hr:loans:approve` | `ApproveLoanRequest` | `void` |
| `POST` | `/loans/{id}/cancel` | — | — | `void` |

### DTOs

- `ApplyForLoanRequest`: `employeeId`, `organizationId`, `principalAmount`, `monthlyDeduction`
- `ApplyForLoanResponse`: `loanId`
- `ApproveLoanRequest`: `startDate`
- `LoanResult`: (see query result above)

---

## RBAC Table

| Permission | Commands |
|---|---|
| `hr:loans:approve` | `ApproveLoanHandler` |

---

## Distributed Architecture

### Locking
- `EmployeeLoan.outstandingBalance` — **Pessimistic Write** during `RecordLoanRepaymentHandler` to prevent concurrent payroll runs corrupting the balance

### Outbox
- `LoanApprovedEvent` — triggers pay disbursement; this event must not be lost as it moves money

### Inbox
- No external events consumed by loan subpackage directly

---

## Complete File List

```
atlashub-hr/src/main/java/com/atlashub/hr/loan/
├── domain/
│   ├── entities/
│   │   └── EmployeeLoan.java
│   ├── events/
│   │   └── LoanApprovedEvent.java
│   ├── exceptions/
│   │   ├── LoanNotFoundException.java
│   │   ├── LoanAlreadyActiveException.java
│   │   ├── EmployeeHasOutstandingLoanException.java
│   │   └── InvalidLoanStateException.java
│   ├── repositories/
│   │   └── EmployeeLoanRepository.java
│   └── valueobject/
│       └── LoanStatus.java
├── application/
│   ├── commands/
│   │   ├── ApplyForLoan/
│   │   │   ├── ApplyForLoanCommand.java
│   │   │   ├── ApplyForLoanHandler.java
│   │   │   └── ApplyForLoanResponse.java
│   │   ├── ApproveLoan/
│   │   │   ├── ApproveLoanCommand.java
│   │   │   └── ApproveLoanHandler.java
│   │   ├── CancelLoan/
│   │   │   ├── CancelLoanCommand.java
│   │   │   └── CancelLoanHandler.java
│   │   └── RecordLoanRepayment/
│   │       ├── RecordLoanRepaymentCommand.java
│   │       └── RecordLoanRepaymentHandler.java
│   └── queries/
│       └── ListLoans/
│           ├── ListLoansQuery.java
│           ├── ListLoansHandler.java
│           └── LoanResult.java
├── infrastructure/
│   ├── persistence/
│   │   ├── adapters/
│   │   │   └── EmployeeLoanRepositoryAdapter.java
│   │   ├── entities/
│   │   │   └── EmployeeLoanJpaEntity.java
│   │   ├── mappers/
│   │   │   └── EmployeeLoanMapper.java
│   │   └── repositories/
│   │       └── EmployeeLoanJpaRepository.java
└── presentation/
    ├── dto/
    │   ├── ApplyForLoanRequest.java
    │   ├── ApplyForLoanResponse.java
    │   ├── ApproveLoanRequest.java
    │   └── LoanResult.java
    └── rest/
        └── HrLoanController.java
```
