package com.atlashub.commerce.inventory.presentation.dto;

import com.atlashub.commerce.inventory.domain.valueobject.RefundMethod;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateCustomerReturnRequest(
        @NotNull(message = "Sales order ID is required")
        Long salesOrderId,

        @NotNull(message = "Outlet ID is required")
        Long outletId,

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotEmpty(message = "Return items list must not be empty")
        List<@Valid ReturnItemRequest> items,

        @NotNull(message = "Refund amount is required")
        BigDecimal refundAmount,

        @NotNull(message = "Refund currency is required")
        CurrencyCode refundCurrency,

        @NotBlank(message = "Return reason is required")
        String reason,

        @NotNull(message = "Refund method is required")
        RefundMethod refundMethod
) {
}
