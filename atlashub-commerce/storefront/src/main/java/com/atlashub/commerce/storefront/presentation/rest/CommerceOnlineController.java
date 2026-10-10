package com.atlashub.commerce.storefront.presentation.rest;

import com.atlashub.commerce.storefront.application.commands.AddCustomerDepositPayment.AddCustomerDepositPaymentCommand;
import com.atlashub.commerce.storefront.application.commands.AddCustomerDepositPayment.AddCustomerDepositPaymentHandler;
import com.atlashub.commerce.storefront.application.commands.CreateCustomerDeposit.CreateCustomerDepositCommand;
import com.atlashub.commerce.storefront.application.commands.CreateCustomerDeposit.CreateCustomerDepositHandler;
import com.atlashub.commerce.storefront.application.commands.CreateCustomerDeposit.CreateCustomerDepositResult;
import com.atlashub.commerce.storefront.application.commands.CreateOnlineOrder.CreateOnlineOrderCommand;
import com.atlashub.commerce.storefront.application.commands.CreateOnlineOrder.CreateOnlineOrderHandler;
import com.atlashub.commerce.storefront.application.commands.CreateOnlineOrder.CreateOnlineOrderResult;
import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.OrderItemDto;
import com.atlashub.commerce.storefront.application.queries.GetCustomerCredit.CustomerCreditResult;
import com.atlashub.commerce.storefront.application.queries.GetCustomerCredit.GetCustomerCreditHandler;
import com.atlashub.commerce.storefront.application.queries.GetCustomerCredit.GetCustomerCreditQuery;
import com.atlashub.commerce.storefront.application.queries.GetSalesOrder.GetSalesOrderHandler;
import com.atlashub.commerce.storefront.application.queries.GetSalesOrder.GetSalesOrderQuery;
import com.atlashub.commerce.storefront.application.queries.GetSalesOrder.SalesOrderResult;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.presentation.dto.AddCustomerDepositPaymentRequest;
import com.atlashub.commerce.storefront.presentation.dto.CreateCustomerDepositRequest;
import com.atlashub.commerce.storefront.presentation.dto.CreateCustomerDepositResponse;
import com.atlashub.commerce.storefront.presentation.dto.CreateOnlineOrderRequest;
import com.atlashub.commerce.storefront.presentation.dto.CreateOnlineOrderResponse;
import com.atlashub.commerce.storefront.presentation.dto.OrderItemRequest;
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

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Commerce Online", description = "Online orders, layaway customer deposits, and credit accounts")
@SecurityRequirement(name = "bearerAuth")
public class CommerceOnlineController {

    private final CreateOnlineOrderHandler createOnlineOrderHandler;
    private final CreateCustomerDepositHandler createCustomerDepositHandler;
    private final AddCustomerDepositPaymentHandler addCustomerDepositPaymentHandler;
    private final GetCustomerCreditHandler getCustomerCreditHandler;
    private final GetSalesOrderHandler getSalesOrderHandler;

    public CommerceOnlineController(CreateOnlineOrderHandler createOnlineOrderHandler,
                                    CreateCustomerDepositHandler createCustomerDepositHandler,
                                    AddCustomerDepositPaymentHandler addCustomerDepositPaymentHandler,
                                    GetCustomerCreditHandler getCustomerCreditHandler,
                                    GetSalesOrderHandler getSalesOrderHandler) {
        this.createOnlineOrderHandler = Objects.requireNonNull(createOnlineOrderHandler, "CreateOnlineOrderHandler must not be null");
        this.createCustomerDepositHandler = Objects.requireNonNull(createCustomerDepositHandler, "CreateCustomerDepositHandler must not be null");
        this.addCustomerDepositPaymentHandler = Objects.requireNonNull(addCustomerDepositPaymentHandler, "AddCustomerDepositPaymentHandler must not be null");
        this.getCustomerCreditHandler = Objects.requireNonNull(getCustomerCreditHandler, "GetCustomerCreditHandler must not be null");
        this.getSalesOrderHandler = Objects.requireNonNull(getSalesOrderHandler, "GetSalesOrderHandler must not be null");
    }

    @PostMapping("/orders/online")
    @Operation(summary = "Create online order")
    public ResponseEntity<ApiResponse<CreateOnlineOrderResponse>> createOnlineOrder(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateOnlineOrderRequest request) {
        List<OrderItemDto> items = request.items().stream()
                .map(this::toOrderItemDto)
                .toList();

        CreateOnlineOrderCommand command = new CreateOnlineOrderCommand(
                principal.activeOrganizationId(),
                request.outletId(),
                request.customerId(),
                items,
                request.deliveryAddress(),
                request.paymentMethod()
        );

        CreateOnlineOrderResult result = createOnlineOrderHandler.execute(command);
        CreateOnlineOrderResponse response = new CreateOnlineOrderResponse(result.salesOrderId(), OrderStatus.PENDING);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Online order created successfully", response, null));
    }

    @PostMapping("/deposits")
    @Operation(summary = "Create customer layaway deposit")
    public ResponseEntity<ApiResponse<CreateCustomerDepositResponse>> createDeposit(
            @Valid @RequestBody CreateCustomerDepositRequest request) {
        CreateCustomerDepositCommand command = new CreateCustomerDepositCommand(
                request.salesOrderId(),
                Money.of(request.initialDeposit(), request.currency()),
                Money.of(request.totalAmount(), request.currency())
        );

        CreateCustomerDepositResult result = createCustomerDepositHandler.execute(command);
        CreateCustomerDepositResponse response = new CreateCustomerDepositResponse(result.depositId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Customer deposit created successfully", response, null));
    }

    @PostMapping("/deposits/{id}/payment")
    @Operation(summary = "Add payment to customer deposit")
    public ResponseEntity<ApiResponse<Void>> addDepositPayment(
            @PathVariable Long id,
            @Valid @RequestBody AddCustomerDepositPaymentRequest request) {
        AddCustomerDepositPaymentCommand command = new AddCustomerDepositPaymentCommand(
                id,
                Money.of(request.paymentAmount(), request.currency())
        );

        addCustomerDepositPaymentHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Deposit payment added successfully", null, null));
    }

    @GetMapping("/credits")
    @Operation(summary = "Get customer credit details")
    public ResponseEntity<ApiResponse<CustomerCreditResult>> getCustomerCredit(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam Long customerId) {
        CustomerCreditResult result = getCustomerCreditHandler.execute(
                new GetCustomerCreditQuery(principal.activeOrganizationId(), customerId)
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer credit retrieved successfully", result, null));
    }

    @GetMapping("/orders")
    @Operation(summary = "Get sales order by id")
    public ResponseEntity<ApiResponse<SalesOrderResult>> getSalesOrder(@RequestParam Long id) {
        SalesOrderResult result = getSalesOrderHandler.execute(new GetSalesOrderQuery(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Sales order retrieved successfully", result, null));
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
