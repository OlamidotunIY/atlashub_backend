package com.atlashub.pay.ledger.presentation.rest;

import com.atlashub.pay.ledger.application.queries.GetAccountBalance.AccountBalanceResult;
import com.atlashub.pay.ledger.application.queries.GetAccountBalance.GetAccountBalanceHandler;
import com.atlashub.pay.ledger.application.queries.GetAccountBalance.GetAccountBalanceQuery;
import com.atlashub.pay.ledger.application.queries.GetWalletBalances.GetWalletBalancesHandler;
import com.atlashub.pay.ledger.application.queries.GetWalletBalances.GetWalletBalancesQuery;
import com.atlashub.pay.ledger.application.queries.GetWalletBalances.WalletBalancesResult;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pay/ledger")
public class LedgerAccountController {
    private final GetAccountBalanceHandler balanceHandler;
    private final GetWalletBalancesHandler walletBalancesHandler;

    public LedgerAccountController(GetAccountBalanceHandler balanceHandler,
                                   GetWalletBalancesHandler walletBalancesHandler) {
        this.balanceHandler = balanceHandler;
        this.walletBalancesHandler = walletBalancesHandler;
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<AccountBalanceResult>> balance(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam String accountType) {
        return ok(balanceHandler.execute(new GetAccountBalanceQuery(
                principal.activeOrganizationId(), principal.environment(), accountType)));
    }

    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<WalletBalancesResult>> walletBalances(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ok(walletBalancesHandler.execute(
                new GetWalletBalancesQuery(principal.activeOrganizationId(), principal.environment())));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

}
