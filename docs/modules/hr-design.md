# HR & Payroll Module Design (`atlashub-hr`)

## Role & Purpose

The HR module is the **people operations engine** of the AtlasHub platform. It manages the full employment lifecycle within an organization — from onboarding a new hire to disbursing their monthly salary to handling their resignation. It is one of the most financially consequential modules because payroll disbursement directly triggers bulk money movement through `atlashub-pay`.

An organization using Atlas HR can:
- Maintain a complete employee registry with bio-data, qualifications, and employment history.
- Define salary grades and allowance/deduction structures that auto-calculate payslips.
- Run monthly payroll with full compliance — PAYE tax, pension, NHF, and other statutory deductions calculated inline.
- Manage annual leave, sick leave, and maternity/paternity leave with balance tracking.
- Track attendance with clock-in/clock-out and auto-calculate overtime.
- Disburse employee loans with installment repayments deducted from payroll.
- Handle disciplinary infractions with resolution tracking.

The HR module talks to two other modules heavily: `pay` (salary disbursement and loan payouts) and `accounting` (journal entries for salary expense, payroll payable, and tax liabilities).

---

## 1. Features

### Employee Onboarding & Management
When a new employee joins, they are either:
- **Directly added** via `OnboardEmployeeUseCase` (with their `userId` linking to an existing platform user).
- **Auto-drafted** when `InvitationAccepted` event fires (if the org has HR subscribed).

The `Employee` aggregate holds complete bio-data, designation, department, salary grade reference, and employment status. Status transitions cover the full employment lifecycle: PROBATION → ACTIVE → ON_LEAVE → SUSPENDED → TERMINATED.

### Salary Grades & Payslip Calculation
Rather than setting individual employee salaries manually for every employee, organizations define `SalaryGrade`s (e.g., "Grade 7 – Senior Engineer" = ₦450,000 base). Each grade includes configurable `GradeAllowance` entries (housing, transport) and `GradeDeduction` entries (PAYE, NHF, pension). Employees assigned to a grade automatically inherit these structures.

Individual employees can also have additional `EmployeeAllowance` and `EmployeeDeduction` overrides beyond their grade — for loan repayments, performance bonuses, etc.

### Payroll Run
The payroll process follows a strict approval workflow:
1. `InitiatePayrollUseCase`: Fetches all ACTIVE/PROBATION employees, calculates gross pay per employee from their grade + individual overrides, deducts all configured deductions (including active loan installments and statutory amounts), generates a `Payslip` per employee. Creates the `PayrollRun` in DRAFT status.
2. `ApprovePayrollUseCase`: Manager/Owner approves. `PayrollRun` moves to APPROVED. Statutory deduction totals are posted to Tax Holding Account in `pay:ledger`.
3. `DisbursePayrollUseCase`: Calls `pay` to execute bulk payouts to all employee bank accounts. `PayrollRun` moves to PROCESSING → DISBURSED on success, or reverts to APPROVED on failure (so admin can retry after funding the wallet).

Before payroll disbursement, the organization **must have sufficient funds in their Payroll Reserve Account** in `pay:ledger`. If not, `FundPayrollReserveCommand` must be called in `pay` to move funds from Operating Account → Payroll Reserve Account. Disbursement then debits the Payroll Reserve Account and credits the Payout Clearing Account.

### Leave Management
Organizations define `LeaveType`s (Annual Leave, Sick Leave, Maternity Leave) with maximum days per year and whether approval is required. Employees apply for leave via `ApplyForLeaveUseCase`, which validates against their `LeaveBalance.remaining`. On approval, the balance is deducted and the employee's status is set to ON_LEAVE. On rejection or cancellation, the balance is restored.

### Attendance Tracking
Employees clock in and out via `ClockInUseCase` / `ClockOutUseCase`. Clock-out automatically calculates `hoursWorked` and `overtimeHours` based on the organization's configured shift hours. Attendance records are used in payroll calculation if the org uses hourly or attendance-based pay.

