package com.atlashub.accounting.cash.presentation.rest;

import com.atlashub.accounting.cash.application.command.FinalizeReconciliation.FinalizeReconciliationCommand;
import com.atlashub.accounting.cash.application.command.FinalizeReconciliation.FinalizeReconciliationHandler;
import com.atlashub.accounting.cash.application.command.MatchReconciliationItem.MatchReconciliationItemCommand;
import com.atlashub.accounting.cash.application.command.MatchReconciliationItem.MatchReconciliationItemHandler;
import com.atlashub.accounting.cash.application.command.StartBankReconciliation.BankStatementLineInput;
import com.atlashub.accounting.cash.application.command.StartBankReconciliation.StartBankReconciliationCommand;
import com.atlashub.accounting.cash.application.command.StartBankReconciliation.StartBankReconciliationHandler;
import com.atlashub.accounting.cash.application.query.GetBankReconciliation.BankReconciliationResult;
import com.atlashub.accounting.cash.application.query.GetBankReconciliation.GetBankReconciliationHandler;
import com.atlashub.accounting.cash.application.query.GetBankReconciliation.GetBankReconciliationQuery;
import com.atlashub.accounting.cash.application.query.ListBankReconciliations.BankReconciliationSummaryResult;
import com.atlashub.accounting.cash.application.query.ListBankReconciliations.ListBankReconciliationsHandler;
import com.atlashub.accounting.cash.application.query.ListBankReconciliations.ListBankReconciliationsQuery;
import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.accounting.cash.presentation.dto.BankReconciliationResponse;
import com.atlashub.accounting.cash.presentation.dto.BankStatementLineResponse;
import com.atlashub.accounting.cash.presentation.dto.FinalizeReconciliationRequest;
import com.atlashub.accounting.cash.presentation.dto.MatchReconciliationItemRequest;
import com.atlashub.accounting.cash.presentation.dto.MatchedItemResponse;
import com.atlashub.accounting.cash.presentation.dto.StartBankReconciliationRequest;
import com.atlashub.accounting.cash.presentation.dto.StartBankReconciliationResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bank-reconciliations")
@Tag(name = "Bank Reconciliations", description = "Bank statement to GL reconciliation management")
@Validated
public class BankReconciliationController {

    private final StartBankReconciliationHandler startBankReconciliationHandler;
    private final MatchReconciliationItemHandler matchReconciliationItemHandler;
    private final FinalizeReconciliationHandler finalizeReconciliationHandler;
    private final GetBankReconciliationHandler getBankReconciliationHandler;
    private final ListBankReconciliationsHandler listBankReconciliationsHandler;

    public BankReconciliationController(StartBankReconciliationHandler startBankReconciliationHandler,
                                        MatchReconciliationItemHandler matchReconciliationItemHandler,
                                        FinalizeReconciliationHandler finalizeReconciliationHandler,
                                        GetBankReconciliationHandler getBankReconciliationHandler,
                                        ListBankReconciliationsHandler listBankReconciliationsHandler) {
        this.startBankReconciliationHandler = startBankReconciliationHandler;
        this.matchReconciliationItemHandler = matchReconciliationItemHandler;
        this.finalizeReconciliationHandler = finalizeReconciliationHandler;
        this.getBankReconciliationHandler = getBankReconciliationHandler;
        this.listBankReconciliationsHandler = listBankReconciliationsHandler;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('accounting:accounts:create')")
    @Operation(summary = "Start a new bank reconciliation", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StartBankReconciliationResponse>> startReconciliation(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody StartBankReconciliationRequest request) {
        CurrencyCode currency = CurrencyCode.valueOf(request.currency());
        Money closingBalance = Money.of(request.statementClosingBalance(), currency);

        List<BankStatementLineInput> lines = request.bankLines().stream()
                .map(line -> new BankStatementLineInput(
                        line.transactionDate(),
                        line.description(),
                        Money.of(line.amount(), currency),
                        line.type()
                ))
                .toList();

        Long reconciliationId = startBankReconciliationHandler.execute(new StartBankReconciliationCommand(
                principal.activeOrganizationId(),
                request.bankAccountCode(),
                request.statementDate(),
                closingBalance,
                lines
        ));

        return ok(new StartBankReconciliationResponse(reconciliationId));
    }

    @GetMapping(params = "id")
    @Operation(summary = "Get bank reconciliation by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<BankReconciliationResponse>> getReconciliationById(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam @Positive Long id) {
        BankReconciliationResult result = getBankReconciliationHandler.execute(
                new GetBankReconciliationQuery(principal.activeOrganizationId(), id));

        List<MatchedItemResponse> matchedItems = result.matchedItems().stream()
                .map(item -> new MatchedItemResponse(
                        item.id(),
                        item.reconciliationId(),
                        item.bankStatementLineId(),
                        item.journalLineId()
                ))
                .toList();

        List<BankStatementLineResponse> unmatchedBankItems = result.unmatchedBankItems().stream()
                .map(line -> new BankStatementLineResponse(
                        line.id(),
                        line.reconciliationId(),
                        line.transactionDate(),
                        line.description(),
                        line.amount().amount(),
                        line.amount().currency().name(),
                        line.type()
                ))
                .toList();

        BankReconciliationResponse response = new BankReconciliationResponse(
                result.id(),
                result.organizationId(),
                result.bankAccountCode(),
                result.statementDate(),
                result.statementClosingBalance().amount(),
                result.bookBalance().amount(),
                result.difference().amount(),
                result.statementClosingBalance().currency().name(),
                result.status(),
                matchedItems,
                unmatchedBankItems,
                result.unmatchedBookItems()
        );

        return ok(response);
    }

    @GetMapping
    @Operation(summary = "List bank reconciliations for organization", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<BankReconciliationSummaryResult>>> listReconciliations(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) ReconciliationStatus status) {
        List<BankReconciliationSummaryResult> results = listBankReconciliationsHandler.execute(
                new ListBankReconciliationsQuery(principal.activeOrganizationId(), status));
        return ok(results);
    }

    @PostMapping("/{id}/match")
    @Operation(summary = "Match bank statement line with GL journal line", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> matchItem(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long id,
            @Valid @RequestBody MatchReconciliationItemRequest request) {
        matchReconciliationItemHandler.execute(new MatchReconciliationItemCommand(
                principal.activeOrganizationId(),
                id,
                request.bankLineId(),
                request.journalLineId()
        ));
        return done("Reconciliation item matched");
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAuthority('accounting:accounts:create')")
    @Operation(summary = "Finalize bank reconciliation", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> finalizeReconciliation(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long id,
            @Valid @RequestBody(required = false) FinalizeReconciliationRequest request) {
        boolean forceFinalize = request != null && request.forceFinalize();
        finalizeReconciliationHandler.execute(new FinalizeReconciliationCommand(
                principal.activeOrganizationId(),
                id,
                forceFinalize
        ));
        return done("Bank reconciliation finalized");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
