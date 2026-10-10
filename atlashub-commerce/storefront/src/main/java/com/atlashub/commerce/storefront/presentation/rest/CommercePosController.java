package com.atlashub.commerce.storefront.presentation.rest;

import com.atlashub.commerce.storefront.application.commands.CloseTill.CloseTillCommand;
import com.atlashub.commerce.storefront.application.commands.CloseTill.CloseTillHandler;
import com.atlashub.commerce.storefront.application.commands.OpenTill.OpenTillCommand;
import com.atlashub.commerce.storefront.application.commands.OpenTill.OpenTillHandler;
import com.atlashub.commerce.storefront.application.commands.OpenTill.OpenTillResult;
import com.atlashub.commerce.storefront.application.commands.ProcessCreditSale.ProcessCreditSaleCommand;
import com.atlashub.commerce.storefront.application.commands.ProcessCreditSale.ProcessCreditSaleHandler;
import com.atlashub.commerce.storefront.application.commands.ProcessCreditSale.ProcessCreditSaleResult;
import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.OrderItemDto;
import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.ProcessPosCheckoutCommand;
import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.ProcessPosCheckoutHandler;
import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.ProcessPosCheckoutResult;
import com.atlashub.commerce.storefront.application.commands.RefundPosSale.RefundPosSaleCommand;
import com.atlashub.commerce.storefront.application.commands.RefundPosSale.RefundPosSaleHandler;
import com.atlashub.commerce.storefront.application.queries.GetSalesOrder.SalesOrderResult;
import com.atlashub.commerce.storefront.application.queries.GetTillSummary.GetTillSummaryHandler;
import com.atlashub.commerce.storefront.application.queries.GetTillSummary.GetTillSummaryQuery;
import com.atlashub.commerce.storefront.application.queries.GetTillSummary.TillSummaryResult;
import com.atlashub.commerce.storefront.application.queries.ListPosTransactions.ListPosTransactionsHandler;
import com.atlashub.commerce.storefront.application.queries.ListPosTransactions.ListPosTransactionsQuery;
import com.atlashub.commerce.storefront.presentation.dto.CloseTillRequest;
import com.atlashub.commerce.storefront.presentation.dto.OpenTillRequest;
import com.atlashub.commerce.storefront.presentation.dto.OpenTillResponse;
import com.atlashub.commerce.storefront.presentation.dto.OrderItemRequest;
import com.atlashub.commerce.storefront.presentation.dto.ProcessCreditSaleRequest;
import com.atlashub.commerce.storefront.presentation.dto.ProcessCreditSaleResponse;
import com.atlashub.commerce.storefront.presentation.dto.ProcessPosCheckoutRequest;
import com.atlashub.commerce.storefront.presentation.dto.ProcessPosCheckoutResponse;
import com.atlashub.commerce.storefront.presentation.dto.RefundPosSaleRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Commerce POS", description = "POS checkout, credit sales, refunds, transactions, and till management")
@SecurityRequirement(name = "bearerAuth")
public class CommercePosController {

    private final ProcessPosCheckoutHandler processPosCheckoutHandler;
    private final ProcessCreditSaleHandler processCreditSaleHandler;
    private final RefundPosSaleHandler refundPosSaleHandler;
    private final ListPosTransactionsHandler listPosTransactionsHandler;
    private final OpenTillHandler openTillHandler;
    private final CloseTillHandler closeTillHandler;
    private final GetTillSummaryHandler getTillSummaryHandler;

    public CommercePosController(ProcessPosCheckoutHandler processPosCheckoutHandler,
                                 ProcessCreditSaleHandler processCreditSaleHandler,
                                 RefundPosSaleHandler refundPosSaleHandler,
                                 ListPosTransactionsHandler listPosTransactionsHandler,
                                 OpenTillHandler openTillHandler,
                                 CloseTillHandler closeTillHandler,
                                 GetTillSummaryHandler getTillSummaryHandler) {
        this.processPosCheckoutHandler = Objects.requireNonNull(processPosCheckoutHandler, "ProcessPosCheckoutHandler must not be null");
        this.processCreditSaleHandler = Objects.requireNonNull(processCreditSaleHandler, "ProcessCreditSaleHandler must not be null");
        this.refundPosSaleHandler = Objects.requireNonNull(refundPosSaleHandler, "RefundPosSaleHandler must not be null");
        this.listPosTransactionsHandler = Objects.requireNonNull(listPosTransactionsHandler, "ListPosTransactionsHandler must not be null");
        this.openTillHandler = Objects.requireNonNull(openTillHandler, "OpenTillHandler must not be null");
        this.closeTillHandler = Objects.requireNonNull(closeTillHandler, "CloseTillHandler must not be null");
        this.getTillSummaryHandler = Objects.requireNonNull(getTillSummaryHandler, "GetTillSummaryHandler must not be null");
    }

