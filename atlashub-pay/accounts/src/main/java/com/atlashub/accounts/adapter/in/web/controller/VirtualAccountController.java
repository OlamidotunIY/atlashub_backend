package com.atlashub.accounts.adapter.in.web.controller;

import com.atlashub.accounts.application.command.IssueVirtualAccountCommand;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.atlashub.accounts.application.query.GetVirtualAccountsQuery;
import com.atlashub.accounts.application.usecase.IssueVirtualAccountUseCase;
import com.atlashub.accounts.application.usecase.GetVirtualAccountsUseCase;
import com.atlashub.accounts.application.usecase.ForceCloseAccountsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.atlashub.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/dedicated_account")
@Tag(name = "Dedicated Accounts", description = "Dedicated virtual account management")
@RequiredArgsConstructor
public class VirtualAccountController {

    private final IssueVirtualAccountUseCase issueVirtualAccountUseCase;
    private final GetVirtualAccountsUseCase getVirtualAccountsUseCase;
    private final ForceCloseAccountsUseCase forceCloseAccountsUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> issueUserAccount(
            @RequestHeader("X-Organization-Id") String OrganizationId,
            @RequestBody IssueVirtualAccountCommand command) {
        
        var accountId = issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                Long.valueOf(OrganizationId),
                command.UserCode(),
                command.accountName(),
                command.bankName(),
                command.currency(),
                command.idempotencyKey()
        ));
        
        return ResponseEntity.accepted().body(new ApiResponse<>(true, "Virtual account issued successfully", String.valueOf(accountId), null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> listAccounts(@RequestHeader("X-Organization-Id") String OrganizationId) {
        var accounts = getVirtualAccountsUseCase.execute(new GetVirtualAccountsQuery(Long.valueOf(OrganizationId)));
        return ResponseEntity.ok(new ApiResponse<>(true, "Accounts retrieved successfully", accounts, null));
    }
}

