package com.atlashub.commerce.catalog.presentation.rest;

import com.atlashub.commerce.catalog.application.commands.CreatePurchaseOrder.CreatePurchaseOrderCommand;
import com.atlashub.commerce.catalog.application.commands.CreatePurchaseOrder.CreatePurchaseOrderHandler;
import com.atlashub.commerce.catalog.application.commands.CreatePurchaseOrder.CreatePurchaseOrderResult;
import com.atlashub.commerce.catalog.application.commands.CreatePurchaseOrder.PurchaseOrderItemDto;
import com.atlashub.commerce.catalog.application.commands.ReceivePurchaseOrder.ReceivePurchaseOrderCommand;
import com.atlashub.commerce.catalog.application.commands.ReceivePurchaseOrder.ReceivePurchaseOrderHandler;
import com.atlashub.commerce.catalog.application.commands.ReceivePurchaseOrder.ReceivedItemDto;
import com.atlashub.commerce.catalog.application.commands.SendPurchaseOrder.SendPurchaseOrderCommand;
import com.atlashub.commerce.catalog.application.commands.SendPurchaseOrder.SendPurchaseOrderHandler;
import com.atlashub.commerce.catalog.application.queries.ListPurchaseOrders.ListPurchaseOrdersHandler;
import com.atlashub.commerce.catalog.application.queries.ListPurchaseOrders.ListPurchaseOrdersQuery;
import com.atlashub.commerce.catalog.application.queries.ListPurchaseOrders.PurchaseOrderResult;
import com.atlashub.commerce.catalog.domain.valueobject.PurchaseOrderStatus;
import com.atlashub.commerce.catalog.presentation.dto.CreatePurchaseOrderRequest;
import com.atlashub.commerce.catalog.presentation.dto.CreatePurchaseOrderResponse;
import com.atlashub.commerce.catalog.presentation.dto.ReceivePurchaseOrderRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
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
@RequestMapping("/api/v1/purchase-orders")
@Tag(name = "Purchase Orders", description = "Purchase order procurement management")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseOrderController {

    private final CreatePurchaseOrderHandler createPurchaseOrderHandler;
    private final SendPurchaseOrderHandler sendPurchaseOrderHandler;
    private final ReceivePurchaseOrderHandler receivePurchaseOrderHandler;
    private final ListPurchaseOrdersHandler listPurchaseOrdersHandler;

    public PurchaseOrderController(CreatePurchaseOrderHandler createPurchaseOrderHandler,
                                   SendPurchaseOrderHandler sendPurchaseOrderHandler,
                                   ReceivePurchaseOrderHandler receivePurchaseOrderHandler,
                                   ListPurchaseOrdersHandler listPurchaseOrdersHandler) {
        this.createPurchaseOrderHandler =
                Objects.requireNonNull(createPurchaseOrderHandler, "CreatePurchaseOrderHandler must not be null");
        this.sendPurchaseOrderHandler =
                Objects.requireNonNull(sendPurchaseOrderHandler, "SendPurchaseOrderHandler must not be null");
        this.receivePurchaseOrderHandler =
                Objects.requireNonNull(receivePurchaseOrderHandler, "ReceivePurchaseOrderHandler must not be null");
        this.listPurchaseOrdersHandler =
                Objects.requireNonNull(listPurchaseOrdersHandler, "ListPurchaseOrdersHandler must not be null");
    }

    @PostMapping
    @Operation(summary = "Create purchase order")
    public ResponseEntity<ApiResponse<CreatePurchaseOrderResponse>> createPurchaseOrder(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreatePurchaseOrderRequest request) {
        CurrencyCode currency = request.currency() != null ? request.currency() : CurrencyCode.NGN;
        List<PurchaseOrderItemDto> items = request.items() != null ? request.items().stream()
                .map(i -> new PurchaseOrderItemDto(i.productId(), i.quantity(),
                        i.unitCost() != null ? Money.of(i.unitCost(), currency) : null)).toList() : List.of();

        CreatePurchaseOrderCommand command =
                new CreatePurchaseOrderCommand(principal.activeOrganizationId(), request.outletId(),
                        request.supplierId(), request.expectedDeliveryDate(), currency, items);
        CreatePurchaseOrderResult result = createPurchaseOrderHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Purchase order created successfully",
                        new CreatePurchaseOrderResponse(result.purchaseOrderId()), null));
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Send purchase order")
    public ResponseEntity<ApiResponse<Void>> sendPurchaseOrder(@PathVariable Long id) {
        sendPurchaseOrderHandler.execute(new SendPurchaseOrderCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Purchase order sent successfully", null, null));
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive items for purchase order")
    public ResponseEntity<ApiResponse<Void>> receivePurchaseOrder(@PathVariable Long id,
                                                                  @Valid @RequestBody ReceivePurchaseOrderRequest request) {
        List<ReceivedItemDto> items =
                request.receivedItems().stream().map(i -> new ReceivedItemDto(i.productId(), i.quantityReceived()))
                        .toList();

        receivePurchaseOrderHandler.execute(new ReceivePurchaseOrderCommand(id, items));
        return ResponseEntity.ok(new ApiResponse<>(true, "Purchase order items received successfully", null, null));
    }

    @GetMapping
    @Operation(summary = "List purchase orders")
    public ResponseEntity<ApiResponse<PageResult<PurchaseOrderResult>>> listPurchaseOrders(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) PurchaseOrderStatus status, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ListPurchaseOrdersQuery query =
                new ListPurchaseOrdersQuery(principal.activeOrganizationId(), status, page, size);
        PageResult<PurchaseOrderResult> result = listPurchaseOrdersHandler.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", result, null));
    }
}
