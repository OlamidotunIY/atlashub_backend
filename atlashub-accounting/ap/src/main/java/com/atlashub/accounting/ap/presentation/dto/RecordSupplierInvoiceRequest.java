package com.atlashub.accounting.ap.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordSupplierInvoiceRequest(
        @NotNull(message = "Supplier ID is required")
        Long supplierId,

        @NotNull(message = "Purchase order ID is required")
        Long purchaseOrderId,

        @NotBlank(message = "Invoice number is required")
        String invoiceNumber,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        String currency,

        @NotNull(message = "Due date is required")
        LocalDate dueDate
) {}
