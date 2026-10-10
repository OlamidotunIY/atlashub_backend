package com.atlashub.commerce.storefront.presentation.dto;

import com.atlashub.shared.domain.valueobject.CurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OpenTillRequest(
        @NotNull Long outletId,
        @NotBlank String name,
        @NotNull BigDecimal openingFloat,
        @NotNull CurrencyCode currency
) {
}
