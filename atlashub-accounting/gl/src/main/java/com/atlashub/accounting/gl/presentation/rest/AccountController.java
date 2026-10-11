package com.atlashub.accounting.gl.presentation.rest;

import com.atlashub.accounting.gl.application.command.CreateAccount.CreateAccountCommand;
import com.atlashub.accounting.gl.application.command.CreateAccount.CreateAccountHandler;
import com.atlashub.accounting.gl.application.command.CreateAccount.CreateAccountResult;
import com.atlashub.accounting.gl.application.command.DeactivateAccount.DeactivateAccountCommand;
import com.atlashub.accounting.gl.application.command.DeactivateAccount.DeactivateAccountHandler;
import com.atlashub.accounting.gl.application.query.GetAccountById.AccountResult;
import com.atlashub.accounting.gl.application.query.GetAccountById.GetAccountByIdHandler;
import com.atlashub.accounting.gl.application.query.GetAccountById.GetAccountByIdQuery;
import com.atlashub.accounting.gl.application.query.ListAccounts.AccountSummaryResult;
import com.atlashub.accounting.gl.application.query.ListAccounts.ListAccountsHandler;
import com.atlashub.accounting.gl.application.query.ListAccounts.ListAccountsQuery;
import com.atlashub.accounting.gl.presentation.dto.AccountResponse;
import com.atlashub.accounting.gl.presentation.dto.CreateAccountRequest;
import com.atlashub.accounting.gl.presentation.dto.CreateAccountResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Chart of Accounts", description = "General ledger chart of accounts management")
@Validated
public class AccountController {

    private final CreateAccountHandler createAccountHandler;
    private final DeactivateAccountHandler deactivateAccountHandler;
    private final GetAccountByIdHandler getAccountByIdHandler;
    private final ListAccountsHandler listAccountsHandler;

    public AccountController(CreateAccountHandler createAccountHandler,
                             DeactivateAccountHandler deactivateAccountHandler,
                             GetAccountByIdHandler getAccountByIdHandler, ListAccountsHandler listAccountsHandler) {
        this.createAccountHandler = createAccountHandler;
        this.deactivateAccountHandler = deactivateAccountHandler;
        this.getAccountByIdHandler = getAccountByIdHandler;
        this.listAccountsHandler = listAccountsHandler;
    }

    @PostMapping
    @Operation(summary = "Create an account in Chart of Accounts", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CreateAccountResponse>> createAccount(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateAccountRequest request) {
        CreateAccountResult result = createAccountHandler.execute(
                new CreateAccountCommand(principal.activeOrganizationId(), request.code(), request.name(),
                        request.type(), request.parentAccountId(), Boolean.TRUE.equals(request.isSystemAccount())));

        CreateAccountResponse response =
                new CreateAccountResponse(result.id(), result.organizationId(), result.code(), result.name(),
                        result.type(), result.parentAccountId(), result.isSystemAccount(), result.isActive(),
                        result.createdAt());
        return ok(response);
    }

    @GetMapping(params = "id")
    @Operation(summary = "Get account by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @RequestParam @Positive Long id) {
        AccountResult result =
                getAccountByIdHandler.execute(new GetAccountByIdQuery(id, principal.activeOrganizationId()));

        AccountResponse response =
                new AccountResponse(result.id(), result.organizationId(), result.code(), result.name(), result.type(),
                        result.parentAccountId(), result.isSystemAccount(), result.isActive(), result.createdAt());
        return ok(response);
    }

    @GetMapping
    @Operation(summary = "List accounts for organization", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<AccountResponse>>> listAccounts(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        List<AccountSummaryResult> results =
                listAccountsHandler.execute(new ListAccountsQuery(principal.activeOrganizationId()));

        List<AccountResponse> responses = results.stream()
                .map(r -> new AccountResponse(r.id(), r.organizationId(), r.code(), r.name(), r.type(),
                        r.parentAccountId(), r.isSystemAccount(), r.isActive(), r.createdAt())).toList();
        return ok(responses);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an account", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable @Positive Long id) {
        deactivateAccountHandler.execute(new DeactivateAccountCommand(id, principal.activeOrganizationId()));
        return done("Account deactivated");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
