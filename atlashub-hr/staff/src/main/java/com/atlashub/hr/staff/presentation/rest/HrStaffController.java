package com.atlashub.hr.staff.presentation.rest;

import com.atlashub.hr.staff.application.command.AddEmployeeAllowance.AddEmployeeAllowanceCommand;
import com.atlashub.hr.staff.application.command.AddEmployeeAllowance.AddEmployeeAllowanceHandler;
import com.atlashub.hr.staff.application.command.AddEmployeeBank.AddEmployeeBankCommand;
import com.atlashub.hr.staff.application.command.AddEmployeeBank.AddEmployeeBankHandler;
import com.atlashub.hr.staff.application.command.AddEmployeeBank.AddEmployeeBankResponse;
import com.atlashub.hr.staff.application.command.AddEmployeeDeduction.AddEmployeeDeductionCommand;
import com.atlashub.hr.staff.application.command.AddEmployeeDeduction.AddEmployeeDeductionHandler;
import com.atlashub.hr.staff.application.command.AddGradeAllowance.AddGradeAllowanceCommand;
import com.atlashub.hr.staff.application.command.AddGradeAllowance.AddGradeAllowanceHandler;
import com.atlashub.hr.staff.application.command.AddGradeDeduction.AddGradeDeductionCommand;
import com.atlashub.hr.staff.application.command.AddGradeDeduction.AddGradeDeductionHandler;
import com.atlashub.hr.staff.application.command.CreateSalaryGrade.CreateSalaryGradeCommand;
import com.atlashub.hr.staff.application.command.CreateSalaryGrade.CreateSalaryGradeHandler;
import com.atlashub.hr.staff.application.command.CreateSalaryGrade.CreateSalaryGradeResponse;
import com.atlashub.hr.staff.application.command.LogInfraction.LogInfractionCommand;
import com.atlashub.hr.staff.application.command.LogInfraction.LogInfractionHandler;
import com.atlashub.hr.staff.application.command.LogInfraction.LogInfractionResponse;
import com.atlashub.hr.staff.application.command.OnboardEmployee.OnboardEmployeeCommand;
import com.atlashub.hr.staff.application.command.OnboardEmployee.OnboardEmployeeHandler;
import com.atlashub.hr.staff.application.command.OnboardEmployee.OnboardEmployeeResponse;
import com.atlashub.hr.staff.application.command.PromoteEmployee.PromoteEmployeeCommand;
import com.atlashub.hr.staff.application.command.PromoteEmployee.PromoteEmployeeHandler;
import com.atlashub.hr.staff.application.command.ReinstateEmployee.ReinstateEmployeeCommand;
import com.atlashub.hr.staff.application.command.ReinstateEmployee.ReinstateEmployeeHandler;
import com.atlashub.hr.staff.application.command.ResolveInfraction.ResolveInfractionCommand;
import com.atlashub.hr.staff.application.command.ResolveInfraction.ResolveInfractionHandler;
import com.atlashub.hr.staff.application.command.SuspendEmployee.SuspendEmployeeCommand;
import com.atlashub.hr.staff.application.command.SuspendEmployee.SuspendEmployeeHandler;
import com.atlashub.hr.staff.application.command.TerminateEmployee.TerminateEmployeeCommand;
import com.atlashub.hr.staff.application.command.TerminateEmployee.TerminateEmployeeHandler;
import com.atlashub.hr.staff.application.command.UpdateEmployeeProfile.UpdateEmployeeProfileCommand;
import com.atlashub.hr.staff.application.command.UpdateEmployeeProfile.UpdateEmployeeProfileHandler;
import com.atlashub.hr.staff.application.query.GetEmployeeDetails.EmployeeDetailsResult;
import com.atlashub.hr.staff.application.query.GetEmployeeDetails.GetEmployeeDetailsHandler;
import com.atlashub.hr.staff.application.query.GetEmployeeDetails.GetEmployeeDetailsQuery;
import com.atlashub.hr.staff.application.query.ListEmployees.EmployeeResult;
import com.atlashub.hr.staff.application.query.ListEmployees.ListEmployeesHandler;
import com.atlashub.hr.staff.application.query.ListEmployees.ListEmployeesQuery;
import com.atlashub.hr.staff.application.query.ListSalaryGrades.ListSalaryGradesHandler;
import com.atlashub.hr.staff.application.query.ListSalaryGrades.ListSalaryGradesQuery;
import com.atlashub.hr.staff.application.query.ListSalaryGrades.SalaryGradeResult;
import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;
import com.atlashub.hr.staff.presentation.dto.AddEmployeeAllowanceRequest;
import com.atlashub.hr.staff.presentation.dto.AddEmployeeBankRequest;
import com.atlashub.hr.staff.presentation.dto.AddEmployeeDeductionRequest;
import com.atlashub.hr.staff.presentation.dto.AddGradeAllowanceRequest;
import com.atlashub.hr.staff.presentation.dto.AddGradeDeductionRequest;
import com.atlashub.hr.staff.presentation.dto.CreateSalaryGradeRequest;
import com.atlashub.hr.staff.presentation.dto.LogInfractionRequest;
import com.atlashub.hr.staff.presentation.dto.OnboardEmployeeRequest;
import com.atlashub.hr.staff.presentation.dto.PromoteEmployeeRequest;
import com.atlashub.hr.staff.presentation.dto.ResolveInfractionRequest;
import com.atlashub.hr.staff.presentation.dto.SuspendEmployeeRequest;
import com.atlashub.hr.staff.presentation.dto.TerminateEmployeeRequest;
import com.atlashub.hr.staff.presentation.dto.UpdateEmployeeProfileRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "HR Staff", description = "Endpoints for staff and salary grade management")
public class HrStaffController {

