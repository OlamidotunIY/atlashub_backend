package com.atlashub.pay.ledger.presentation.rest;

import com.atlashub.pay.ledger.application.queries.GetAccountBalance.AccountBalanceResult;
import com.atlashub.pay.ledger.application.queries.GetAccountBalance.GetAccountBalanceHandler;
import com.atlashub.pay.ledger.application.queries.GetAccountBalance.GetAccountBalanceQuery;
import com.atlashub.pay.ledger.application.queries.GetWalletBalances.GetWalletBalancesHandler;
import com.atlashub.pay.ledger.application.queries.GetWalletBalances.GetWalletBalancesQuery;
import com.atlashub.pay.ledger.application.queries.GetWalletBalances.WalletBalancesResult;
import com.atlashub.pay.ledger.presentation.dto.AccountBalanceResponse;
import com.atlashub.pay.ledger.presentation.dto.WalletBalancesResponse;
import com.atlashub.pay.ledger.application.queries.GetPartyBalance.GetPartyBalanceHandler;
import com.atlashub.pay.ledger.application.queries.GetPartyBalance.GetPartyBalanceQuery;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ledger-accounts")
@Tag(name = "Ledger Accounts", description = "Payment ledger balances")
public class LedgerAccountController {
    private final GetAccountBalanceHandler balanceHandler;
    private final GetWalletBalancesHandler walletBalancesHandler;
    private final GetPartyBalanceHandler partyBalanceHandler;

    public LedgerAccountController(GetAccountBalanceHandler balanceHandler,
                                   GetWalletBalancesHandler walletBalancesHandler,
                                   GetPartyBalanceHandler partyBalanceHandler) {
        this.balanceHandler = balanceHandler;
        this.walletBalancesHandler = walletBalancesHandler;
        this.partyBalanceHandler = partyBalanceHandler;
    }

    @GetMapping("/party-balance")
    @Operation(summary = "Get party ledger balance")
    public ResponseEntity<ApiResponse<AccountBalanceResponse>> partyBalance(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam String partyType,
            @RequestParam String partyReferenceId,
            @RequestParam(defaultValue = "NGN") String currency) {
        AccountBalanceResult result = partyBalanceHandler.execute(new GetPartyBalanceQuery(
                principal.activeOrganizationId(), principal.environment(), partyType, partyReferenceId, currency));
        return ok(new AccountBalanceResponse(result.accountId(), result.accountType(), result.balance(),
                result.currency(), result.asOf()));
    }

    @GetMapping("/balance")
    @Operation(summary = "Get ledger account balance")
    public ResponseEntity<ApiResponse<AccountBalanceResponse>> balance(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam String accountType,
            @RequestParam(defaultValue = "NGN") String currency) {
        AccountBalanceResult result = balanceHandler.execute(new GetAccountBalanceQuery(
                principal.activeOrganizationId(), principal.environment(), accountType, currency));
        return ok(new AccountBalanceResponse(result.accountId(), result.accountType(), result.balance(),
                result.currency(), result.asOf()));
    }

    @GetMapping("/balances")
    @Operation(summary = "Get organization ledger balances")
    public ResponseEntity<ApiResponse<WalletBalancesResponse>> walletBalances(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        WalletBalancesResult result = walletBalancesHandler.execute(
                new GetWalletBalancesQuery(principal.activeOrganizationId(), principal.environment()));
        return ok(new WalletBalancesResponse(
                result.organizationId(), result.balancesByAccountType(), result.currency()));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

}
