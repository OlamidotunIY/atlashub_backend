package com.atlashub.hr.payroll.presentation.rest;

import com.atlashub.hr.payroll.application.command.ApprovePayroll.ApprovePayrollCommand;
import com.atlashub.hr.payroll.application.command.ApprovePayroll.ApprovePayrollHandler;
import com.atlashub.hr.payroll.application.command.InitiatePayroll.InitiatePayrollCommand;
import com.atlashub.hr.payroll.application.command.InitiatePayroll.InitiatePayrollHandler;
import com.atlashub.hr.payroll.application.command.SubmitPayrollForApproval.SubmitPayrollForApprovalCommand;
import com.atlashub.hr.payroll.application.command.SubmitPayrollForApproval.SubmitPayrollForApprovalHandler;
import com.atlashub.hr.payroll.application.query.GetPayslip.GetPayslipHandler;
import com.atlashub.hr.payroll.application.query.GetPayslip.GetPayslipQuery;
import com.atlashub.hr.payroll.application.query.GetPayslip.PayslipResult;
import com.atlashub.hr.payroll.application.query.ListPayrollRuns.ListPayrollRunsHandler;
import com.atlashub.hr.payroll.application.query.ListPayrollRuns.ListPayrollRunsQuery;
import com.atlashub.hr.payroll.application.query.ListPayrollRuns.PayrollRunResult;
import com.atlashub.hr.payroll.application.query.ListPayslipsByEmployee.ListPayslipsByEmployeeHandler;
import com.atlashub.hr.payroll.application.query.ListPayslipsByEmployee.ListPayslipsByEmployeeQuery;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.hr.payroll.presentation.dto.InitiatePayrollRequest;
import com.atlashub.hr.payroll.presentation.dto.InitiatePayrollResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "HR Payroll", description = "Endpoints for monthly payroll initiation, approval, disbursement, and payslips")
public class HrPayrollController {

    private final InitiatePayrollHandler initiatePayrollHandler;
    private final SubmitPayrollForApprovalHandler submitPayrollForApprovalHandler;
    private final ApprovePayrollHandler approvePayrollHandler;
    private final ListPayrollRunsHandler listPayrollRunsHandler;
    private final GetPayslipHandler getPayslipHandler;
    private final ListPayslipsByEmployeeHandler listPayslipsByEmployeeHandler;

    public HrPayrollController(
            InitiatePayrollHandler initiatePayrollHandler,
            SubmitPayrollForApprovalHandler submitPayrollForApprovalHandler,
            ApprovePayrollHandler approvePayrollHandler,
            ListPayrollRunsHandler listPayrollRunsHandler,
            GetPayslipHandler getPayslipHandler,
            ListPayslipsByEmployeeHandler listPayslipsByEmployeeHandler
    ) {
        this.initiatePayrollHandler = initiatePayrollHandler;
        this.submitPayrollForApprovalHandler = submitPayrollForApprovalHandler;
        this.approvePayrollHandler = approvePayrollHandler;
        this.listPayrollRunsHandler = listPayrollRunsHandler;
        this.getPayslipHandler = getPayslipHandler;
        this.listPayslipsByEmployeeHandler = listPayslipsByEmployeeHandler;
    }

    @PostMapping("/payroll/initiate")
    @PreAuthorize("hasAuthority('hr:payroll:initiate')")
    @Operation(summary = "Initiate monthly payroll run", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<InitiatePayrollResponse>> initiatePayroll(
            @Valid @RequestBody InitiatePayrollRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long organizationId = request.organizationId() != null
                ? request.organizationId()
                : (principal != null ? principal.activeOrganizationId() : null);

        Long initiatorUserId = principal != null ? principal.userId() : 0L;
        YearMonth period = YearMonth.parse(request.period());

        var result = initiatePayrollHandler.execute(new InitiatePayrollCommand(
                organizationId,
                period,
                initiatorUserId
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Payroll initiated successfully", new InitiatePayrollResponse(result.payrollRunId()), null));
    }

    @PostMapping("/payroll/{id}/submit")
    @PreAuthorize("hasAuthority('hr:payroll:initiate')")
    @Operation(summary = "Submit payroll run for approval", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> submitPayroll(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long initiatorUserId = principal != null ? principal.userId() : 0L;
        submitPayrollForApprovalHandler.execute(new SubmitPayrollForApprovalCommand(id, initiatorUserId));
        return ResponseEntity.ok(new ApiResponse<>(true, "Payroll submitted for approval successfully", null, null));
    }

    @PostMapping("/payroll/{id}/approve")
    @PreAuthorize("hasAuthority('hr:payroll:approve')")
    @Operation(summary = "Approve payroll run (checker)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> approvePayroll(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long approverUserId = principal != null ? principal.userId() : 0L;
        approvePayrollHandler.execute(new ApprovePayrollCommand(id, approverUserId));
        return ResponseEntity.ok(new ApiResponse<>(true, "Payroll approved and queued for disbursement successfully", null, null));
    }

    @GetMapping("/payroll")
    @Operation(summary = "List payroll runs", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<PayrollRunResult>>> listPayrollRuns(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) PayrollStatus status,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long effectiveOrgId = orgId != null
                ? orgId
                : (principal != null ? principal.activeOrganizationId() : null);

        List<PayrollRunResult> results = listPayrollRunsHandler.execute(new ListPayrollRunsQuery(effectiveOrgId, status));
        return ResponseEntity.ok(new ApiResponse<>(true, "Payroll runs retrieved successfully", results, null));
    }

    @GetMapping("/payslips/{id}")
    @Operation(summary = "Get payslip by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PayslipResult>> getPayslip(
            @PathVariable Long id
    ) {
        PayslipResult result = getPayslipHandler.execute(new GetPayslipQuery(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Payslip retrieved successfully", result, null));
    }

    @GetMapping("/payslips")
    @Operation(summary = "List payslips for employee", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<PayslipResult>>> listPayslips(
            @RequestParam Long employeeId,
            @RequestParam int year
    ) {
        List<PayslipResult> results = listPayslipsByEmployeeHandler.execute(new ListPayslipsByEmployeeQuery(employeeId, year));
        return ResponseEntity.ok(new ApiResponse<>(true, "Payslips retrieved successfully", results, null));
    }
}
