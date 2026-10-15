package com.atlashub.accounting.ar.presentation.rest;

import com.atlashub.accounting.ar.application.command.CreateCustomerReceivable.CreateCustomerReceivableCommand;
import com.atlashub.accounting.ar.application.command.CreateCustomerReceivable.CreateCustomerReceivableHandler;
import com.atlashub.accounting.ar.application.command.RecordReceivablePayment.RecordReceivablePaymentCommand;
import com.atlashub.accounting.ar.application.command.RecordReceivablePayment.RecordReceivablePaymentHandler;
import com.atlashub.accounting.ar.application.command.WriteOffReceivable.WriteOffReceivableCommand;
import com.atlashub.accounting.ar.application.command.WriteOffReceivable.WriteOffReceivableHandler;
import com.atlashub.accounting.ar.application.query.GetOpenReceivables.CustomerReceivableResult;
import com.atlashub.accounting.ar.application.query.GetOpenReceivables.GetOpenReceivablesHandler;
import com.atlashub.accounting.ar.application.query.GetOpenReceivables.GetOpenReceivablesQuery;
import com.atlashub.accounting.ar.presentation.dto.CreateCustomerReceivableRequest;
import com.atlashub.accounting.ar.presentation.dto.CustomerReceivableResponse;
import com.atlashub.accounting.ar.presentation.dto.RecordReceivablePaymentRequest;
import com.atlashub.accounting.ar.presentation.dto.WriteOffRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/receivables")
@Tag(name = "Accounts Receivable", description = "Customer receivable and debt management")
@Validated
public class AccountingArController {

    private final CreateCustomerReceivableHandler createHandler;
    private final RecordReceivablePaymentHandler paymentHandler;
    private final WriteOffReceivableHandler writeOffHandler;
    private final GetOpenReceivablesHandler getOpenHandler;

    public AccountingArController(CreateCustomerReceivableHandler createHandler,
                                  RecordReceivablePaymentHandler paymentHandler,
                                  WriteOffReceivableHandler writeOffHandler,
                                  GetOpenReceivablesHandler getOpenHandler) {
        this.createHandler = createHandler;
        this.paymentHandler = paymentHandler;
        this.writeOffHandler = writeOffHandler;
        this.getOpenHandler = getOpenHandler;
    }

    @GetMapping
    @Operation(summary = "Get open receivables", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<CustomerReceivableResponse>>> getOpenReceivables(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        List<CustomerReceivableResult> results = getOpenHandler.execute(
                new GetOpenReceivablesQuery(principal.activeOrganizationId())
        );

        List<CustomerReceivableResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();

        return ok(responses);
    }

    @PostMapping
    @Operation(summary = "Create customer receivable", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Long>> createReceivable(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateCustomerReceivableRequest request) {
        CurrencyCode currency = (request.currency() != null && !request.currency().isBlank())
                ? CurrencyCode.valueOf(request.currency())
                : CurrencyCode.NGN;
        Money amount = Money.of(request.amount(), currency);

        Long receivableId = createHandler.execute(new CreateCustomerReceivableCommand(
                principal.activeOrganizationId(),
                request.customerId(),
                request.salesOrderId(),
                amount,
                request.dueDate()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Customer receivable created successfully", receivableId, null));
    }

    @PostMapping("/{id}/payment")
    @Operation(summary = "Record receivable payment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> recordPayment(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long id,
            @Valid @RequestBody RecordReceivablePaymentRequest request) {
        CurrencyCode currency = (request.currency() != null && !request.currency().isBlank())
                ? CurrencyCode.valueOf(request.currency())
                : CurrencyCode.NGN;
        Money payment = Money.of(request.amount(), currency);

        paymentHandler.execute(new RecordReceivablePaymentCommand(id, payment));
        return done("Payment recorded successfully");
    }

    @PostMapping("/{id}/write-off")
    @PreAuthorize("hasAuthority('accounting:journal:approve')")
    @Operation(summary = "Write off receivable", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> writeOff(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long id,
            @Valid @RequestBody WriteOffRequest request) {
        writeOffHandler.execute(new WriteOffReceivableCommand(id, request.reason()));
        return done("Receivable written off successfully");
    }

    private CustomerReceivableResponse toResponse(CustomerReceivableResult result) {
        return new CustomerReceivableResponse(
                result.id(),
                result.organizationId(),
                result.customerId(),
                result.salesOrderId(),
                result.totalAmount().amount(),
                result.paidAmount().amount(),
                result.outstandingBalance().amount(),
                result.totalAmount().currency().name(),
                result.dueDate(),
                result.status(),
                result.createdAt()
        );
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
