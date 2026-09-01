# HR & Payroll Module Design (`atlashub-hr`)

## 1. Domain Entities & Aggregates

### `staff` Submodule
**`Employee` (Aggregate Root)**
- **Fields**: 
  - `id`: Long, `organizationId`: Long, `userId`: Long
  - `firstName`: String, `lastName`: String, `email`: String, `phone`: String
  - `dob`: LocalDate, `address`: String
  - `hireDate`: LocalDate, `terminationDate`: LocalDate
  - `designation`: String, `department`: String
  - `status`: `EmployeeStatus` (ACTIVE, PROBATION, SUSPENDED, TERMINATED, ON_LEAVE)
- **Methods**: `updateBioData(...)`, `promote(String newDesignation)`, `suspend()`, `terminate(LocalDate date)`

**`Qualification` (Entity)**
- **Fields**: `id`, `employeeId`, `degree`, `institution`, `yearAwarded`

**`Infraction` (Aggregate Root)**
- **Fields**: `id`, `employeeId`, `organizationId`, `description`, `severity` (LOW, MEDIUM, HIGH), `reportedAt`: LocalDate, `status` (PENDING, RESOLVED)
- **Methods**: `resolve(String resolutionNotes)`

### `payroll` Submodule
**`PayrollRun` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `period`: String (e.g., "2026-09"), `totalGross`: BigDecimal, `totalDeductions`: BigDecimal, `totalNet`: BigDecimal, `status`: `PayrollStatus` (DRAFT, PENDING_APPROVAL, APPROVED, DISBURSED)
- **Methods**: `addPayslip(Payslip payslip)`, `submitForApproval()`, `approve()`, `markDisbursed()`

**`Payslip` (Entity)**
- **Fields**: `id`, `payrollRunId`, `employeeId`, `grossPay`, `deductions`, `netPay`, `status`

**`EmployeeBank` (Aggregate Root)**
- **Fields**: `id`, `employeeId`, `bankCode`, `bankName`, `accountNumber`, `accountName`, `isVerified`: Boolean
- **Methods**: `verify()`

## 2. Domain Events (Wrapped in `EnvelopedDomainEvent`)
- `EmployeeOnboardedEvent(Long employeeId, Long organizationId, Long userId)`
- `EmployeeTerminatedEvent(Long employeeId, LocalDate date)`
- `InfractionLoggedEvent(Long infractionId, Long employeeId, String severity)`
- `InfractionResolvedEvent(Long infractionId)`
- `PayrollInitiatedEvent(Long payrollRunId, String period)`
- `PayrollApprovedEvent(Long payrollRunId, BigDecimal totalNet)`
- `PayrollDisbursedEvent(Long payrollRunId)`
- `EmployeeBankAddedEvent(Long employeeId, String accountNumber)`

## 3. Exceptions & Errors
**`HrErrorCode`** (implements `ErrorCode`):
- `EMPLOYEE_NOT_FOUND`, `USER_ALREADY_EMPLOYED`
- `INVALID_PAYROLL_STATE`, `PAYROLL_RUN_NOT_FOUND`
- `BANK_ACCOUNT_INVALID`
- `INFRACTION_NOT_FOUND`

## 4. Commands & Use Cases
- **Command**: `OnboardEmployeeCommand(Long orgId, Long userId, BioData bio, String designation)` -> `OnboardEmployeeUseCase`
- **Command**: `UpdateEmployeeProfileCommand(...)` -> `UpdateEmployeeProfileUseCase`
- **Command**: `TerminateEmployeeCommand(Long employeeId, LocalDate date)` -> `TerminateEmployeeUseCase`
- **Command**: `AddQualificationCommand(Long employeeId, String degree, String institution, Integer year)` -> `AddQualificationUseCase`
- **Command**: `LogInfractionCommand(Long employeeId, String description, String severity)` -> `LogInfractionUseCase`
- **Command**: `ResolveInfractionCommand(Long infractionId, String notes)` -> `ResolveInfractionUseCase`
- **Command**: `InitiatePayrollCommand(Long orgId, String period)` -> `InitiatePayrollUseCase` (Calculates gross, net, generates payslips).
- **Command**: `ApprovePayrollCommand(Long payrollRunId)` -> `ApprovePayrollUseCase`
- **Command**: `DisbursePayrollCommand(Long payrollRunId)` -> `DisbursePayrollUseCase` (Triggers bulk payout via Pay module).
- **Command**: `AddEmployeeBankCommand(...)` -> `AddEmployeeBankUseCase`

## 5. Queries
- `ListEmployeesQuery(Long orgId, EmployeeStatus status)` -> `List<EmployeeResult>`
- `GetEmployeeDetailsQuery(Long employeeId)` -> `EmployeeDetailsResult`
- `ListInfractionsQuery(Long orgId, Long employeeId)` -> `List<InfractionResult>`
- `ListPayrollRunsQuery(Long orgId)` -> `List<PayrollRunResult>`
- `GetPayslipQuery(Long payslipId)` -> `PayslipResult`

## 6. Listeners
- `UserInvitationAcceptedListener`: Listens to `InvitationAccepted` (from `identity`) to automatically draft an `Employee` record.
- `BulkPayoutSuccessfulListener`: Listens to `PayoutCompletedEvent` (from Pay module) matching a payroll reference to dispatch `DisbursePayrollCommand`.

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Optimistic Locking (@Version)**: Applied to Employee and PayrollRun aggregates.

### Sagas & Compensation
- **Payroll Saga**: When ApprovePayrollCommand is executed, salaries are sent to the tlashub-pay module. If the Payment module publishes a PayoutFailedEvent (e.g., due to insufficient company wallet balance), a compensation action is triggered in HR to revert the PayrollRun status from DISBURSED back to FAILED.

### Inbox & Outbox Patterns
- **Outbox**: Used to publish PayrollApprovedEvent guaranteeing delivery to the Pay module.
- **Inbox (EventDeliveryTracker)**: Ensures PayoutCompletedEvents are processed idempotently.