    private final OnboardEmployeeHandler onboardEmployeeHandler;
    private final UpdateEmployeeProfileHandler updateEmployeeProfileHandler;
    private final PromoteEmployeeHandler promoteEmployeeHandler;
    private final SuspendEmployeeHandler suspendEmployeeHandler;
    private final ReinstateEmployeeHandler reinstateEmployeeHandler;
    private final TerminateEmployeeHandler terminateEmployeeHandler;
    private final AddEmployeeBankHandler addEmployeeBankHandler;
    private final AddEmployeeAllowanceHandler addEmployeeAllowanceHandler;
    private final AddEmployeeDeductionHandler addEmployeeDeductionHandler;
    private final LogInfractionHandler logInfractionHandler;
    private final ResolveInfractionHandler resolveInfractionHandler;
    private final CreateSalaryGradeHandler createSalaryGradeHandler;
    private final AddGradeAllowanceHandler addGradeAllowanceHandler;
    private final AddGradeDeductionHandler addGradeDeductionHandler;
    private final ListEmployeesHandler listEmployeesHandler;
    private final GetEmployeeDetailsHandler getEmployeeDetailsHandler;
    private final ListSalaryGradesHandler listSalaryGradesHandler;

    public HrStaffController(
            OnboardEmployeeHandler onboardEmployeeHandler,
            UpdateEmployeeProfileHandler updateEmployeeProfileHandler,
            PromoteEmployeeHandler promoteEmployeeHandler,
            SuspendEmployeeHandler suspendEmployeeHandler,
            ReinstateEmployeeHandler reinstateEmployeeHandler,
            TerminateEmployeeHandler terminateEmployeeHandler,
            AddEmployeeBankHandler addEmployeeBankHandler,
            AddEmployeeAllowanceHandler addEmployeeAllowanceHandler,
            AddEmployeeDeductionHandler addEmployeeDeductionHandler,
            LogInfractionHandler logInfractionHandler,
            ResolveInfractionHandler resolveInfractionHandler,
            CreateSalaryGradeHandler createSalaryGradeHandler,
            AddGradeAllowanceHandler addGradeAllowanceHandler,
            AddGradeDeductionHandler addGradeDeductionHandler,
            ListEmployeesHandler listEmployeesHandler,
            GetEmployeeDetailsHandler getEmployeeDetailsHandler,
            ListSalaryGradesHandler listSalaryGradesHandler
    ) {
        this.onboardEmployeeHandler = onboardEmployeeHandler;
        this.updateEmployeeProfileHandler = updateEmployeeProfileHandler;
        this.promoteEmployeeHandler = promoteEmployeeHandler;
        this.suspendEmployeeHandler = suspendEmployeeHandler;
        this.reinstateEmployeeHandler = reinstateEmployeeHandler;
        this.terminateEmployeeHandler = terminateEmployeeHandler;
        this.addEmployeeBankHandler = addEmployeeBankHandler;
        this.addEmployeeAllowanceHandler = addEmployeeAllowanceHandler;
        this.addEmployeeDeductionHandler = addEmployeeDeductionHandler;
        this.logInfractionHandler = logInfractionHandler;
        this.resolveInfractionHandler = resolveInfractionHandler;
        this.createSalaryGradeHandler = createSalaryGradeHandler;
        this.addGradeAllowanceHandler = addGradeAllowanceHandler;
        this.addGradeDeductionHandler = addGradeDeductionHandler;
        this.listEmployeesHandler = listEmployeesHandler;
        this.getEmployeeDetailsHandler = getEmployeeDetailsHandler;
        this.listSalaryGradesHandler = listSalaryGradesHandler;
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAuthority('hr:employees:onboard')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Onboard a new employee")
    public ResponseEntity<ApiResponse<OnboardEmployeeResponse>> onboardEmployee(
            @Valid @RequestBody OnboardEmployeeRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long orgId = request.organizationId() != null ? request.organizationId() : principal.activeOrganizationId();
        CurrencyCode currency = request.currency() != null
                ? CurrencyCode.valueOf(request.currency().toUpperCase())
                : CurrencyCode.NGN;

        OnboardEmployeeCommand command = new OnboardEmployeeCommand(
                orgId,
                request.userId(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.dateOfBirth(),
                request.address(),
                request.nationality(),
                request.hireDate(),
                request.designation(),
                request.department(),
                request.reportingManagerId(),
                request.salaryGradeId(),
                Money.of(request.baseSalary(), currency)
        );

        OnboardEmployeeResponse response = onboardEmployeeHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Employee onboarded successfully", response, null));
    }

    @GetMapping("/employees")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get employee details or list employees")
    public ResponseEntity<ApiResponse<?>> getEmployees(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        if (id != null) {
            EmployeeDetailsResult result = getEmployeeDetailsHandler.execute(
                    new GetEmployeeDetailsQuery(id, principal.activeOrganizationId())
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "Employee details retrieved successfully", result, null));
        }

        PageResult<EmployeeResult> result = listEmployeesHandler.execute(
                new ListEmployeesQuery(principal.activeOrganizationId(), status, department, page, size)
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Employees retrieved successfully", result, null));
    }

    @PutMapping("/employees")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update employee bio-data")
    public ResponseEntity<ApiResponse<Void>> updateEmployeeProfile(
            @RequestParam Long id,
            @Valid @RequestBody UpdateEmployeeProfileRequest request
    ) {
        UpdateEmployeeProfileCommand command = new UpdateEmployeeProfileCommand(
                id,
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.address(),
                request.nationality()
        );
        updateEmployeeProfileHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee profile updated successfully", null, null));
    }

    @PostMapping("/employees/{id}/promote")
    @PreAuthorize("hasAuthority('hr:employees:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Promote an employee")
    public ResponseEntity<ApiResponse<Void>> promoteEmployee(
            @PathVariable Long id,
            @Valid @RequestBody PromoteEmployeeRequest request
    ) {
        CurrencyCode currency = request.currency() != null
                ? CurrencyCode.valueOf(request.currency().toUpperCase())
                : CurrencyCode.NGN;

        PromoteEmployeeCommand command = new PromoteEmployeeCommand(
                id,
                request.newDesignation(),
                request.newGradeId(),
                Money.of(request.newBaseSalary(), currency)
        );
        promoteEmployeeHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee promoted successfully", null, null));
    }

    @PostMapping("/employees/{id}/suspend")
    @PreAuthorize("hasAuthority('hr:employees:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Suspend an employee")
    public ResponseEntity<ApiResponse<Void>> suspendEmployee(
            @PathVariable Long id,
            @Valid @RequestBody SuspendEmployeeRequest request
    ) {
        suspendEmployeeHandler.execute(new SuspendEmployeeCommand(id, request.reason()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee suspended successfully", null, null));
    }

    @PostMapping("/employees/{id}/reinstate")
    @PreAuthorize("hasAuthority('hr:employees:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reinstate a suspended employee")
    public ResponseEntity<ApiResponse<Void>> reinstateEmployee(@PathVariable Long id) {
        reinstateEmployeeHandler.execute(new ReinstateEmployeeCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee reinstated successfully", null, null));
    }

    @PostMapping("/employees/{id}/terminate")
    @PreAuthorize("hasAuthority('hr:employees:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Terminate an employee")
    public ResponseEntity<ApiResponse<Void>> terminateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody TerminateEmployeeRequest request
    ) {
        terminateEmployeeHandler.execute(new TerminateEmployeeCommand(id, request.terminationDate()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee terminated successfully", null, null));
    }

    @PostMapping("/employees/{id}/banks")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add bank account for an employee")
    public ResponseEntity<ApiResponse<AddEmployeeBankResponse>> addEmployeeBank(
            @PathVariable Long id,
            @Valid @RequestBody AddEmployeeBankRequest request
    ) {
        AddEmployeeBankCommand command = new AddEmployeeBankCommand(
                id,
                request.bankCode(),
                request.bankName(),
                request.accountNumber(),
                request.setPrimary()
        );
        AddEmployeeBankResponse response = addEmployeeBankHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Employee bank added successfully", response, null));
    }

    @PostMapping("/employees/{id}/allowances")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add customized allowance for an employee")
    public ResponseEntity<ApiResponse<Void>> addEmployeeAllowance(
            @PathVariable Long id,
            @Valid @RequestBody AddEmployeeAllowanceRequest request
    ) {
        AddEmployeeAllowanceCommand command = new AddEmployeeAllowanceCommand(
                id,
                request.name(),
                request.type(),
                request.value(),
                request.effectiveFrom(),
                request.effectiveTo()
        );
        addEmployeeAllowanceHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Employee allowance added successfully", null, null));
    }

    @PostMapping("/employees/{id}/deductions")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add customized deduction for an employee")
    public ResponseEntity<ApiResponse<Void>> addEmployeeDeduction(
            @PathVariable Long id,
            @Valid @RequestBody AddEmployeeDeductionRequest request
    ) {
        AddEmployeeDeductionCommand command = new AddEmployeeDeductionCommand(
                id,
                request.name(),
                request.type(),
                request.value(),
                request.effectiveFrom(),
                request.effectiveTo(),
                request.loanId()
        );
        addEmployeeDeductionHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Employee deduction added successfully", null, null));
    }

    @PostMapping("/employees/{id}/infractions")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Log an infraction for an employee")
    public ResponseEntity<ApiResponse<LogInfractionResponse>> logInfraction(
            @PathVariable Long id,
            @Valid @RequestBody LogInfractionRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        LogInfractionCommand command = new LogInfractionCommand(
                id,
                principal.activeOrganizationId(),
                request.description(),
                request.severity(),
                principal.userId()
        );
        LogInfractionResponse response = logInfractionHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Infraction logged successfully", response, null));
    }

    @PutMapping("/infractions/{id}/resolve")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Resolve an infraction")
    public ResponseEntity<ApiResponse<Void>> resolveInfraction(
            @PathVariable Long id,
            @Valid @RequestBody ResolveInfractionRequest request
    ) {
        resolveInfractionHandler.execute(new ResolveInfractionCommand(id, request.resolutionNotes()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Infraction resolved successfully", null, null));
    }

    @PostMapping("/salary-grades")
    @PreAuthorize("hasAuthority('hr:employees:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a salary grade")
    public ResponseEntity<ApiResponse<CreateSalaryGradeResponse>> createSalaryGrade(
            @Valid @RequestBody CreateSalaryGradeRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long orgId = request.organizationId() != null ? request.organizationId() : principal.activeOrganizationId();
        CurrencyCode currency = request.currency() != null
                ? CurrencyCode.valueOf(request.currency().toUpperCase())
                : CurrencyCode.NGN;

        CreateSalaryGradeCommand command = new CreateSalaryGradeCommand(
                orgId,
                request.name(),
                Money.of(request.baseSalary(), currency)
        );
        CreateSalaryGradeResponse response = createSalaryGradeHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Salary grade created successfully", response, null));
    }

    @GetMapping("/salary-grades")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List salary grades for active organization")
    public ResponseEntity<ApiResponse<List<SalaryGradeResult>>> listSalaryGrades(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        List<SalaryGradeResult> results = listSalaryGradesHandler.execute(
                new ListSalaryGradesQuery(principal.activeOrganizationId())
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Salary grades retrieved successfully", results, null));
    }

    @PostMapping("/salary-grades/{id}/allowances")
    @PreAuthorize("hasAuthority('hr:employees:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add an allowance to a salary grade")
    public ResponseEntity<ApiResponse<Void>> addGradeAllowance(
            @PathVariable Long id,
            @Valid @RequestBody AddGradeAllowanceRequest request
    ) {
        AddGradeAllowanceCommand command = new AddGradeAllowanceCommand(
                id,
                request.name(),
                request.type(),
                request.value()
        );
        addGradeAllowanceHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Grade allowance added successfully", null, null));
    }

    @PostMapping("/salary-grades/{id}/deductions")
    @PreAuthorize("hasAuthority('hr:employees:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add a deduction to a salary grade")
    public ResponseEntity<ApiResponse<Void>> addGradeDeduction(
            @PathVariable Long id,
            @Valid @RequestBody AddGradeDeductionRequest request
    ) {
        AddGradeDeductionCommand command = new AddGradeDeductionCommand(
                id,
                request.name(),
                request.type(),
                request.value(),
                request.isStatutory()
        );
        addGradeDeductionHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Grade deduction added successfully", null, null));
    }
}
