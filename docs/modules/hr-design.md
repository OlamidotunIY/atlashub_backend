# HR & Payroll Module Design (`atlashub-hr`)

## 1. Domain Entities & Aggregates

### `staff` Submodule

**`Employee` (Aggregate Root)**
- **Fields**:
  - `id`: Long, `organizationId`: Long, `userId`: Long
  - `firstName`: String, `lastName`: String, `email`: String, `phone`: String
  - `dob`: LocalDate, `address`: String
  - `hireDate`: LocalDate, `terminationDate`: LocalDate (nullable)
  - `designation`: String, `department`: String
  - `salaryGradeId`: Long (nullable — links to `SalaryGrade` for automated payslip calculation)
  - `baseSalary`: **`Money`** (nullable — set directly or derived from `SalaryGrade`)
  - `status`: `EmployeeStatus` (ACTIVE, PROBATION, SUSPENDED, TERMINATED, ON_LEAVE)
- **Methods**: `updateBioData(...)`, `promote(String newDesignation, Long newGradeId)`, `suspend()`, `terminate(LocalDate date)`, `placeOnLeave()`, `returnFromLeave()`

**`Qualification` (Entity)**
- **Fields**: `id`, `employeeId`, `degree`, `institution`, `yearAwarded`

**`Infraction` (Aggregate Root)**
- **Fields**: `id`, `employeeId`, `organizationId`, `description`, `severity` (`InfractionSeverity`: LOW, MEDIUM, HIGH), `reportedAt`: LocalDate, `status` (PENDING, RESOLVED)
- **Methods**: `resolve(String resolutionNotes)`

**`SalaryGrade` (Aggregate Root)**
Defines a pay band used for automated payslip calculation.
- **Fields**: `id`, `organizationId`, `name` (e.g., "Grade 7 – Senior Engineer"), `baseSalary`: **`Money`**, `allowances`: `List<GradeAllowance>`, `deductions`: `List<GradeDeduction>`
- **Methods**: `updateBaseSalary(Money amount)`, `addAllowance(GradeAllowance)`, `addDeduction(GradeDeduction)`

**`GradeAllowance` (Entity)**
- **Fields**: `id`, `gradeId`, `name` (e.g., "Housing Allowance"), `type` (FIXED, PERCENTAGE_OF_GROSS), `value`: BigDecimal

**`GradeDeduction` (Entity)**
- **Fields**: `id`, `gradeId`, `name` (e.g., "PAYE Tax", "NHF", "Pension"), `type` (FIXED, PERCENTAGE_OF_GROSS), `value`: BigDecimal, `isStatutory`: Boolean

**`EmployeeAllowance` (Entity)**
Employee-specific allowance overrides beyond their grade.
- **Fields**: `id`, `employeeId`, `name`, `type` (FIXED, PERCENTAGE_OF_GROSS), `value`: BigDecimal, `effectiveFrom`: LocalDate

**`EmployeeDeduction` (Entity)**
Employee-specific deductions (e.g., loan repayments).
- **Fields**: `id`, `employeeId`, `name`, `type` (FIXED, PERCENTAGE_OF_GROSS), `value`: BigDecimal, `effectiveFrom`: LocalDate

### `leave` Submodule

**`LeaveType` (Aggregate Root)**
Organization-defined leave categories.
- **Fields**: `id`, `organizationId`, `name` (e.g., "Annual Leave", "Sick Leave", "Maternity Leave"), `maxDaysPerYear`: Integer, `isPaid`: Boolean, `requiresApproval`: Boolean

**`LeaveApplication` (Aggregate Root)**
- **Fields**: `id`, `employeeId`, `organizationId`, `leaveTypeId`, `startDate`: LocalDate, `endDate`: LocalDate, `daysRequested`: Integer, `reason`: String, `status`: `LeaveStatus` (PENDING, APPROVED, REJECTED, CANCELLED), `approvedBy`: Long (nullable), `approvedAt`: ZonedDateTime (nullable)
- **Methods**: `approve(Long approverId)`, `reject(String reason)`, `cancel()`

**`LeaveBalance` (Entity)**
Tracks remaining leave days per employee per leave type per year.
- **Fields**: `id`, `employeeId`, `leaveTypeId`, `year`: Integer, `entitlement`: Integer, `used`: Integer, `remaining`: Integer
- **Methods**: `deduct(Integer days)`, `restore(Integer days)`

### `attendance` Submodule

**`AttendanceRecord` (Aggregate Root)**
- **Fields**: `id`, `employeeId`, `organizationId`, `date`: LocalDate, `clockIn`: ZonedDateTime (nullable), `clockOut`: ZonedDateTime (nullable), `hoursWorked`: BigDecimal (nullable), `overtimeHours`: BigDecimal (nullable), `status` (PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE)
- **Methods**: `clockIn(ZonedDateTime time)`, `clockOut(ZonedDateTime time)` (auto-calculates `hoursWorked` and `overtimeHours` based on org-defined shift hours)

