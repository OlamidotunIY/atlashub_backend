package com.atlashub.pay.accounts.presentation.rest;

import com.atlashub.pay.accounts.application.commands.ChangeReservedAccountStatus.ChangeReservedAccountStatusCommand;
import com.atlashub.pay.accounts.application.commands.ChangeReservedAccountStatus.ChangeReservedAccountStatusHandler;
import com.atlashub.pay.accounts.application.commands.IssueReservedAccount.IssueReservedAccountCommand;
import com.atlashub.pay.accounts.application.commands.IssueReservedAccount.IssueReservedAccountHandler;
import com.atlashub.pay.accounts.application.queries.AccountResults.ReservedAccountResult;
import com.atlashub.pay.accounts.application.queries.GetReservedAccount.GetReservedAccountHandler;
import com.atlashub.pay.accounts.application.queries.GetReservedAccount.GetReservedAccountQuery;
import com.atlashub.pay.accounts.application.queries.ListReservedAccounts.ListReservedAccountsHandler;
import com.atlashub.pay.accounts.application.queries.ListReservedAccounts.ListReservedAccountsQuery;
import com.atlashub.pay.accounts.domain.ports.AnchorBankingPort.ReservedAccountCustomer;
import com.atlashub.pay.accounts.presentation.dto.IssueReservedAccountRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pay/accounts/reserved")
public class ReservedAccountController {
    private final IssueReservedAccountHandler issueHandler;
    private final ChangeReservedAccountStatusHandler statusHandler;
    private final GetReservedAccountHandler getHandler;
    private final ListReservedAccountsHandler listHandler;

    public ReservedAccountController(IssueReservedAccountHandler issueHandler,
                                     ChangeReservedAccountStatusHandler statusHandler,
                                     GetReservedAccountHandler getHandler,
                                     ListReservedAccountsHandler listHandler) {
        this.issueHandler = issueHandler;
        this.statusHandler = statusHandler;
        this.getHandler = getHandler;
        this.listHandler = listHandler;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> issue(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody IssueReservedAccountRequest request) {
        ReservedAccountCustomer customer = new ReservedAccountCustomer(
                request.customerType(), request.customerReferenceId(), request.fullName(),
                request.email(), request.bvn());
        return ok(issueHandler.execute(new IssueReservedAccountCommand(
                principal.activeOrganizationId(), request.ownerType(), request.ownerReferenceId(),
                customer, request.provider(), idempotencyKey, principal.environment())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<ReservedAccountResult>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) String ownerType,
            @RequestParam(required = false) String ownerReferenceId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ok(listHandler.execute(new ListReservedAccountsQuery(
                principal.activeOrganizationId(), ownerType, ownerReferenceId, status, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservedAccountResult>> get(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long id) {
        return ok(getHandler.execute(new GetReservedAccountQuery(principal.activeOrganizationId(), id)));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspend(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable Long id) {
        statusHandler.execute(new ChangeReservedAccountStatusCommand(
                principal.activeOrganizationId(), id, ChangeReservedAccountStatusCommand.Action.SUSPEND));
        return done("Reserved account suspended");
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<Void>> reactivate(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable Long id) {
        statusHandler.execute(new ChangeReservedAccountStatusCommand(
                principal.activeOrganizationId(), id, ChangeReservedAccountStatusCommand.Action.REACTIVATE));
        return done("Reserved account reactivated");
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<Void>> close(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable Long id) {
        statusHandler.execute(new ChangeReservedAccountStatusCommand(
                principal.activeOrganizationId(), id, ChangeReservedAccountStatusCommand.Action.CLOSE));
        return done("Reserved account closed");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
