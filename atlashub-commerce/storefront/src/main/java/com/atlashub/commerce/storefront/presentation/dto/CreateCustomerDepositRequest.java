package com.atlashub.commerce.storefront.presentation.dto;

import com.atlashub.shared.domain.valueobject.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateCustomerDepositRequest(
        @NotNull Long salesOrderId,
        @NotNull BigDecimal initialDeposit,
        @NotNull BigDecimal totalAmount,
        @NotNull CurrencyCode currency
) {
}
