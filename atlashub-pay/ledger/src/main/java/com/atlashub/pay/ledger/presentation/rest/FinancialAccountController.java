package com.atlashub.pay.ledger.presentation.rest;

import com.atlashub.pay.ledger.application.commands.CreateBusinessAccount.CreateBusinessAccountCommand;
import com.atlashub.pay.ledger.application.commands.CreateBusinessAccount.CreateBusinessAccountHandler;
import com.atlashub.pay.ledger.application.queries.FinancialAccountResults.FinancialAccountResult;
import com.atlashub.pay.ledger.application.queries.GetFinancialAccount.GetFinancialAccountHandler;
import com.atlashub.pay.ledger.application.queries.GetFinancialAccount.GetFinancialAccountQuery;
import com.atlashub.pay.ledger.application.queries.ListFinancialAccounts.ListFinancialAccountsHandler;
import com.atlashub.pay.ledger.application.queries.ListFinancialAccounts.ListFinancialAccountsQuery;
import com.atlashub.pay.ledger.presentation.dto.CreateBusinessAccountRequest;
import com.atlashub.pay.ledger.presentation.dto.CreateBusinessAccountResponse;
import com.atlashub.pay.ledger.presentation.dto.ExternalBankAccountResponse;
import com.atlashub.pay.ledger.presentation.dto.FinancialAccountResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pay/accounts")
@Tag(name = "Financial Accounts")
@SecurityRequirement(name = "bearerAuth")
public class FinancialAccountController {
    private final ListFinancialAccountsHandler listHandler;
    private final GetFinancialAccountHandler getHandler;
    private final CreateBusinessAccountHandler createHandler;

    public FinancialAccountController(ListFinancialAccountsHandler listHandler, GetFinancialAccountHandler getHandler,
                                      CreateBusinessAccountHandler createHandler) {
        this.listHandler = listHandler;
        this.getHandler = getHandler;
        this.createHandler = createHandler;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FinancialAccountResponse>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        var accounts = listHandler.execute(new ListFinancialAccountsQuery(
                principal.activeOrganizationId(), principal.environment())).stream().map(this::map).toList();
        return ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialAccountResponse>> get(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable Long id) {
        return ok(map(getHandler.execute(new GetFinancialAccountQuery(
                principal.activeOrganizationId(), principal.environment(), id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateBusinessAccountResponse>> create(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateBusinessAccountRequest request) {
        var result = createHandler.execute(new CreateBusinessAccountCommand(principal.activeOrganizationId(),
                principal.userId(), principal.environment(), request.name(), request.accountType(), request.currency()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Account created",
                new CreateBusinessAccountResponse(result.accountId()), null));
    }

    private FinancialAccountResponse map(FinancialAccountResult result) {
        ExternalBankAccountResponse bankAccount = result.bankAccount() == null ? null : new ExternalBankAccountResponse(
                result.bankAccount().id(), result.bankAccount().accountName(), result.bankAccount().maskedAccountNumber(),
                result.bankAccount().bankName(), result.bankAccount().bankCode(), result.bankAccount().currency(),
                result.bankAccount().status(), result.bankAccount().activatedAt());
        return new FinancialAccountResponse(result.id(), result.name(), result.type(), result.scope(),
                result.currency(), result.status(), result.restrictions(), result.balance(), result.balanceAsOf(),
                bankAccount, result.createdAt());
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", data, null));
    }
}
