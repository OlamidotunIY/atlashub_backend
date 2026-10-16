package com.atlashub.accounting.cash.presentation.rest;

import com.atlashub.accounting.cash.application.command.ConfirmCashEvacuation.ConfirmCashEvacuationCommand;
import com.atlashub.accounting.cash.application.command.ConfirmCashEvacuation.ConfirmCashEvacuationHandler;
import com.atlashub.accounting.cash.application.command.RecordCashEvacuation.RecordCashEvacuationCommand;
import com.atlashub.accounting.cash.application.command.RecordCashEvacuation.RecordCashEvacuationHandler;
import com.atlashub.accounting.cash.presentation.dto.RecordCashEvacuationRequest;
import com.atlashub.accounting.cash.presentation.dto.RecordCashEvacuationResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cash-evacuations")
@Tag(name = "Cash Evacuations", description = "Till to bank cash movement management")
@Validated
public class CashEvacuationController {

    private final RecordCashEvacuationHandler recordCashEvacuationHandler;
    private final ConfirmCashEvacuationHandler confirmCashEvacuationHandler;

    public CashEvacuationController(RecordCashEvacuationHandler recordCashEvacuationHandler,
                                    ConfirmCashEvacuationHandler confirmCashEvacuationHandler) {
        this.recordCashEvacuationHandler = recordCashEvacuationHandler;
        this.confirmCashEvacuationHandler = confirmCashEvacuationHandler;
    }

    @PostMapping
    @Operation(summary = "Record a cash evacuation from till", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RecordCashEvacuationResponse>> recordEvacuation(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody RecordCashEvacuationRequest request) {
        CurrencyCode currency = CurrencyCode.valueOf(request.currency());
        Money amount = Money.of(request.amount(), currency);

        Long evacuationId = recordCashEvacuationHandler.execute(new RecordCashEvacuationCommand(
                principal.activeOrganizationId(),
                request.outletId(),
                request.tillId(),
                amount,
                principal.userId()
        ));

        return ok(new RecordCashEvacuationResponse(evacuationId));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm cash evacuation", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> confirmEvacuation(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long id) {
        confirmCashEvacuationHandler.execute(new ConfirmCashEvacuationCommand(
                principal.activeOrganizationId(),
                id
        ));

        return done("Cash evacuation confirmed");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