### `loan` Submodule

**`EmployeeLoan` (Aggregate Root)**
Salary advance or loan with installment repayment.
- **Fields**: `id`, `employeeId`, `organizationId`, `principalAmount`: **`Money`**, `outstandingBalance`: **`Money`**, `monthlyDeduction`: **`Money`**, `status` (PENDING_APPROVAL, ACTIVE, FULLY_REPAID, CANCELLED), `approvedBy`: Long (nullable), `startDate`: LocalDate
- **Methods**: `approve(Long approverId, LocalDate startDate)`, `recordRepayment(Money amount)`, `cancel()`

### `payroll` Submodule

**`PayrollRun` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `period`: String (e.g., "2026-09"), `totalGross`: **`Money`**, `totalDeductions`: **`Money`**, `totalNet`: **`Money`**, `status`: `PayrollStatus` (DRAFT, PENDING_APPROVAL, APPROVED, PROCESSING, DISBURSED, FAILED)
- **Methods**: `addPayslip(Payslip)`, `submitForApproval()`, `approve()`, `markProcessing()`, `markDisbursed()`, `revertToApproved(String reason)`

> **Multi-Currency Note**: `PayrollRun.totalNet` uses the Organization's base `CurrencyCode`. If an employee's bank is in a different currency, an explicit FX payout must be triggered.

**`Payslip` (Entity)**
- **Fields**: `id`, `payrollRunId`, `employeeId`, `grossPay`: **`Money`**, `totalAllowances`: **`Money`**, `totalDeductions`: **`Money`**, `netPay`: **`Money`**, `breakdown`: `List<PayslipLineItem>` (named line items — e.g., "PAYE: ₦15,000"), `status`

**`PayslipLineItem` (Entity)**
- **Fields**: `id`, `payslipId`, `name`, `type` (ALLOWANCE, DEDUCTION), `amount`: **`Money`**

**`EmployeeBank` (Aggregate Root)**
- **Fields**: `id`, `employeeId`, `bankCode`, `bankName`, `accountNumber`, `accountName`, `isVerified`: Boolean, `isPrimary`: Boolean
- **Methods**: `verify()`, `markPrimary()`

## 2. Domain Events (Wrapped in `EnvelopedDomainEvent`)
- `EmployeeOnboardedEvent(Long employeeId, Long organizationId, Long userId)`
- `EmployeeTerminatedEvent(Long employeeId, LocalDate date)`
- `EmployeePromotedEvent(Long employeeId, String newDesignation, Long newGradeId)`
- `InfractionLoggedEvent(Long infractionId, Long employeeId, String severity)`
- `InfractionResolvedEvent(Long infractionId)`
- `LeaveApplicationCreatedEvent(Long leaveId, Long employeeId, LocalDate start, LocalDate end)`
- `LeaveApplicationApprovedEvent(Long leaveId, Long employeeId)`
- `LeaveApplicationRejectedEvent(Long leaveId, Long employeeId, String reason)`
- `EmployeeClockedInEvent(Long recordId, Long employeeId, ZonedDateTime time)`
- `EmployeeClockedOutEvent(Long recordId, Long employeeId, ZonedDateTime time, BigDecimal hoursWorked)`
- `EmployeeLoanApprovedEvent(Long loanId, Long employeeId, Money amount)`
- `PayrollInitiatedEvent(Long payrollRunId, String period)`
- `PayrollApprovedEvent(Long payrollRunId, Money totalNet)`
- `PayrollDisbursedEvent(Long payrollRunId)`
- `EmployeeBankAddedEvent(Long employeeId, String accountNumber)`

## 3. Exceptions & Errors
**`HrErrorCode`** (implements `ErrorCode`):
- `EMPLOYEE_NOT_FOUND`, `USER_ALREADY_EMPLOYED`
- `INVALID_PAYROLL_STATE`, `PAYROLL_RUN_NOT_FOUND`
- `BANK_ACCOUNT_INVALID`
- `INFRACTION_NOT_FOUND`
- `LEAVE_TYPE_NOT_FOUND`, `INSUFFICIENT_LEAVE_BALANCE`, `LEAVE_APPLICATION_NOT_FOUND`
- `INVALID_LEAVE_STATE`, `LEAVE_DATES_CONFLICT`
- `LOAN_NOT_FOUND`, `LOAN_APPROVAL_REQUIRED`, `LOAN_ALREADY_ACTIVE`
- `SALARY_GRADE_NOT_FOUND`

## 4. Commands & Use Cases