### Employee Loans
Employees can apply for salary advances or loans. Approved loans are disbursed via `pay` (crediting the employee's bank account). Repayments are automatically deducted as `EmployeeDeduction` entries in the next payroll run, reducing `outstandingBalance` on each payslip.

### Infractions & Disciplinary
Managers can log `Infraction` records (LOW, MEDIUM, HIGH severity) against employees. These can be resolved with resolution notes. HIGH severity infractions can trigger suspension.

---

## 2. Domain Entities & Aggregates

### `staff` Submodule

**`Employee` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `userId`, `firstName`, `lastName`, `email`, `phone`, `dob`, `address`, `hireDate`, `terminationDate`, `designation`, `department`, `salaryGradeId`, `baseSalary`: `Money`, `status`: `EmployeeStatus` (ACTIVE, PROBATION, SUSPENDED, TERMINATED, ON_LEAVE)
- **Methods**: `updateBioData(...)`, `promote(String newDesignation, Long newGradeId)`, `suspend()`, `terminate(LocalDate date)`, `placeOnLeave()`, `returnFromLeave()`

**`Qualification` (Entity)**
- **Fields**: `id`, `employeeId`, `degree`, `institution`, `yearAwarded`

**`Infraction` (Aggregate Root)**
- **Fields**: `id`, `employeeId`, `organizationId`, `description`, `severity` (`InfractionSeverity`: LOW, MEDIUM, HIGH), `reportedAt`, `status` (PENDING, RESOLVED)
- **Methods**: `resolve(String resolutionNotes)`

**`SalaryGrade` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `name`, `baseSalary`: `Money`, `allowances`: `List<GradeAllowance>`, `deductions`: `List<GradeDeduction>`
- **Methods**: `updateBaseSalary(Money amount)`, `addAllowance(...)`, `addDeduction(...)`

**`GradeAllowance` (Entity)**: `id`, `gradeId`, `name`, `type` (FIXED, PERCENTAGE_OF_GROSS), `value`: BigDecimal

**`GradeDeduction` (Entity)**: `id`, `gradeId`, `name`, `type`, `value`, `isStatutory`: Boolean

**`EmployeeAllowance` (Entity)**: `id`, `employeeId`, `name`, `type`, `value`, `effectiveFrom`: LocalDate

**`EmployeeDeduction` (Entity)**: `id`, `employeeId`, `name`, `type`, `value`, `effectiveFrom`: LocalDate

**`EmployeeBank` (Aggregate Root)**
- **Fields**: `id`, `employeeId`, `bankCode`, `bankName`, `accountNumber`, `accountName`, `isVerified`, `isPrimary`
- **Methods**: `verify()`, `markPrimary()`

### `leave` Submodule

**`LeaveType` (Aggregate Root)**: `id`, `organizationId`, `name`, `maxDaysPerYear`, `isPaid`, `requiresApproval`

**`LeaveApplication` (Aggregate Root)**
- **Fields**: `id`, `employeeId`, `organizationId`, `leaveTypeId`, `startDate`, `endDate`, `daysRequested`, `reason`, `status` (PENDING, APPROVED, REJECTED, CANCELLED), `approvedBy`, `approvedAt`
- **Methods**: `approve(Long approverId)`, `reject(String reason)`, `cancel()`

**`LeaveBalance` (Entity)**: `id`, `employeeId`, `leaveTypeId`, `year`, `entitlement`, `used`, `remaining`
- **Methods**: `deduct(Integer days)`, `restore(Integer days)`

### `attendance` Submodule

**`AttendanceRecord` (Aggregate Root)**
- **Fields**: `id`, `employeeId`, `organizationId`, `date`, `clockIn`, `clockOut`, `hoursWorked`, `overtimeHours`, `status` (PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE)
- **Methods**: `clockIn(ZonedDateTime time)`, `clockOut(ZonedDateTime time)`

### `loan` Submodule

**`EmployeeLoan` (Aggregate Root)**
- **Fields**: `id`, `employeeId`, `organizationId`, `principalAmount`: `Money`, `outstandingBalance`: `Money`, `monthlyDeduction`: `Money`, `status` (PENDING_APPROVAL, ACTIVE, FULLY_REPAID, CANCELLED), `approvedBy`, `startDate`
- **Methods**: `approve(Long approverId, LocalDate startDate)`, `recordRepayment(Money amount)`, `cancel()`

### `payroll` Submodule

**`PayrollRun` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `period` (e.g., "2026-09"), `totalGross`: `Money`, `totalDeductions`: `Money`, `totalNet`: `Money`, `status`: `PayrollStatus` (DRAFT, PENDING_APPROVAL, APPROVED, PROCESSING, DISBURSED, FAILED)
- **Methods**: `addPayslip(Payslip)`, `submitForApproval()`, `approve()`, `markProcessing()`, `markDisbursed()`, `revertToApproved(String reason)`

**`Payslip` (Entity)**
- **Fields**: `id`, `payrollRunId`, `employeeId`, `grossPay`: `Money`, `totalAllowances`: `Money`, `totalDeductions`: `Money`, `netPay`: `Money`, `breakdown`: `List<PayslipLineItem>`, `status`

**`PayslipLineItem` (Entity)**: `id`, `payslipId`, `name`, `type` (ALLOWANCE, DEDUCTION), `amount`: `Money`

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `EmployeeOnboardedEvent` | New employee created | `notifications` (welcome email), `accounting` (open employment record) |
| `EmployeeTerminatedEvent` | Employee terminated | `pay` (close any active salary mandates), `accounting` |
| `EmployeePromotedEvent` | Employee promoted | `notifications` |
| `InfractionLoggedEvent` | Infraction recorded | `notifications` (notify employee/manager) |
| `LeaveApplicationCreatedEvent` | Leave applied for | `notifications` (notify approver) |
| `LeaveApplicationApprovedEvent` | Leave approved | `notifications`, attendance tracking |
| `LeaveApplicationRejectedEvent` | Leave rejected | `notifications` (notify employee with reason) |
| `EmployeeLoanApprovedEvent` | Loan approved | `pay` (disburse loan amount to employee bank account) |
| `PayrollInitiatedEvent` | Payroll run created | `notifications` (notify approver) |
| `PayrollApprovedEvent` | Payroll approved by manager | `pay` (execute bulk salary payouts), `accounting` (Debit Salary Expense, Credit Payroll Payable) |
| `PayrollDisbursedEvent` | All salaries disbursed | `accounting` (Debit Payroll Payable, Credit Payroll Reserve Account), `notifications` |

---

## 4. Exceptions & Errors

**`HrErrorCode`** (implements `ErrorCode`):
- `EMPLOYEE_NOT_FOUND`, `USER_ALREADY_EMPLOYED`
- `INVALID_PAYROLL_STATE`, `PAYROLL_RUN_NOT_FOUND`
- `BANK_ACCOUNT_INVALID`, `INFRACTION_NOT_FOUND`
- `LEAVE_TYPE_NOT_FOUND`, `INSUFFICIENT_LEAVE_BALANCE`, `LEAVE_APPLICATION_NOT_FOUND`
- `INVALID_LEAVE_STATE`, `LEAVE_DATES_CONFLICT`
- `LOAN_NOT_FOUND`, `LOAN_APPROVAL_REQUIRED`, `LOAN_ALREADY_ACTIVE`
- `SALARY_GRADE_NOT_FOUND`
- `INSUFFICIENT_PAYROLL_RESERVE` — raised when attempting to disburse payroll without sufficient balance in Payroll Reserve Account

---

## 5. Commands & Use Cases

### Staff Management
- `OnboardEmployeeCommand(orgId, userId, bio, designation, salaryGradeId)` → `OnboardEmployeeUseCase`
- `UpdateEmployeeProfileCommand(...)` → `UpdateEmployeeProfileUseCase`
- `TerminateEmployeeCommand(employeeId, date)` → `TerminateEmployeeUseCase`
- `PromoteEmployeeCommand(employeeId, newDesignation, newGradeId)` → `PromoteEmployeeUseCase`
- `SuspendEmployeeCommand(employeeId, reason)` → `SuspendEmployeeUseCase`
- `AddQualificationCommand(employeeId, degree, institution, year)` → `AddQualificationUseCase`
- `LogInfractionCommand(employeeId, description, severity)` → `LogInfractionUseCase`
- `ResolveInfractionCommand(infractionId, notes)` → `ResolveInfractionUseCase`

### Salary Grades
- `CreateSalaryGradeCommand(orgId, name, baseSalary)` → `CreateSalaryGradeUseCase`
- `AddGradeAllowanceCommand(gradeId, name, type, value)` → `AddGradeAllowanceUseCase`
- `AddGradeDeductionCommand(gradeId, name, type, value, isStatutory)` → `AddGradeDeductionUseCase`
- `AddEmployeeAllowanceCommand(employeeId, name, type, value)` → `AddEmployeeAllowanceUseCase`
- `AddEmployeeDeductionCommand(employeeId, name, type, value)` → `AddEmployeeDeductionUseCase`
- `AddEmployeeBankCommand(employeeId, bankCode, accountNumber)` → `AddEmployeeBankUseCase`

### Leave
- `CreateLeaveTypeCommand(orgId, name, maxDays, isPaid, requiresApproval)` → `CreateLeaveTypeUseCase`
- `ApplyForLeaveCommand(employeeId, leaveTypeId, startDate, endDate, reason)` → `ApplyForLeaveUseCase`
- `ApproveLeaveCommand(leaveId, approverId)` → `ApproveLeaveUseCase`
- `RejectLeaveCommand(leaveId, reason)` → `RejectLeaveUseCase`

### Attendance
- `ClockInCommand(employeeId, orgId)` → `ClockInUseCase`
- `ClockOutCommand(employeeId, orgId)` → `ClockOutUseCase`

### Loans
- `ApplyForLoanCommand(employeeId, amount, monthlyDeduction)` → `ApplyForLoanUseCase`
- `ApproveLoanCommand(loanId, approverId)` → `ApproveLoanUseCase` — publishes `EmployeeLoanApprovedEvent` → `pay` disburses loan
- `RecordLoanRepaymentCommand(loanId, amount)` → `RecordLoanRepaymentUseCase`

### Payroll
- `InitiatePayrollCommand(orgId, period)` → `InitiatePayrollUseCase`
- `ApprovePayrollCommand(payrollRunId)` → `ApprovePayrollUseCase`
- `DisbursePayrollCommand(payrollRunId)` → `DisbursePayrollUseCase`
  Validates Payroll Reserve Account has sufficient balance. Publishes `PayrollApprovedEvent` → triggers bulk payouts in `atlashub-pay`.

---

## 6. Queries

- `ListEmployeesQuery(orgId, status)` → `List<EmployeeResult>`
- `GetEmployeeDetailsQuery(employeeId)` → `EmployeeDetailsResult`
- `ListInfractionsQuery(orgId, employeeId)` → `List<InfractionResult>`
- `ListPayrollRunsQuery(orgId)` → `List<PayrollRunResult>`
- `GetPayslipQuery(payslipId)` → `PayslipResult`
- `ListSalaryGradesQuery(orgId)` → `List<SalaryGradeResult>`
- `GetLeaveBalanceQuery(employeeId, year)` → `List<LeaveBalanceResult>`
- `ListLeaveApplicationsQuery(orgId, employeeId, status)` → `List<LeaveApplicationResult>`
- `GetAttendanceSummaryQuery(orgId, employeeId, month, year)` → `AttendanceSummaryResult`
- `ListLoansQuery(orgId, employeeId, status)` → `List<LoanResult>`

---

## 7. Listeners

- **`UserInvitationAcceptedListener`**: Listens to `InvitationAccepted` (from `identity`). If the org has HR subscribed (checked via `billing`), drafts an `Employee` record and sends an onboarding reminder.
- **`BulkPayoutCompletedListener`**: Listens to `BulkPayoutCompletedEvent` (from `pay`). Calls `PayrollRun.markDisbursed()`. Triggers `PayrollDisbursedEvent` → `accounting` posts the final journal entry.
- **`BulkPayoutFailedListener`**: Listens to `BulkPayoutFailedEvent` (from `pay`). Calls `PayrollRun.revertToApproved(reason)` so admin can investigate, refund the failed items, and retry.

---

## 8. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `Employee`, `PayrollRun`, and `LeaveBalance`.
- **Pessimistic Locking (`@Lock(PESSIMISTIC_WRITE)`)**: Applied to `LeaveBalance` during `ApproveLeaveUseCase` — prevents concurrent approvals from over-drawing the same leave balance. Applied to `EmployeeLoan.outstandingBalance` during repayment recording.

### Sagas & Compensation
- **Payroll Saga**: `DisbursePayrollUseCase` publishes `PayrollApprovedEvent` → `pay` processes bulk payouts → On `BulkPayoutFailedEvent`, HR calls `PayrollRun.revertToApproved()` so admin can retry after funding the Payroll Reserve Account.
- **Loan Disbursement Saga**: `ApproveLoanUseCase` publishes `EmployeeLoanApprovedEvent` → `pay` credits employee bank → On failure, HR reverts loan status to PENDING_APPROVAL.

### Inbox & Outbox Patterns
- **Outbox**: Publishes `PayrollApprovedEvent` and `EmployeeLoanApprovedEvent` guaranteeing delivery to `pay`.
- **Inbox (`EventDeliveryTracker`)**: Idempotent processing of `BulkPayoutCompletedEvent` and `BulkPayoutFailedEvent`. Without this, a retry storm could mark the same payroll run as disbursed multiple times.
