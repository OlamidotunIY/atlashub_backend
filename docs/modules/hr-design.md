# HR & Payroll Module Design (`atlashub-hr`)

## Role & Purpose

The HR module is the **people operations engine** of AtlasHub. It manages the complete employment lifecycle within an organization — from onboarding a new hire to disbursing their monthly salary, managing their leave, and handling their resignation.

HR is one of the most financially consequential modules because payroll disbursement directly triggers bulk money movement through `atlashub-pay`. This is enforced by a strict **Maker-Checker** workflow: one authorized user initiates the payroll run (maker), and a different authorized user with `hr:payroll:approve` permission must approve it before any money moves (checker).

An organization using Atlas HR can:
- Maintain a complete employee registry with bio-data, qualifications, and employment history
- Define salary grades and allowance/deduction structures (organizations configure their own rules — no auto-calculated Nigerian tax; they enter what they want deducted)
- Run monthly payroll with full maker-checker approval workflow
- Manage leave types, applications, and balances
- Track attendance with clock-in/clock-out and overtime calculation
- Disburse employee loans with installment repayments deducted automatically from payroll
- Handle disciplinary infractions with resolution tracking

---

## 1. Features

### Employee Onboarding
When a new employee joins:
- Directly added via `OnboardEmployeeUseCase` by an HR admin
- Or auto-drafted when `InvitationAcceptedEvent` fires (if the org has HR subscribed)

The `Employee` aggregate holds complete bio-data, designation, department, and salary grade reference. Status covers the full employment lifecycle: PROBATION → ACTIVE → ON_LEAVE → SUSPENDED → TERMINATED.

### Salary Grades (Configurable)
Organizations define `SalaryGrade`s (e.g., "Grade 7 — Senior Engineer, ₦450,000 base"). Each grade includes configurable `GradeAllowance` entries (housing, transport — FIXED or PERCENTAGE_OF_GROSS) and `GradeDeduction` entries (PAYE, pension, NHF — organizations enter the values themselves).

Individual employees can have additional `EmployeeAllowance` and `EmployeeDeduction` overrides (e.g., loan repayment, performance bonus).

### Payroll Run (Maker-Checker)
The payroll process enforces the four-eyes principle:

```
1. INITIATE (Maker — requires hr:payroll:initiate permission)
   InitiatePayrollUseCase fetches all ACTIVE/PROBATION employees,
   calculates gross from grade + overrides, applies all deductions,
   generates Payslip per employee → PayrollRun created in DRAFT

2. SUBMIT FOR APPROVAL (Maker)
   Maker submits → PayrollRun transitions to PENDING_APPROVAL
   Approvers are notified via WebSocket + email

3. APPROVE (Checker — requires hr:payroll:approve, MUST be a different user)
   ApprovePayrollUseCase validates checker ≠ initiator
   PayrollRun transitions to APPROVED
   Statutory deduction totals posted to Tax Holding Account in pay:ledger

4. DISBURSE (System — automatic after approval)
   DisbursePayrollUseCase validates Payroll Reserve Account has sufficient funds
   Calls pay to execute bulk payouts to all employee bank accounts
   PayrollRun transitions to PROCESSING → DISBURSED (on success)
   On partial failure: revert to APPROVED so admin can retry after investigation
```

### Leave Management
Organizations define `LeaveType`s (Annual Leave, Sick Leave, Maternity Leave) with maximum days per year. `LeaveBalance` tracks each employee's entitlement, used days, and remaining days. Approval is optional per leave type — some orgs auto-approve sick leave.

### Attendance Tracking
Clock-in/clock-out via `ClockInUseCase` / `ClockOutUseCase`. Clock-out calculates `hoursWorked` and `overtimeHours` based on configured shift hours. Used in payroll if the org uses attendance-based pay.

### Employee Loans
Employees apply for salary advances or loans. Approved loans are disbursed via `pay`. Repayments are automatically added as `EmployeeDeduction` entries for subsequent payroll runs, reducing `outstandingBalance` each month.

### Disciplinary Infractions
Managers log `Infraction` records (LOW, MEDIUM, HIGH severity). HIGH-severity infractions can trigger `SuspendEmployeeUseCase`. All infractions can be resolved with resolution notes.

---

## 2. Domain Entities & Aggregates

### `staff` Submodule

