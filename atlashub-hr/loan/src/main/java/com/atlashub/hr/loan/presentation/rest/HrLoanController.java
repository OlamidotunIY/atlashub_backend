package com.atlashub.hr.loan.presentation.rest;

import com.atlashub.hr.loan.application.command.ApplyForLoan.ApplyForLoanCommand;
import com.atlashub.hr.loan.application.command.ApplyForLoan.ApplyForLoanHandler;
import com.atlashub.hr.loan.application.command.ApproveLoan.ApproveLoanCommand;
import com.atlashub.hr.loan.application.command.ApproveLoan.ApproveLoanHandler;
import com.atlashub.hr.loan.application.command.CancelLoan.CancelLoanCommand;
import com.atlashub.hr.loan.application.command.CancelLoan.CancelLoanHandler;
import com.atlashub.hr.loan.application.query.ListLoans.ListLoansHandler;
import com.atlashub.hr.loan.application.query.ListLoans.ListLoansQuery;
import com.atlashub.hr.loan.application.query.ListLoans.LoanResult;
import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.hr.loan.presentation.dto.ApplyForLoanRequest;
import com.atlashub.hr.loan.presentation.dto.ApplyForLoanResponse;
import com.atlashub.hr.loan.presentation.dto.ApproveLoanRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "HR Loan", description = "Endpoints for employee loan applications and approvals")
public class HrLoanController {

    private final ApplyForLoanHandler applyForLoanHandler;
    private final ApproveLoanHandler approveLoanHandler;
    private final CancelLoanHandler cancelLoanHandler;
    private final ListLoansHandler listLoansHandler;

    public HrLoanController(
            ApplyForLoanHandler applyForLoanHandler,
            ApproveLoanHandler approveLoanHandler,
            CancelLoanHandler cancelLoanHandler,
            ListLoansHandler listLoansHandler
    ) {
        this.applyForLoanHandler = applyForLoanHandler;
        this.approveLoanHandler = approveLoanHandler;
        this.cancelLoanHandler = cancelLoanHandler;
        this.listLoansHandler = listLoansHandler;
    }

    @PostMapping("/loans/apply")
    @Operation(summary = "Apply for a loan", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ApplyForLoanResponse>> applyForLoan(
            @Valid @RequestBody ApplyForLoanRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long organizationId = request.organizationId() != null
                ? request.organizationId()
                : (principal != null ? principal.activeOrganizationId() : null);

        CurrencyCode currency = CurrencyCode.valueOf(request.currency().toUpperCase());
        Money principalAmount = Money.of(request.principalAmount(), currency);
        Money monthlyDeduction = Money.of(request.monthlyDeduction(), currency);

        var result = applyForLoanHandler.execute(new ApplyForLoanCommand(
                request.employeeId(),
                organizationId,
                principalAmount,
                monthlyDeduction
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Loan application submitted successfully", new ApplyForLoanResponse(result.loanId()), null));
    }

    @GetMapping("/loans")
    @Operation(summary = "List loans", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<LoanResult>>> listLoans(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LoanStatus status,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long effectiveOrgId = orgId != null
                ? orgId
                : (principal != null ? principal.activeOrganizationId() : null);

        List<LoanResult> result = listLoansHandler.execute(new ListLoansQuery(effectiveOrgId, employeeId, status));
        return ResponseEntity.ok(new ApiResponse<>(true, "Loans retrieved successfully", result, null));
    }

    @PostMapping("/loans/{id}/approve")
    @PreAuthorize("hasAuthority('hr:loans:approve')")
    @Operation(summary = "Approve a loan", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> approveLoan(
            @PathVariable Long id,
            @Valid @RequestBody ApproveLoanRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long approverId = principal != null ? principal.userId() : 0L;
        approveLoanHandler.execute(new ApproveLoanCommand(id, approverId, request.startDate()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Loan approved successfully", null, null));
    }

    @PostMapping("/loans/{id}/cancel")
    @Operation(summary = "Cancel a loan", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> cancelLoan(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        cancelLoanHandler.execute(new CancelLoanCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Loan cancelled successfully", null, null));
    }
}