### Staff Management
- `OnboardEmployeeCommand(orgId, userId, bio, designation, salaryGradeId)` → `OnboardEmployeeUseCase`
- `UpdateEmployeeProfileCommand(...)` → `UpdateEmployeeProfileUseCase`
- `TerminateEmployeeCommand(employeeId, date)` → `TerminateEmployeeUseCase`
- `PromoteEmployeeCommand(employeeId, newDesignation, newGradeId)` → `PromoteEmployeeUseCase`
- `SuspendEmployeeCommand(employeeId, reason)` → `SuspendEmployeeUseCase`
- `AddQualificationCommand(employeeId, degree, institution, year)` → `AddQualificationUseCase`
- `LogInfractionCommand(employeeId, description, severity)` → `LogInfractionUseCase`
- `ResolveInfractionCommand(infractionId, notes)` → `ResolveInfractionUseCase`

### Salary Grades & Allowances
- `CreateSalaryGradeCommand(orgId, name, baseSalary)` → `CreateSalaryGradeUseCase`
- `AddGradeAllowanceCommand(gradeId, name, type, value)` → `AddGradeAllowanceUseCase`
- `AddGradeDeductionCommand(gradeId, name, type, value, isStatutory)` → `AddGradeDeductionUseCase`
- `AddEmployeeAllowanceCommand(employeeId, name, type, value)` → `AddEmployeeAllowanceUseCase`
- `AddEmployeeDeductionCommand(employeeId, name, type, value)` → `AddEmployeeDeductionUseCase`

### Leave
- `CreateLeaveTypeCommand(orgId, name, maxDays, isPaid, requiresApproval)` → `CreateLeaveTypeUseCase`
- `ApplyForLeaveCommand(employeeId, leaveTypeId, startDate, endDate, reason)` → `ApplyForLeaveUseCase` (Validates `LeaveBalance.remaining >= daysRequested`.)
- `ApproveLeaveCommand(leaveId, approverId)` → `ApproveLeaveUseCase` (Deducts from `LeaveBalance`, sets employee status to ON_LEAVE.)
- `RejectLeaveCommand(leaveId, reason)` → `RejectLeaveUseCase`

### Attendance
- `ClockInCommand(employeeId, orgId)` → `ClockInUseCase`
- `ClockOutCommand(employeeId, orgId)` → `ClockOutUseCase` (Calculates `hoursWorked` and `overtimeHours`.)
- `GenerateAttendanceReportCommand(orgId, month, year)` → `GenerateAttendanceReportUseCase`

### Loans
- `ApplyForLoanCommand(employeeId, amount, monthlyDeduction)` → `ApplyForLoanUseCase`
- `ApproveLoanCommand(loanId, approverId)` → `ApproveLoanUseCase` (Triggers pay module to credit employee wallet/bank.)
- `RecordLoanRepaymentCommand(loanId, amount)` → `RecordLoanRepaymentUseCase`

### Payroll
- `InitiatePayrollCommand(orgId, period)` → `InitiatePayrollUseCase` (Fetches all active employees, calculates gross, allowances, deductions including active loan installments and statutory deductions, generates `Payslip` per employee.)
- `ApprovePayrollCommand(payrollRunId)` → `ApprovePayrollUseCase`
- `DisbursePayrollCommand(payrollRunId)` → `DisbursePayrollUseCase` (Publishes `PayrollApprovedEvent` → triggers bulk payout in `atlashub-pay`.)
- `AddEmployeeBankCommand(employeeId, bankCode, accountNumber)` → `AddEmployeeBankUseCase`

## 5. Queries
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

## 6. Listeners
- `UserInvitationAcceptedListener`: Listens to `InvitationAccepted` (from `identity`) to draft an `Employee` record.
- `BulkPayoutCompletedListener`: Listens to `BulkPayoutCompletedEvent` (from Pay) to mark `PayrollRun` as DISBURSED.
- `BulkPayoutFailedListener`: Listens to `BulkPayoutFailedEvent` (from Pay) to call `PayrollRun.revertToApproved()`.

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `Employee`, `PayrollRun`, and `LeaveBalance`.
- **Pessimistic Locking (`@Lock(PESSIMISTIC_WRITE)`)**: Applied to `LeaveBalance` during leave approval to prevent over-drawing leave days under concurrent requests.
- **Pessimistic Locking**: Applied to `EmployeeLoan.outstandingBalance` during repayment recording.

### Sagas & Compensation
- **Payroll Saga**: `DisbursePayrollUseCase` publishes `PayrollApprovedEvent` → Pay processes bulk payouts → On `BulkPayoutFailedEvent`, HR calls `PayrollRun.revertToApproved()` so admin can retry after funding the wallet.
- **Loan Disbursement Saga**: `ApproveLoanUseCase` publishes `EmployeeLoanApprovedEvent` → Pay credits employee bank → On failure, HR reverts loan status back to PENDING_APPROVAL.

### Inbox & Outbox Patterns
- **Outbox**: Publishes `PayrollApprovedEvent` and `EmployeeLoanApprovedEvent` guaranteeing delivery to Pay module.
- **Inbox (`EventDeliveryTracker`)**: Idempotent processing of `BulkPayoutCompletedEvent` and `BulkPayoutFailedEvent`.