**`Employee` (Aggregate Root)**
```
Employee
├── id: Long
├── organizationId: Long
├── userId: Long
├── employeeNumber: String              ← auto-generated, e.g., "EMP-0042"
├── firstName: String
├── lastName: String
├── email: EmailAddress
├── phone: PhoneNumber
├── dateOfBirth: LocalDate
├── address: String
├── nationality: String
├── hireDate: LocalDate
├── terminationDate: LocalDate          ← nullable
├── designation: String
├── department: String
├── reportingManagerId: Long            ← nullable
├── salaryGradeId: Long
├── baseSalary: Money                   ← can override grade base salary
├── status: EmployeeStatus             ← PROBATION, ACTIVE, ON_LEAVE, SUSPENDED, TERMINATED
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `updateBioData(...)` → registers `EmployeeProfileUpdatedEvent`
- `promote(String newDesignation, Long newGradeId, Money newBaseSalary)` → registers `EmployeePromotedEvent`
- `suspend(String reason)` → transitions to SUSPENDED → registers `EmployeeSuspendedEvent`
- `reinstate()` → transitions to ACTIVE
- `terminate(LocalDate date)` → transitions to TERMINATED → registers `EmployeeTerminatedEvent`
- `placeOnLeave()`, `returnFromLeave()`

**Domain Rules:**
- An employee cannot be terminated while they have an ACTIVE loan with outstanding balance (must be settled or transferred first)
- A TERMINATED employee's payslips are read-only — no modifications

---

**`Qualification` (Entity)**: `id`, `employeeId`, `degree`, `institution`, `yearAwarded`

**`EmployeeBank` (Aggregate Root)**
```
EmployeeBank
├── id: Long
├── employeeId: Long
├── bankCode: String
├── bankName: String
├── accountNumber: String
├── accountName: String                 ← verified via Paystack name enquiry
├── isVerified: Boolean
├── isPrimary: Boolean                  ← which account to use for salary payment
└── addedAt: ZonedDateTime
```

**`SalaryGrade` (Aggregate Root)**
```
SalaryGrade
├── id: Long
├── organizationId: Long
├── name: String                        ← e.g., "Grade 7 — Senior Engineer"
├── baseSalary: Money
├── allowances: List<GradeAllowance>
└── deductions: List<GradeDeduction>
```

**`GradeAllowance` (Entity)**: `id`, `gradeId`, `name` (e.g., "Housing"), `type` (FIXED, PERCENTAGE_OF_GROSS), `value: BigDecimal`

**`GradeDeduction` (Entity)**: `id`, `gradeId`, `name` (e.g., "PAYE Tax"), `type` (FIXED, PERCENTAGE_OF_GROSS), `value: BigDecimal`, `isStatutory: Boolean`

**`EmployeeAllowance` (Entity)**: `id`, `employeeId`, `name`, `type`, `value`, `effectiveFrom: LocalDate`, `effectiveTo: LocalDate`

**`EmployeeDeduction` (Entity)**: `id`, `employeeId`, `name`, `type`, `value`, `effectiveFrom: LocalDate`, `effectiveTo: LocalDate`, `loanId: Long` (nullable — links loan repayment deductions)

**`Infraction` (Aggregate Root)**: `id`, `employeeId`, `organizationId`, `description`, `severity: InfractionSeverity` (LOW, MEDIUM, HIGH), `reportedAt`, `reportedBy: Long`, `status` (OPEN, RESOLVED), `resolutionNotes: String`

---

### `leave` Submodule

**`LeaveType` (Aggregate Root)**: `id`, `organizationId`, `name`, `maxDaysPerYear: Integer`, `isPaid: Boolean`, `requiresApproval: Boolean`, `carryOverAllowed: Boolean`, `maxCarryOverDays: Integer`

**`LeaveApplication` (Aggregate Root)**
```
LeaveApplication
├── id: Long
├── employeeId: Long
├── organizationId: Long
├── leaveTypeId: Long
├── startDate: LocalDate
├── endDate: LocalDate
├── daysRequested: Integer
├── reason: String
├── status: LeaveStatus               ← PENDING, APPROVED, REJECTED, CANCELLED
├── approvedBy: Long                  ← nullable
└── approvedAt: ZonedDateTime         ← nullable
```

**Business Methods:**
- `approve(Long approverId)` → pessimistic lock on `LeaveBalance` → deducts days → registers `LeaveApprovedEvent`
- `reject(String reason)` → registers `LeaveRejectedEvent`
- `cancel()` → restores balance if previously approved

**`LeaveBalance` (Entity)**
```
LeaveBalance
├── id: Long
├── employeeId: Long
├── leaveTypeId: Long
├── year: Integer
├── entitlement: Integer
├── used: Integer
└── remaining: Integer
```

**Business Methods:**
- `deduct(int days)` → guards: remaining ≥ days
- `restore(int days)` → called on cancellation or rejection after approval

---

### `attendance` Submodule

**`AttendanceRecord` (Aggregate Root)**
```
AttendanceRecord
├── id: Long
├── employeeId: Long
├── organizationId: Long
├── date: LocalDate
├── clockIn: ZonedDateTime            ← nullable
├── clockOut: ZonedDateTime           ← nullable
├── hoursWorked: BigDecimal           ← calculated on clock-out
├── overtimeHours: BigDecimal         ← hours beyond configured shift
└── status: AttendanceStatus          ← PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE
```

---

### `loan` Submodule

**`EmployeeLoan` (Aggregate Root)**
```
EmployeeLoan
├── id: Long
├── employeeId: Long
├── organizationId: Long
├── principalAmount: Money
├── outstandingBalance: Money
├── monthlyDeduction: Money
├── disbursedAt: ZonedDateTime        ← nullable
├── status: LoanStatus                ← PENDING_APPROVAL, APPROVED, ACTIVE, FULLY_REPAID, CANCELLED
├── approvedBy: Long                  ← nullable
└── startDate: LocalDate              ← first repayment month
```

**Business Methods:**
- `approve(Long approverId, LocalDate startDate)` → registers `LoanApprovedEvent`
- `disburse()` → transitions APPROVED → ACTIVE; creates `EmployeeDeduction` entries in payroll
- `recordRepayment(Money amount)` → pessimistic lock on `outstandingBalance`; if balance reaches zero, transitions to FULLY_REPAID; removes `EmployeeDeduction`
- `cancel()` → only if PENDING_APPROVAL

---

### `payroll` Submodule

**`PayrollRun` (Aggregate Root)**
```
PayrollRun
├── id: Long
├── organizationId: Long
├── period: YearMonth                 ← e.g., 2026-09
├── payslips: List<Payslip>
├── totalGross: Money
├── totalDeductions: Money
├── totalNet: Money
├── status: PayrollStatus             ← DRAFT, PENDING_APPROVAL, APPROVED, PROCESSING, DISBURSED, FAILED
├── initiatedBy: Long                 ← userId (maker)
├── approvedBy: Long                  ← userId (checker), nullable
├── approvedAt: ZonedDateTime         ← nullable
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `submitForApproval(Long initiatorId)` → transitions DRAFT → PENDING_APPROVAL
- `approve(Long approverId)` → **validates approverId ≠ initiatedBy** (maker ≠ checker); transitions → APPROVED → registers `PayrollApprovedEvent`
- `markProcessing()` → APPROVED → PROCESSING
- `markDisbursed()` → PROCESSING → DISBURSED → registers `PayrollDisbursedEvent`
- `revertToApproved(String reason)` → PROCESSING → APPROVED (for retry after payout failure)
- `markFailed(String reason)` → called on unrecoverable failure

