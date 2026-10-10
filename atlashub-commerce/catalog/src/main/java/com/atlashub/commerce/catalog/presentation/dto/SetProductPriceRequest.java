package com.atlashub.commerce.catalog.presentation.dto;

import com.atlashub.commerce.catalog.domain.valueobject.PriceLevel;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SetProductPriceRequest(
        Long variantId,
        @NotNull(message = "Price level is required")
        PriceLevel priceLevel,
        BigDecimal costPrice,
        @NotNull(message = "Selling price is required")
        BigDecimal sellingPrice
) {
}
