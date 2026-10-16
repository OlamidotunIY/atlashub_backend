package com.atlashub.billing.presentation.rest;

import com.atlashub.billing.application.command.VoidInvoice.VoidInvoiceCommand;
import com.atlashub.billing.application.command.VoidInvoice.VoidInvoiceHandler;
import com.atlashub.billing.application.command.VoidInvoice.VoidInvoiceResult;
import com.atlashub.billing.application.query.GetInvoiceDetails.GetInvoiceDetailsHandler;
import com.atlashub.billing.application.query.GetInvoiceDetails.GetInvoiceDetailsQuery;
import com.atlashub.billing.application.query.GetInvoiceDetails.InvoiceDetailsResult;
import com.atlashub.billing.application.query.GetTransactionVolume.GetTransactionVolumeHandler;
import com.atlashub.billing.application.query.GetTransactionVolume.GetTransactionVolumeQuery;
import com.atlashub.billing.application.query.GetTransactionVolume.TransactionVolumeResult;
import com.atlashub.billing.application.query.ListInvoices.InvoiceSummaryResult;
import com.atlashub.billing.application.query.ListInvoices.ListInvoicesHandler;
import com.atlashub.billing.application.query.ListInvoices.ListInvoicesQuery;
import com.atlashub.billing.presentation.dto.InvoiceLineItemResponse;
import com.atlashub.billing.presentation.dto.InvoiceResponse;
import com.atlashub.billing.presentation.dto.InvoiceSummaryResponse;
import com.atlashub.billing.presentation.dto.TransactionVolumeResponse;
import com.atlashub.billing.presentation.dto.VoidInvoiceRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing-invoices")
@Tag(name = "Billing Invoices", description = "Universal subscription and usage fee invoices")
@SecurityRequirement(name = "bearerAuth")
public class BillingInvoiceController {

    private final ListInvoicesHandler listInvoicesHandler;
    private final GetInvoiceDetailsHandler getInvoiceDetailsHandler;
    private final VoidInvoiceHandler voidInvoiceHandler;
    private final GetTransactionVolumeHandler getTransactionVolumeHandler;

    public BillingInvoiceController(
            ListInvoicesHandler listInvoicesHandler,
            GetInvoiceDetailsHandler getInvoiceDetailsHandler,
            VoidInvoiceHandler voidInvoiceHandler,
            GetTransactionVolumeHandler getTransactionVolumeHandler
    ) {
        this.listInvoicesHandler = listInvoicesHandler;
        this.getInvoiceDetailsHandler = getInvoiceDetailsHandler;
        this.voidInvoiceHandler = voidInvoiceHandler;
        this.getTransactionVolumeHandler = getTransactionVolumeHandler;
    }

    @GetMapping
    @Operation(summary = "List invoices for current organization")
    public ResponseEntity<ApiResponse<List<InvoiceSummaryResponse>>> listInvoices(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(name = "status", required = false) String status
    ) {
        List<InvoiceSummaryResult> results = listInvoicesHandler.execute(
                new ListInvoicesQuery(principal.activeOrganizationId(), status)
        );

        List<InvoiceSummaryResponse> response = results.stream()
                .map(res -> new InvoiceSummaryResponse(
                        res.id(),
                        res.organizationId(),
                        res.subscriptionId(),
                        res.invoiceNumber(),
                        res.productCode(),
                        res.periodStart(),
                        res.periodEnd(),
                        res.subtotal(),
                        res.tax(),
                        res.totalAmount(),
                        res.currency(),
                        res.status(),
                        res.dueDate(),
                        res.issuedAt(),
                        res.paidAt()
                ))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Invoices retrieved successfully", response, null));
    }

    @GetMapping("/details")
    @Operation(summary = "Get detailed invoice by ID")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceDetails(
            @RequestParam(name = "id") Long invoiceId
    ) {
        InvoiceDetailsResult res = getInvoiceDetailsHandler.execute(
                new GetInvoiceDetailsQuery(invoiceId)
        );

        List<InvoiceLineItemResponse> lineItems = res.lineItems().stream()
                .map(item -> new InvoiceLineItemResponse(
                        item.id(),
                        item.invoiceId(),
                        item.description(),
                        item.quantity(),
                        item.unitPrice(),
                        item.totalAmount(),
                        item.lineItemType()
                ))
                .toList();

        InvoiceResponse response = new InvoiceResponse(
                res.id(),
                res.organizationId(),
                res.subscriptionId(),
                res.invoiceNumber(),
                res.productCode(),
                res.periodStart(),
                res.periodEnd(),
                res.subtotal(),
                res.tax(),
                res.totalAmount(),
                res.currency(),
                res.status(),
                res.dueDate(),
                res.issuedAt(),
                res.paidAt(),
                res.paymentReference(),
                res.paymentCheckoutUrl(),
                lineItems
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Invoice details retrieved successfully", response, null));
    }

    @PostMapping("/void")
    @Operation(summary = "Void an invoice")
    public ResponseEntity<ApiResponse<VoidInvoiceResult>> voidInvoice(
            @RequestParam(name = "id") Long invoiceId,
            @Valid @RequestBody VoidInvoiceRequest request
    ) {
        VoidInvoiceResult result = voidInvoiceHandler.execute(
                new VoidInvoiceCommand(invoiceId, request.reason())
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Invoice voided successfully", result, null));
    }

    @GetMapping("/volume")
    @Operation(summary = "Get transaction volume ledger for an organization and month")
    public ResponseEntity<ApiResponse<TransactionVolumeResponse>> getTransactionVolume(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(name = "month") String month
    ) {
        TransactionVolumeResult result = getTransactionVolumeHandler.execute(
                new GetTransactionVolumeQuery(principal.activeOrganizationId(), month)
        );

        TransactionVolumeResponse response = new TransactionVolumeResponse(
                result.organizationId(),
                result.month(),
                result.totalVolume(),
                result.totalTransactionCount(),
                result.currency(),
                result.lastUpdatedAt()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Transaction volume retrieved successfully", response, null));
    }
}
