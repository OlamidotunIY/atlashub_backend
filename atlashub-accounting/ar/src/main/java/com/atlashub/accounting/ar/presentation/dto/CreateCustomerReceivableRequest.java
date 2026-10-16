package com.atlashub.accounting.ar.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCustomerReceivableRequest(
        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotNull(message = "Sales order ID is required")
        Long salesOrderId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        String currency,

        @NotNull(message = "Due date is required")
        LocalDate dueDate
) {}
