package com.atlashub.commerce.inventory.presentation.rest;

import com.atlashub.commerce.inventory.application.commands.ApproveCustomerReturn.ApproveCustomerReturnCommand;
import com.atlashub.commerce.inventory.application.commands.ApproveCustomerReturn.ApproveCustomerReturnHandler;
import com.atlashub.commerce.inventory.application.commands.CreateCustomerReturn.CreateCustomerReturnCommand;
import com.atlashub.commerce.inventory.application.commands.CreateCustomerReturn.CreateCustomerReturnHandler;
import com.atlashub.commerce.inventory.application.commands.CreateCustomerReturn.CreateCustomerReturnResult;
import com.atlashub.commerce.inventory.application.commands.CreateCustomerReturn.ReturnItemDto;
import com.atlashub.commerce.inventory.presentation.dto.CreateCustomerReturnRequest;
import com.atlashub.commerce.inventory.presentation.dto.CreateCustomerReturnResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/returns")
@Tag(name = "Customer Returns", description = "Customer return creation and manual approval")
@SecurityRequirement(name = "bearerAuth")
public class CustomerReturnController {

    private final CreateCustomerReturnHandler createCustomerReturnHandler;
    private final ApproveCustomerReturnHandler approveCustomerReturnHandler;

    public CustomerReturnController(CreateCustomerReturnHandler createCustomerReturnHandler,
                                    ApproveCustomerReturnHandler approveCustomerReturnHandler) {
        this.createCustomerReturnHandler = Objects.requireNonNull(createCustomerReturnHandler, "CreateCustomerReturnHandler must not be null");
        this.approveCustomerReturnHandler = Objects.requireNonNull(approveCustomerReturnHandler, "ApproveCustomerReturnHandler must not be null");
    }

    @PostMapping
    @Operation(summary = "Create a new customer return request")
    public ResponseEntity<ApiResponse<CreateCustomerReturnResponse>> createReturn(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateCustomerReturnRequest request) {
        List<ReturnItemDto> items = request.items().stream()
                .map(i -> new ReturnItemDto(i.productId(), i.quantity()))
                .toList();

        CreateCustomerReturnResult result = createCustomerReturnHandler.execute(new CreateCustomerReturnCommand(
                principal.activeOrganizationId(),
                request.outletId(),
                request.salesOrderId(),
                request.customerId(),
                Money.of(request.refundAmount(), request.refundCurrency()),
                request.reason(),
                request.refundMethod(),
                items
        ));

        CreateCustomerReturnResponse response = new CreateCustomerReturnResponse(result.returnId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Customer return created successfully", response, null));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Manually approve a customer return")
    public ResponseEntity<ApiResponse<Void>> approveReturn(@PathVariable Long id) {
        approveCustomerReturnHandler.execute(new ApproveCustomerReturnCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer return approved successfully", null, null));
    }
}
