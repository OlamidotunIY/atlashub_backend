package com.atlashub.commerce.inventory.presentation.rest;

import com.atlashub.commerce.inventory.application.commands.ApproveStockTransfer.ApproveStockTransferCommand;
import com.atlashub.commerce.inventory.application.commands.ApproveStockTransfer.ApproveStockTransferHandler;
import com.atlashub.commerce.inventory.application.commands.ReceiveStockTransfer.ReceiveStockTransferCommand;
import com.atlashub.commerce.inventory.application.commands.ReceiveStockTransfer.ReceiveStockTransferHandler;
import com.atlashub.commerce.inventory.application.commands.ReceiveStockTransfer.ReceivedTransferItemDto;
import com.atlashub.commerce.inventory.application.commands.RequestStockTransfer.RequestStockTransferCommand;
import com.atlashub.commerce.inventory.application.commands.RequestStockTransfer.RequestStockTransferHandler;
import com.atlashub.commerce.inventory.application.commands.RequestStockTransfer.RequestStockTransferResult;
import com.atlashub.commerce.inventory.application.commands.RequestStockTransfer.TransferItemDto;
import com.atlashub.commerce.inventory.application.queries.ListStockTransfers.ListStockTransfersHandler;
import com.atlashub.commerce.inventory.application.queries.ListStockTransfers.ListStockTransfersQuery;
import com.atlashub.commerce.inventory.application.queries.ListStockTransfers.StockTransferResult;
import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import com.atlashub.commerce.inventory.presentation.dto.ReceiveStockTransferRequest;
import com.atlashub.commerce.inventory.presentation.dto.RequestStockTransferRequest;
import com.atlashub.commerce.inventory.presentation.dto.RequestStockTransferResponse;
import com.atlashub.commerce.inventory.presentation.dto.StockTransferItemResponse;
import com.atlashub.commerce.inventory.presentation.dto.StockTransferResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
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

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/stock-transfers")
@Tag(name = "Stock Transfers", description = "Inter-outlet stock transfer requests, approvals, and receipts")
@SecurityRequirement(name = "bearerAuth")
public class StockTransferController {

    private final RequestStockTransferHandler requestStockTransferHandler;
    private final ApproveStockTransferHandler approveStockTransferHandler;
    private final ReceiveStockTransferHandler receiveStockTransferHandler;
    private final ListStockTransfersHandler listStockTransfersHandler;

    public StockTransferController(RequestStockTransferHandler requestStockTransferHandler,
                                   ApproveStockTransferHandler approveStockTransferHandler,
                                   ReceiveStockTransferHandler receiveStockTransferHandler,
                                   ListStockTransfersHandler listStockTransfersHandler) {
        this.requestStockTransferHandler = Objects.requireNonNull(requestStockTransferHandler, "RequestStockTransferHandler must not be null");
        this.approveStockTransferHandler = Objects.requireNonNull(approveStockTransferHandler, "ApproveStockTransferHandler must not be null");
        this.receiveStockTransferHandler = Objects.requireNonNull(receiveStockTransferHandler, "ReceiveStockTransferHandler must not be null");
        this.listStockTransfersHandler = Objects.requireNonNull(listStockTransfersHandler, "ListStockTransfersHandler must not be null");
    }

    @PostMapping
    @Operation(summary = "Request an inter-outlet stock transfer")
    public ResponseEntity<ApiResponse<RequestStockTransferResponse>> requestTransfer(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody RequestStockTransferRequest request) {
        List<TransferItemDto> items = request.items().stream()
                .map(i -> new TransferItemDto(i.productId(), i.quantity()))
                .toList();

        RequestStockTransferResult result = requestStockTransferHandler.execute(new RequestStockTransferCommand(
                principal.activeOrganizationId(),
                request.sourceOutletId(),
                request.destinationOutletId(),
                items
        ));

        RequestStockTransferResponse response = new RequestStockTransferResponse(result.transferId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Stock transfer requested successfully", response, null));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve an inter-outlet stock transfer")
    public ResponseEntity<ApiResponse<Void>> approveTransfer(@PathVariable Long id) {
        approveStockTransferHandler.execute(new ApproveStockTransferCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock transfer approved successfully", null, null));
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive items for an approved stock transfer at the destination outlet")
    public ResponseEntity<ApiResponse<Void>> receiveTransfer(
            @PathVariable Long id,
            @Valid @RequestBody ReceiveStockTransferRequest request) {
        List<ReceivedTransferItemDto> items = request.items().stream()
                .map(i -> new ReceivedTransferItemDto(i.productId(), i.quantityReceived()))
                .toList();

        receiveStockTransferHandler.execute(new ReceiveStockTransferCommand(id, items));
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock transfer received successfully", null, null));
    }

    @GetMapping
    @Operation(summary = "List stock transfers for an organization")
    public ResponseEntity<ApiResponse<PageResult<StockTransferResponse>>> listTransfers(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<StockTransferResult> results = listStockTransfersHandler.execute(
                new ListStockTransfersQuery(principal.activeOrganizationId(), status, page, size)
        );

        List<StockTransferResponse> content = results.content().stream()
                .map(r -> new StockTransferResponse(
                        r.id(),
                        r.sourceOutletId(),
                        r.destinationOutletId(),
                        r.status(),
                        r.items().stream().map(i -> new StockTransferItemResponse(i.productId(), i.quantityRequested(), i.quantityReceived())).toList(),
                        r.requestedAt(),
                        r.receivedAt()
                ))
                .toList();

        PageResult<StockTransferResponse> response = new PageResult<>(
                content,
                results.pageNumber(),
                results.pageSize(),
                results.totalElements(),
                results.totalPages()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Stock transfers retrieved successfully", response, null));
    }
}
