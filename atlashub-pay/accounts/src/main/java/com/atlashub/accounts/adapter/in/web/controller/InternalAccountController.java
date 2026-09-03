package com.atlashub.accounts.adapter.in.web.controller;

import com.atlashub.accounts.application.query.GetInternalAccountQuery;
import com.atlashub.accounts.application.result.InternalAccountDto;
import com.atlashub.accounts.application.usecase.GetInternalAccountUseCase;
import com.atlashub.accounts.domain.valueobject.InternalAccountType;
import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/internal-accounts")
@RequiredArgsConstructor
@Tag(name = "Internal Accounts", description = "Organization Internal Ledger Accounts")
public class InternalAccountController {

    private final GetInternalAccountUseCase getInternalAccountUseCase;

    @Operation(summary = "Get specific internal account for an organization")
    @GetMapping
    public ResponseEntity<ApiResponse<InternalAccountDto>> getInternalAccount(
            @PathVariable Long organizationId,
            @RequestParam InternalAccountType type) {
        
        InternalAccountDto result = getInternalAccountUseCase.execute(new GetInternalAccountQuery(organizationId, type));
        return ResponseEntity.ok(new ApiResponse<>(true, "Internal account retrieved successfully", result, null));
    }
}