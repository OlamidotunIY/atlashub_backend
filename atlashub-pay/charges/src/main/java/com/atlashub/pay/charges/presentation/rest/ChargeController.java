package com.atlashub.pay.charges.presentation.rest;

import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeCommand;
import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeHandler;
import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeResult;
import com.atlashub.pay.charges.application.commands.RefundCharge.RefundChargeCommand;
import com.atlashub.pay.charges.application.commands.RefundCharge.RefundChargeHandler;
import com.atlashub.pay.charges.application.commands.RefundCharge.RefundChargeResult;
import com.atlashub.pay.charges.application.queries.GetChargeDetails.ChargeResult;
import com.atlashub.pay.charges.application.queries.GetChargeDetails.GetChargeDetailsHandler;
import com.atlashub.pay.charges.application.queries.GetChargeDetails.GetChargeDetailsQuery;
import com.atlashub.pay.charges.presentation.dto.ChargeDetailsResponse;
import com.atlashub.pay.charges.presentation.dto.InitializeChargeRequest;
import com.atlashub.pay.charges.presentation.dto.InitializeChargeResponse;
import com.atlashub.pay.charges.presentation.dto.RefundChargeRequest;
import com.atlashub.pay.charges.presentation.dto.RefundChargeResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.Money;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pay/charges")
@Tag(name = "Charges", description = "Universal checkout and inbound payment collections")
@SecurityRequirement(name = "bearerAuth")
public class ChargeController {

    private final InitializeChargeHandler initializeChargeHandler;
    private final RefundChargeHandler refundChargeHandler;
    private final GetChargeDetailsHandler getChargeDetailsHandler;

    public ChargeController(InitializeChargeHandler initializeChargeHandler, RefundChargeHandler refundChargeHandler,
                            GetChargeDetailsHandler getChargeDetailsHandler) {
        this.initializeChargeHandler = initializeChargeHandler;
        this.refundChargeHandler = refundChargeHandler;
        this.getChargeDetailsHandler = getChargeDetailsHandler;
    }

    @PostMapping
    @Operation(summary = "Initialize universal checkout charge")
    public ResponseEntity<ApiResponse<InitializeChargeResponse>> initialize(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody InitializeChargeRequest request) {
        InitializeChargeCommand command =
                new InitializeChargeCommand(principal.activeOrganizationId(), principal.apiEnvironment(),
                        request.reference(), Money.of(request.amount(), request.currency()), request.channel(),
                        request.email(), request.sourceSystem(), request.sourceReferenceId(),
                        request.customerReferenceId(), request.terminalAssignmentId(), request.metadata());

        InitializeChargeResult result = initializeChargeHandler.execute(command);
        InitializeChargeResponse response =
                new InitializeChargeResponse(result.chargeId(), result.reference(), result.amount().amount(),
                        result.amount().currency(), result.channel(), result.status(), result.authorizationUrl(),
                        result.accessCode(), result.expiresAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Charge initialized successfully", response, null));
    }

    @PostMapping("/{chargeId}/refunds")
    @Operation(summary = "Request a refund for a successful charge")
    public ResponseEntity<ApiResponse<RefundChargeResponse>> refund(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable Long chargeId,
            @Valid @RequestBody RefundChargeRequest request) {
        RefundChargeCommand command =
                new RefundChargeCommand(chargeId, principal.activeOrganizationId(), principal.apiEnvironment(),
                        request.reason());

        RefundChargeResult result = refundChargeHandler.execute(command);
        RefundChargeResponse response =
                new RefundChargeResponse(result.chargeId(), result.reference(), result.status(), result.reason());

        return ResponseEntity.ok(new ApiResponse<>(true, "Refund initiated successfully", response, null));
    }

    @GetMapping
    @Operation(summary = "Get charge details by ID")
    public ResponseEntity<ApiResponse<ChargeDetailsResponse>> getDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @RequestParam("id") Long id) {
        GetChargeDetailsQuery query =
                new GetChargeDetailsQuery(id, principal.activeOrganizationId(), principal.apiEnvironment());

        ChargeResult result = getChargeDetailsHandler.execute(query);
        ChargeDetailsResponse response =
                new ChargeDetailsResponse(result.id(), result.reference(), result.amount().amount(),
                        result.amount().currency(), result.channel(), result.status(), result.customerReferenceId(),
                        result.providerFee() == null ? null : result.providerFee().amount(), result.authorizationUrl(),
                        result.accessCode(), result.failureMessage(), result.providerRefundReference(),
                        result.refundReason(), result.refundedAt(), result.disputeReference(),
                        result.disputeStatus(), result.disputeReason(), result.successfulAt(), result.expiresAt(),
                        result.createdAt());

        return ResponseEntity.ok(new ApiResponse<>(true, "Success", response, null));
    }
}
