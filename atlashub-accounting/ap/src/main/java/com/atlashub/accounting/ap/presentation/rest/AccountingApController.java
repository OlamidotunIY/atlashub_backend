package com.atlashub.accounting.ap.presentation.rest;

import com.atlashub.accounting.ap.application.command.DisputeSupplierInvoice.DisputeSupplierInvoiceCommand;
import com.atlashub.accounting.ap.application.command.DisputeSupplierInvoice.DisputeSupplierInvoiceHandler;
import com.atlashub.accounting.ap.application.command.RecordSupplierInvoice.RecordSupplierInvoiceCommand;
import com.atlashub.accounting.ap.application.command.RecordSupplierInvoice.RecordSupplierInvoiceHandler;
import com.atlashub.accounting.ap.application.query.ListSupplierInvoices.ListSupplierInvoicesHandler;
import com.atlashub.accounting.ap.application.query.ListSupplierInvoices.ListSupplierInvoicesQuery;
import com.atlashub.accounting.ap.application.query.ListSupplierInvoices.SupplierInvoiceResult;
import com.atlashub.accounting.ap.domain.valueobject.ApStatus;
import com.atlashub.accounting.ap.presentation.dto.DisputeInvoiceRequest;
import com.atlashub.accounting.ap.presentation.dto.RecordSupplierInvoiceRequest;
import com.atlashub.accounting.ap.presentation.dto.SupplierInvoiceResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payables")
@Tag(name = "Accounts Payable", description = "Supplier invoice and payables management")
@Validated
public class AccountingApController {

    private final RecordSupplierInvoiceHandler recordSupplierInvoiceHandler;
    private final DisputeSupplierInvoiceHandler disputeSupplierInvoiceHandler;
    private final ListSupplierInvoicesHandler listSupplierInvoicesHandler;

    public AccountingApController(RecordSupplierInvoiceHandler recordSupplierInvoiceHandler,
                                  DisputeSupplierInvoiceHandler disputeSupplierInvoiceHandler,
                                  ListSupplierInvoicesHandler listSupplierInvoicesHandler) {
        this.recordSupplierInvoiceHandler = recordSupplierInvoiceHandler;
        this.disputeSupplierInvoiceHandler = disputeSupplierInvoiceHandler;
        this.listSupplierInvoicesHandler = listSupplierInvoicesHandler;
    }

    @GetMapping
    @Operation(summary = "List supplier invoices", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<SupplierInvoiceResponse>>> listInvoices(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) ApStatus status) {
        List<SupplierInvoiceResult> results = listSupplierInvoicesHandler.execute(
                new ListSupplierInvoicesQuery(principal.activeOrganizationId(), supplierId, status)
        );

        List<SupplierInvoiceResponse> responseList = results.stream()
                .map(this::toResponse)
                .toList();

        return ok(responseList);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('accounting:accounts:create')")
    @Operation(summary = "Record a supplier invoice", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Long>> recordInvoice(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody RecordSupplierInvoiceRequest request) {
        CurrencyCode currency = (request.currency() != null && !request.currency().isBlank())
                ? CurrencyCode.valueOf(request.currency())
                : CurrencyCode.NGN;
        Money amount = Money.of(request.amount(), currency);

        Long invoiceId = recordSupplierInvoiceHandler.execute(new RecordSupplierInvoiceCommand(
                principal.activeOrganizationId(),
                request.supplierId(),
                request.purchaseOrderId(),
                request.invoiceNumber(),
                amount,
                request.dueDate()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Supplier invoice recorded successfully", invoiceId, null));
    }

    @PostMapping("/{id}/dispute")
    @Operation(summary = "Dispute a supplier invoice", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> disputeInvoice(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long id,
            @Valid @RequestBody DisputeInvoiceRequest request) {
        disputeSupplierInvoiceHandler.execute(new DisputeSupplierInvoiceCommand(id, request.reason()));
        return done("Supplier invoice disputed successfully");
    }

    private SupplierInvoiceResponse toResponse(SupplierInvoiceResult result) {
        return new SupplierInvoiceResponse(
                result.id(),
                result.organizationId(),
                result.supplierId(),
                result.purchaseOrderId(),
                result.invoiceNumber(),
                result.amount().amount(),
                result.amount().currency().name(),
                result.dueDate(),
                result.status(),
                result.paidAt(),
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
