package com.atlashub.pay.accounts.presentation.rest;

import com.atlashub.pay.accounts.application.queries.AccountResults.BusinessBankingResult;
import com.atlashub.pay.accounts.application.queries.GetBusinessBanking.GetBusinessBankingHandler;
import com.atlashub.pay.accounts.application.queries.GetBusinessBanking.GetBusinessBankingQuery;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business-accounts")
@Tag(name = "Business Banking", description = "Organization business banking profile")
public class BusinessBankingController {
    private final GetBusinessBankingHandler handler;

    public BusinessBankingController(GetBusinessBankingHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @Operation(summary = "Get business banking details")
    public ResponseEntity<ApiResponse<BusinessBankingResult>> get(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ok(handler.execute(new GetBusinessBankingQuery(principal.activeOrganizationId(), principal.environment())));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }
}