    @PostMapping("/pos/checkout")
    @PreAuthorize("hasAuthority('commerce:orders:create')")
    @Operation(summary = "Process POS checkout")
    public ResponseEntity<ApiResponse<ProcessPosCheckoutResponse>> checkout(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ProcessPosCheckoutRequest request) {
        List<OrderItemDto> items = request.items().stream()
                .map(this::toOrderItemDto)
                .toList();

        ProcessPosCheckoutCommand command = new ProcessPosCheckoutCommand(
                principal.activeOrganizationId(),
                request.outletId(),
                request.tillId(),
                request.customerId(),
                request.type(),
                items,
                request.discountId(),
                request.paymentMethod(),
                principal.userId()
        );

        ProcessPosCheckoutResult result = processPosCheckoutHandler.execute(command);
        ProcessPosCheckoutResponse response = new ProcessPosCheckoutResponse(result.salesOrderId(), result.status());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "POS checkout processed successfully", response, null));
    }

    @PostMapping("/pos/credit-sale")
    @PreAuthorize("hasAuthority('commerce:orders:create')")
    @Operation(summary = "Process credit sale")
    public ResponseEntity<ApiResponse<ProcessCreditSaleResponse>> processCreditSale(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ProcessCreditSaleRequest request) {
        List<OrderItemDto> items = request.items().stream()
                .map(this::toOrderItemDto)
                .toList();

        ProcessCreditSaleCommand command = new ProcessCreditSaleCommand(
                principal.activeOrganizationId(),
                request.outletId(),
                request.customerId(),
                principal.userId(),
                items
        );

        ProcessCreditSaleResult result = processCreditSaleHandler.execute(command);
        ProcessCreditSaleResponse response = new ProcessCreditSaleResponse(result.salesOrderId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Credit sale processed successfully", response, null));
    }

    @PostMapping("/pos/refund")
    @PreAuthorize("hasAuthority('commerce:orders:create')")
    @Operation(summary = "Refund POS sale")
    public ResponseEntity<ApiResponse<Void>> refundSale(
            @Valid @RequestBody RefundPosSaleRequest request) {
        RefundPosSaleCommand command = new RefundPosSaleCommand(
                request.salesOrderId(),
                request.reason(),
                request.customerNuban(),
                request.customerBankCode()
        );

        refundPosSaleHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "POS sale refunded successfully", null, null));
    }

    @GetMapping("/pos/transactions")
    @Operation(summary = "List POS transactions")
    public ResponseEntity<ApiResponse<PageResult<SalesOrderResult>>> listTransactions(
            @RequestParam Long outletId,
            @RequestParam(required = false) Long tillId,
            @RequestParam(required = false) Long cashierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ListPosTransactionsQuery query = new ListPosTransactionsQuery(
                outletId,
                tillId,
                cashierId,
                from,
                to,
                page,
                size
        );

        PageResult<SalesOrderResult> result = listPosTransactionsHandler.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transactions retrieved successfully", result, null));
    }

    @PostMapping("/tills")
    @PreAuthorize("hasAuthority('commerce:till:open')")
    @Operation(summary = "Open till")
    public ResponseEntity<ApiResponse<OpenTillResponse>> openTill(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody OpenTillRequest request) {
        OpenTillCommand command = new OpenTillCommand(
                principal.activeOrganizationId(),
                request.outletId(),
                request.name(),
                principal.userId(),
                Money.of(request.openingFloat(), request.currency())
        );

        OpenTillResult result = openTillHandler.execute(command);
        OpenTillResponse response = new OpenTillResponse(result.tillId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Till opened successfully", response, null));
    }

    @PostMapping("/tills/{id}/close")
    @PreAuthorize("hasAuthority('commerce:till:open')")
    @Operation(summary = "Close till")
    public ResponseEntity<ApiResponse<Void>> closeTill(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CloseTillRequest request) {
        CloseTillCommand command = new CloseTillCommand(
                id,
                principal.userId(),
                Money.of(request.actualClosingBalance(), request.currency())
        );

        closeTillHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Till closed successfully", null, null));
    }

    @GetMapping("/tills/{id}/summary")
    @Operation(summary = "Get till summary")
    public ResponseEntity<ApiResponse<TillSummaryResult>> getTillSummary(@PathVariable Long id) {
        TillSummaryResult result = getTillSummaryHandler.execute(new GetTillSummaryQuery(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Till summary retrieved successfully", result, null));
    }

    private OrderItemDto toOrderItemDto(OrderItemRequest item) {
        Money taxAmount = item.taxAmount() != null ? Money.of(item.taxAmount(), item.currency()) : null;
        Money discountAmount = item.discountAmount() != null ? Money.of(item.discountAmount(), item.currency()) : null;
        return new OrderItemDto(
                item.productId(),
                item.variantId(),
                item.quantity(),
                Money.of(item.unitPrice(), item.currency()),
                taxAmount,
                discountAmount
        );
    }
}
