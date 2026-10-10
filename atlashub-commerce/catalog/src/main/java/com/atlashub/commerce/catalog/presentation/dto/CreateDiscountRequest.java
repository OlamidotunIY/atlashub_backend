package com.atlashub.commerce.catalog.presentation.dto;

import com.atlashub.commerce.catalog.domain.valueobject.DiscountScope;
import com.atlashub.commerce.catalog.domain.valueobject.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDiscountRequest(
        @NotBlank(message = "Discount name is required")
        String name,
        @NotNull(message = "Discount type is required")
        DiscountType type,
        @NotNull(message = "Discount value is required")
        BigDecimal value,
        @NotNull(message = "Discount scope is required")
        DiscountScope scope,
        BigDecimal minOrderAmount,
        Integer maxUses,
        LocalDate validFrom,
        LocalDate validTo
) {
}
