package com.atlaspay.accounts.presentation.rest.controller;

import com.atlaspay.accounts.application.command.IssueVirtualAccountCommand;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.atlaspay.accounts.application.query.GetVirtualAccountsQuery;
import com.atlaspay.accounts.application.usecase.IssueVirtualAccountUseCase;
import com.atlaspay.accounts.application.usecase.GetVirtualAccountsUseCase;
import com.atlaspay.accounts.application.usecase.ForceCloseAccountsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.atlaspay.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/dedicated_account")
@Tag(name = "Dedicated Accounts", description = "Dedicated virtual account management")
@RequiredArgsConstructor
public class VirtualAccountController {

    private final IssueVirtualAccountUseCase issueVirtualAccountUseCase;
    private final GetVirtualAccountsUseCase getVirtualAccountsUseCase;
    private final ForceCloseAccountsUseCase forceCloseAccountsUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> issueCustomerAccount(
            @RequestHeader("X-Merchant-Id") String merchantId,
            @RequestBody IssueVirtualAccountCommand command) {
        
        var accountId = issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                Long.valueOf(merchantId),
                command.customerCode(),
                command.accountName(),
                command.bankName(),
                command.currency(),
                command.idempotencyKey()
        ));
        
        return ResponseEntity.accepted().body(new ApiResponse<>(true, "Virtual account issued successfully", String.valueOf(accountId), null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> listAccounts(@RequestHeader("X-Merchant-Id") String merchantId) {
        var accounts = getVirtualAccountsUseCase.execute(new GetVirtualAccountsQuery(Long.valueOf(merchantId)));
        return ResponseEntity.ok(new ApiResponse<>(true, "Accounts retrieved successfully", accounts, null));
    }
}

