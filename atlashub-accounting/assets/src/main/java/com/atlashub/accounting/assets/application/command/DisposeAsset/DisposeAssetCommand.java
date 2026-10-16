package com.atlashub.accounting.assets.application.command.DisposeAsset;

import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;

public record DisposeAssetCommand(
        Long assetId,
        Long organizationId,
        Money saleValue,
        LocalDate disposalDate
) {}
