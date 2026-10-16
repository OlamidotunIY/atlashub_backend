package com.atlashub.accounting.assets.application.command.RegisterAsset;

import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;

public record RegisterAssetCommand(
        Long organizationId,
        String name,
        String tagNumber,
        String category,
        LocalDate purchaseDate,
        Money purchaseValue,
        Money salvageValue,
        int usefulLifeMonths,
        DepreciationMethod depreciationMethod
) {}
