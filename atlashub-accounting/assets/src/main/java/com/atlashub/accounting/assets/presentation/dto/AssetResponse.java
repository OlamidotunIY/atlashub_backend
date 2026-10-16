package com.atlashub.accounting.assets.presentation.dto;

import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetResponse(
        Long id,
        Long organizationId,
        String name,
        String tagNumber,
        String category,
        LocalDate purchaseDate,
        BigDecimal purchaseValue,
        BigDecimal salvageValue,
        Integer usefulLifeMonths,
        DepreciationMethod depreciationMethod,
        BigDecimal accumulatedDepreciation,
        BigDecimal currentBookValue,
        AssetStatus status,
        String currency,
        LocalDate disposalDate,
        BigDecimal saleValue
) {}
