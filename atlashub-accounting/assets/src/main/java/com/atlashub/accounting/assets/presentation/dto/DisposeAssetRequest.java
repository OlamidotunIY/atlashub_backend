package com.atlashub.accounting.assets.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DisposeAssetRequest(
        @NotNull(message = "Sale value is required")
        @PositiveOrZero(message = "Sale value cannot be negative")
        BigDecimal saleValue,

        @NotNull(message = "Disposal date is required")
        LocalDate disposalDate
) {}