**Domain Rule (Maker-Checker):**
The check `approverId ≠ initiatedBy` is enforced in the domain method, not just the controller. Even if the authorization layer is bypassed, the domain will reject self-approval.

---

**`Payslip` (Entity)**
```
Payslip
├── id: Long
├── payrollRunId: Long
├── employeeId: Long
├── grossPay: Money
├── totalAllowances: Money
├── totalDeductions: Money
├── netPay: Money
├── breakdown: List<PayslipLineItem>  ← each allowance and deduction as a line
└── status: PayslipStatus            ← DRAFT, DISBURSED, FAILED
```

**`PayslipLineItem` (Entity)**: `id`, `payslipId`, `name`, `type` (ALLOWANCE, DEDUCTION), `amount: Money`

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `EmployeeOnboardedEvent` | New employee created | `notifications` (welcome email + onboarding checklist) |
| `EmployeeTerminatedEvent` | Employee terminated | `pay` (close active mandates), `accounting`, `notifications` |
| `EmployeePromotedEvent` | Promotion processed | `notifications` (email employee) |
| `EmployeeSuspendedEvent` | Suspended | `iam` (revoke access to org if needed), `notifications` |
| `InfractionLoggedEvent` | Infraction recorded | `notifications` (notify employee and reporting manager) |
| `LeaveApprovedEvent` | Leave approved | `notifications` (email employee), `attendance` (mark as ON_LEAVE for those dates) |
| `LeaveRejectedEvent` | Leave rejected | `notifications` (email employee with reason) |
| `LoanApprovedEvent` | Loan approved | `pay` (disburse loan amount to employee bank), `accounting` (Debit Loan Receivable, Credit Bank) |
| `PayrollApprovedEvent` | Payroll approved by checker | `pay` (execute bulk payouts), `accounting` (Debit Salary Expense, Credit Payroll Payable) |
| `PayrollDisbursedEvent` | All salaries paid | `accounting` (Debit Payroll Payable, Credit Payroll Reserve Account), `notifications` (SMS each employee) |

