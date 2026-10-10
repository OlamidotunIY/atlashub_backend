package com.atlashub.commerce.catalog.presentation.rest;

import com.atlashub.commerce.catalog.application.commands.CreateDiscount.CreateDiscountCommand;
import com.atlashub.commerce.catalog.application.commands.CreateDiscount.CreateDiscountHandler;
import com.atlashub.commerce.catalog.application.commands.CreateDiscount.CreateDiscountResult;
import com.atlashub.commerce.catalog.presentation.dto.CreateDiscountRequest;
import com.atlashub.commerce.catalog.presentation.dto.CreateDiscountResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/discounts")
@Tag(name = "Discounts", description = "Discount and promotion management")
@SecurityRequirement(name = "bearerAuth")
public class DiscountController {

    private final CreateDiscountHandler createDiscountHandler;

    public DiscountController(CreateDiscountHandler createDiscountHandler) {
        this.createDiscountHandler =
                Objects.requireNonNull(createDiscountHandler, "CreateDiscountHandler must not be null");
    }

    @PostMapping
    @Operation(summary = "Create discount")
    public ResponseEntity<ApiResponse<CreateDiscountResponse>> createDiscount(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateDiscountRequest request) {
        CreateDiscountCommand command =
                new CreateDiscountCommand(principal.activeOrganizationId(), request.name(), request.type(),
                        request.value(), request.scope(),
                        request.minOrderAmount() != null ? Money.of(request.minOrderAmount(), CurrencyCode.NGN) : null,
                        request.maxUses(), request.validFrom(), request.validTo());
        CreateDiscountResult result = createDiscountHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Discount created successfully",
                new CreateDiscountResponse(result.discountId()), null));
    }
}
