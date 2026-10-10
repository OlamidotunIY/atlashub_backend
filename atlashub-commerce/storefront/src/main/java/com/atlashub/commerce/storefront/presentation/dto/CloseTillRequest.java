package com.atlashub.commerce.storefront.presentation.dto;

import com.atlashub.shared.domain.valueobject.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CloseTillRequest(
        @NotNull BigDecimal actualClosingBalance,
        @NotNull CurrencyCode currency
) {
}