---

## 4. Exceptions & Errors

**`HrErrorCode`**:
- `EMPLOYEE_NOT_FOUND`, `USER_ALREADY_EMPLOYED`
- `EMPLOYEE_BANK_NOT_FOUND`, `EMPLOYEE_BANK_UNVERIFIED`
- `SALARY_GRADE_NOT_FOUND`
- `INVALID_PAYROLL_STATE`, `PAYROLL_RUN_NOT_FOUND`
- `SELF_APPROVAL_NOT_ALLOWED` — maker cannot be checker
- `INSUFFICIENT_PAYROLL_RESERVE` — not enough funds in Payroll Reserve Account
- `LEAVE_TYPE_NOT_FOUND`, `INSUFFICIENT_LEAVE_BALANCE`, `LEAVE_DATES_CONFLICT`
- `INVALID_LEAVE_STATE`
- `LOAN_NOT_FOUND`, `LOAN_ALREADY_ACTIVE`
- `EMPLOYEE_HAS_OUTSTANDING_LOAN` — cannot terminate until settled
- `INFRACTION_NOT_FOUND`

---

## 5. Commands & Use Cases

### Staff
- `OnboardEmployeeCommand(orgId, userId, bio, designation, department, salaryGradeId, baseSalary)` → `OnboardEmployeeUseCase`
- `UpdateEmployeeProfileCommand(...)` → `UpdateEmployeeProfileUseCase`
- `PromoteEmployeeCommand(employeeId, newDesignation, newGradeId, newBaseSalary)` → `PromoteEmployeeUseCase`
- `SuspendEmployeeCommand(employeeId, reason)` → `SuspendEmployeeUseCase`
- `TerminateEmployeeCommand(employeeId, terminationDate)` → `TerminateEmployeeUseCase`
- `AddEmployeeBankCommand(employeeId, bankCode, accountNumber)` → `AddEmployeeBankUseCase` (verifies via Paystack)
- `LogInfractionCommand(employeeId, description, severity)` → `LogInfractionUseCase`
- `ResolveInfractionCommand(infractionId, resolutionNotes)` → `ResolveInfractionUseCase`

### Salary Grades
- `CreateSalaryGradeCommand(orgId, name, baseSalary)` → `CreateSalaryGradeUseCase`
- `AddGradeAllowanceCommand(gradeId, name, type, value)` → `AddGradeAllowanceUseCase`
- `AddGradeDeductionCommand(gradeId, name, type, value, isStatutory)` → `AddGradeDeductionUseCase`
- `AddEmployeeAllowanceCommand(employeeId, name, type, value, effectiveFrom, effectiveTo)` → `AddEmployeeAllowanceUseCase`
- `AddEmployeeDeductionCommand(employeeId, name, type, value, effectiveFrom, effectiveTo)` → `AddEmployeeDeductionUseCase`

