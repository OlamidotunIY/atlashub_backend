package com.atlashub.accounting.budget.presentation.rest;

import com.atlashub.accounting.budget.application.command.ApproveBudget.ApproveBudgetCommand;
import com.atlashub.accounting.budget.application.command.ApproveBudget.ApproveBudgetHandler;
import com.atlashub.accounting.budget.application.command.CloseBudget.CloseBudgetCommand;
import com.atlashub.accounting.budget.application.command.CloseBudget.CloseBudgetHandler;
import com.atlashub.accounting.budget.application.command.CreateBudget.CreateBudgetCommand;
import com.atlashub.accounting.budget.application.command.CreateBudget.CreateBudgetHandler;
import com.atlashub.accounting.budget.application.query.GetBudgetVariance.BudgetVarianceResult;
import com.atlashub.accounting.budget.application.query.GetBudgetVariance.GetBudgetVarianceHandler;
import com.atlashub.accounting.budget.application.query.GetBudgetVariance.GetBudgetVarianceQuery;
import com.atlashub.accounting.budget.application.query.ListBudgets.BudgetResult;
import com.atlashub.accounting.budget.application.query.ListBudgets.ListBudgetsHandler;
import com.atlashub.accounting.budget.application.query.ListBudgets.ListBudgetsQuery;
import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;
import com.atlashub.accounting.budget.presentation.dto.BudgetResponse;
import com.atlashub.accounting.budget.presentation.dto.BudgetVarianceResponse;
import com.atlashub.accounting.budget.presentation.dto.CreateBudgetRequest;
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
@Tag(name = "Accounting Budgets", description = "Endpoints for managing financial budgets and variances")
public class AccountingBudgetController {

    private final CreateBudgetHandler createBudgetHandler;
    private final ApproveBudgetHandler approveBudgetHandler;
    private final CloseBudgetHandler closeBudgetHandler;
    private final ListBudgetsHandler listBudgetsHandler;
    private final GetBudgetVarianceHandler getBudgetVarianceHandler;

    public AccountingBudgetController(
            CreateBudgetHandler createBudgetHandler,
            ApproveBudgetHandler approveBudgetHandler,
            CloseBudgetHandler closeBudgetHandler,
            ListBudgetsHandler listBudgetsHandler,
            GetBudgetVarianceHandler getBudgetVarianceHandler
    ) {
        this.createBudgetHandler = createBudgetHandler;
        this.approveBudgetHandler = approveBudgetHandler;
        this.closeBudgetHandler = closeBudgetHandler;
        this.listBudgetsHandler = listBudgetsHandler;
        this.getBudgetVarianceHandler = getBudgetVarianceHandler;
    }

    @PostMapping("/budgets")
    @PreAuthorize("hasAuthority('accounting:budgets:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new financial budget")
    public ResponseEntity<ApiResponse<Long>> createBudget(
            @Valid @RequestBody CreateBudgetRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        CurrencyCode currencyCode = CurrencyCode.valueOf(request.currency().toUpperCase());
        CreateBudgetCommand command = new CreateBudgetCommand(
                principal.activeOrganizationId(),
                request.accountId(),
                request.name(),
                request.period(),
                Money.of(request.budgetedAmount(), currencyCode),
                principal.userId()
        );

        Long budgetId = createBudgetHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Budget created successfully", budgetId, null));
    }

    @PostMapping("/budgets/{id}/approve")
    @PreAuthorize("hasAuthority('accounting:budgets:approve')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve a financial budget")
    public ResponseEntity<ApiResponse<Void>> approveBudget(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        ApproveBudgetCommand command = new ApproveBudgetCommand(id, principal.userId());
        approveBudgetHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Budget approved successfully", null, null));
    }

    @PostMapping("/budgets/{id}/close")
    @PreAuthorize("hasAuthority('accounting:budgets:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Close a financial budget")
    public ResponseEntity<ApiResponse<Void>> closeBudget(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        CloseBudgetCommand command = new CloseBudgetCommand(id);
        closeBudgetHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Budget closed successfully", null, null));
    }

    @GetMapping("/budgets")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List financial budgets for organization")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> listBudgets(
            @RequestParam(required = false) BudgetStatus status,
            @RequestParam(required = false) String period,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        List<BudgetResult> results = listBudgetsHandler.execute(
                new ListBudgetsQuery(principal.activeOrganizationId(), status, period)
        );

        List<BudgetResponse> responses = results.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Budgets retrieved successfully", responses, null));
    }

    @GetMapping("/budgets/{id}/variance")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get budget variance details against actuals")
    public ResponseEntity<ApiResponse<BudgetVarianceResponse>> getBudgetVariance(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        BudgetVarianceResult result = getBudgetVarianceHandler.execute(
                new GetBudgetVarianceQuery(id, principal.activeOrganizationId())
        );

        BudgetVarianceResponse response = new BudgetVarianceResponse(
                result.budgetId(),
                result.accountId(),
                result.period(),
                result.budgetedAmount(),
                result.actualAmount(),
                result.variance(),
                result.variancePercentage(),
                result.currency()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Budget variance retrieved successfully", response, null));
    }

    private BudgetResponse toResponse(BudgetResult result) {
        return new BudgetResponse(
                result.id(),
                result.organizationId(),
                result.accountId(),
                result.name(),
                result.period(),
                result.budgetedAmount(),
                result.currency(),
                result.status(),
                result.initiatedBy(),
                result.approvedBy(),
                result.approvedAt()
        );
    }
}
