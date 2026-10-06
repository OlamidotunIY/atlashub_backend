package com.atlashub.pay.accounts.presentation.rest;

import com.atlashub.pay.accounts.application.commands.EnablePaymentCapability.EnablePaymentCapabilityCommand;
import com.atlashub.pay.accounts.application.commands.EnablePaymentCapability.EnablePaymentCapabilityHandler;
import com.atlashub.pay.accounts.application.commands.EnablePaymentCapability.EnablePaymentCapabilityResult;
import com.atlashub.pay.accounts.presentation.dto.EnablePaymentCapabilityRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-capabilities")
@Tag(name = "Payment Capabilities", description = "Payment collection and terminal capabilities")
public class PaymentCapabilityController {
    private final EnablePaymentCapabilityHandler enableHandler;

    public PaymentCapabilityController(EnablePaymentCapabilityHandler enableHandler) {
        this.enableHandler = enableHandler;
    }

    @PostMapping("/{capability}/enable")
    @Operation(summary = "Enable a payment capability")
    @PreAuthorize("hasAuthority('pay:capabilities:manage')")
    public ResponseEntity<ApiResponse<EnablePaymentCapabilityResult>> enable(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable String capability,
            @RequestBody(required = false) EnablePaymentCapabilityRequest request
    ) {
        String terminalProvider = request == null ? null : request.terminalProvider();
        return ok(enableHandler.execute(new EnablePaymentCapabilityCommand(
                principal.activeOrganizationId(), principal.environment(), capability, terminalProvider)));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }
}