### Leave
- `CreateLeaveTypeCommand(orgId, name, maxDays, isPaid, requiresApproval)` → `CreateLeaveTypeUseCase`
- `ApplyForLeaveCommand(employeeId, leaveTypeId, startDate, endDate, reason)` → `ApplyForLeaveUseCase`
- `ApproveLeaveCommand(leaveId, approverId)` → `ApproveLeaveUseCase`
- `RejectLeaveCommand(leaveId, reason)` → `RejectLeaveUseCase`
- `CancelLeaveCommand(leaveId)` → `CancelLeaveUseCase`

### Attendance
- `ClockInCommand(employeeId, orgId)` → `ClockInUseCase`
- `ClockOutCommand(employeeId, orgId)` → `ClockOutUseCase`

### Loans
- `ApplyForLoanCommand(employeeId, amount, monthlyDeduction)` → `ApplyForLoanUseCase`
- `ApproveLoanCommand(loanId, approverId)` → `ApproveLoanUseCase` → publishes `LoanApprovedEvent`
- `CancelLoanCommand(loanId)` → `CancelLoanUseCase`

### Payroll
- `InitiatePayrollCommand(orgId, period, initiatorUserId)` → `InitiatePayrollUseCase`
- `SubmitPayrollForApprovalCommand(payrollRunId, initiatorUserId)` → `SubmitPayrollForApprovalUseCase`
- `ApprovePayrollCommand(payrollRunId, approverUserId)` → `ApprovePayrollUseCase` — validates approver ≠ initiator
- `DisbursePayrollCommand(payrollRunId)` → `DisbursePayrollUseCase` — checks Payroll Reserve Account balance

---

## 6. Queries

- `ListEmployeesQuery(orgId, status, department)` → `Page<EmployeeResult>`
- `GetEmployeeDetailsQuery(employeeId)` → `EmployeeDetailsResult`
- `ListPayrollRunsQuery(orgId, status)` → `List<PayrollRunResult>`
- `GetPayslipQuery(payslipId)` → `PayslipResult`
- `ListPayslipsByEmployeeQuery(employeeId, year)` → `List<PayslipResult>`
- `GetLeaveBalanceQuery(employeeId, year)` → `List<LeaveBalanceResult>`
- `ListLeaveApplicationsQuery(orgId, employeeId, status)` → `List<LeaveApplicationResult>`
- `GetAttendanceSummaryQuery(orgId, employeeId, month, year)` → `AttendanceSummaryResult`
- `ListLoansQuery(orgId, employeeId, status)` → `List<LoanResult>`
- `ListSalaryGradesQuery(orgId)` → `List<SalaryGradeResult>`

---

## 7. Listeners

- **`UserInvitationAcceptedListener`**: `InvitationAcceptedEvent` from `iam`. Calls `OnboardEmployeeUseCase` to draft an employee record (if org has HR subscribed — checked via `EntitlementQueryPort`).
- **`BulkPayoutCompletedListener`**: `BulkPayoutCompletedEvent` from `pay`. Calls `PayrollRun.markDisbursed()` → publishes `PayrollDisbursedEvent`.
- **`BulkPayoutFailedListener`**: `BulkPayoutFailedEvent` from `pay`. Calls `PayrollRun.revertToApproved(reason)` → admin investigates and retries.

---

## 8. Distributed Architecture

### Locking
- **Optimistic Locking**: `Employee`, `PayrollRun`, `Infraction`
- **Pessimistic Locking**: `LeaveBalance` during `ApproveLeaveUseCase` — prevents concurrent approvals overdrawing leave balance
- **Pessimistic Locking**: `EmployeeLoan.outstandingBalance` during `recordRepayment()` — prevents concurrent payroll deductions corrupting the balance
- **Pessimistic Locking**: `EmployeeDeduction` list during payroll initiation — prevents concurrent deduction additions changing the payslip mid-calculation

### Payroll Saga
```
DisbursePayrollUseCase → publishes PayrollApprovedEvent
  → pay executes bulk payouts
  → SUCCESS: BulkPayoutCompletedEvent → HR marks DISBURSED → PayrollDisbursedEvent
  → FAILURE: BulkPayoutFailedEvent → HR reverts to APPROVED (admin can investigate and retry)
```

### Outbox & Inbox
- **Outbox**: `PayrollApprovedEvent`, `LoanApprovedEvent` — drive money movement and must not be lost
- **Inbox**: `BulkPayoutCompletedEvent`, `BulkPayoutFailedEvent` — idempotent. A retry storm must not mark the same payroll disbursed twice.
