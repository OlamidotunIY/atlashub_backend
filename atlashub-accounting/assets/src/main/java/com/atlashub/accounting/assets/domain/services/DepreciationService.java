package com.atlashub.accounting.assets.domain.services;

import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DepreciationService {

    public Money calculateMonthlyDepreciation(Asset asset) {
        if (asset == null || asset.getStatus() != AssetStatus.ACTIVE) {
            return Money.zero(asset != null && asset.getPurchaseValue() != null ? asset.getPurchaseValue().currency() : null);
        }

        Money depreciable = asset.getPurchaseValue().subtract(asset.getSalvageValue());
        if (!depreciable.isPositive()) {
            return Money.zero(asset.getPurchaseValue().currency());
        }

        BigDecimal monthlyAmount = depreciable.amount()
                .divide(BigDecimal.valueOf(asset.getUsefulLifeMonths()), 4, RoundingMode.HALF_UP);

        Money remainingToDepreciate = asset.getCurrentBookValue().subtract(asset.getSalvageValue());
        if (monthlyAmount.compareTo(remainingToDepreciate.amount()) > 0) {
            return remainingToDepreciate;
        }

        return Money.of(monthlyAmount, asset.getPurchaseValue().currency());
    }

    public boolean isFullyDepreciated(Asset asset) {
        if (asset == null) {
            return false;
        }
        if (asset.getStatus() == AssetStatus.FULLY_DEPRECIATED) {
            return true;
        }
        return asset.getCurrentBookValue().amount().compareTo(asset.getSalvageValue().amount()) <= 0;
    }
}
