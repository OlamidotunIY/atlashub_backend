package com.atlashub.accounting.assets.presentation.dto;

import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterAssetRequest(
        @NotBlank(message = "Asset name is required")
        String name,

        @NotBlank(message = "Asset tag number is required")
        String tagNumber,

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Purchase date is required")
        LocalDate purchaseDate,

        @NotNull(message = "Purchase value is required")
        @Positive(message = "Purchase value must be positive")
        BigDecimal purchaseValue,

        @NotNull(message = "Salvage value is required")
        BigDecimal salvageValue,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Useful life months is required")
        @Positive(message = "Useful life months must be positive")
        Integer usefulLifeMonths,

        DepreciationMethod depreciationMethod
) {}
