package com.atlashub.accounting.assets.domain.entities;

import com.atlashub.accounting.assets.domain.events.AssetDepreciatedEvent;
import com.atlashub.accounting.assets.domain.events.AssetDisposedEvent;
import com.atlashub.accounting.assets.domain.exceptions.AssetAlreadyDisposedException;
import com.atlashub.accounting.assets.domain.exceptions.AssetAlreadyFullyDepreciatedException;
import com.atlashub.accounting.assets.domain.exceptions.InvalidAssetOperationException;
import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class Asset extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String name;
    private final String tagNumber;
    private final String category;
    private final LocalDate purchaseDate;
    private final Money purchaseValue;
    private final Money salvageValue;
    private final Integer usefulLifeMonths;
    private final DepreciationMethod depreciationMethod;
    private Money accumulatedDepreciation;
    private Money currentBookValue;
    private AssetStatus status;
    private LocalDate disposalDate;
    private Money saleValue;

    public Asset(
            Long id,
            Long organizationId,
            String name,
            String tagNumber,
            String category,
            LocalDate purchaseDate,
            Money purchaseValue,
            Money salvageValue,
            Integer usefulLifeMonths,
            DepreciationMethod depreciationMethod,
            Money accumulatedDepreciation,
            Money currentBookValue,
            AssetStatus status,
            LocalDate disposalDate,
            Money saleValue
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.tagNumber = tagNumber;
        this.category = category;
        this.purchaseDate = purchaseDate;
        this.purchaseValue = purchaseValue;
        this.salvageValue = salvageValue;
        this.usefulLifeMonths = usefulLifeMonths;
        this.depreciationMethod = depreciationMethod;
        this.accumulatedDepreciation = accumulatedDepreciation;
        this.currentBookValue = currentBookValue;
        this.status = status;
        this.disposalDate = disposalDate;
        this.saleValue = saleValue;
    }

    public static Asset create(
            Long id,
            Long organizationId,
            String name,
            String tagNumber,
            String category,
            LocalDate purchaseDate,
            Money purchaseValue,
            Money salvageValue,
            Integer usefulLifeMonths,
            DepreciationMethod depreciationMethod
    ) {
        if (organizationId == null) {
            throw new InvalidAssetOperationException("Organization ID is required");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidAssetOperationException("Asset name is required");
        }
        if (tagNumber == null || tagNumber.isBlank()) {
            throw new InvalidAssetOperationException("Asset tag number is required");
        }
        if (category == null || category.isBlank()) {
            throw new InvalidAssetOperationException("Asset category is required");
        }
        if (purchaseDate == null) {
            throw new InvalidAssetOperationException("Purchase date is required");
        }
        if (purchaseValue == null || !purchaseValue.isPositive()) {
            throw new InvalidAssetOperationException("Purchase value must be positive");
        }
        if (salvageValue == null || salvageValue.isNegative()) {
            throw new InvalidAssetOperationException("Salvage value cannot be negative");
        }
        if (salvageValue.amount().compareTo(purchaseValue.amount()) > 0) {
            throw new InvalidAssetOperationException("Salvage value cannot exceed purchase value");
        }
        if (usefulLifeMonths == null || usefulLifeMonths <= 0) {
            throw new InvalidAssetOperationException("Useful life months must be greater than zero");
        }
        if (depreciationMethod == null) {
            depreciationMethod = DepreciationMethod.STRAIGHT_LINE;
        }

        Money zeroDepreciation = Money.zero(purchaseValue.currency());
        Money initialBookValue = purchaseValue;

        return new Asset(
                id,
                organizationId,
                name.trim(),
                tagNumber.trim(),
                category.trim(),
                purchaseDate,
                purchaseValue,
                salvageValue,
                usefulLifeMonths,
                depreciationMethod,
                zeroDepreciation,
                initialBookValue,
                AssetStatus.ACTIVE,
                null,
                null
        );
    }

    public void depreciate(Money amount) {
        if (status == AssetStatus.DISPOSED) {
            throw new AssetAlreadyDisposedException(id);
        }
        if (status == AssetStatus.FULLY_DEPRECIATED) {
            throw new AssetAlreadyFullyDepreciatedException();
        }
        if (amount == null || !amount.isPositive()) {
            throw new InvalidAssetOperationException("Depreciation amount must be positive");
        }

        Money maxDepreciable = purchaseValue.subtract(salvageValue);
        Money newAccumulated = accumulatedDepreciation.add(amount);

        if (newAccumulated.amount().compareTo(maxDepreciable.amount()) >= 0) {
            this.accumulatedDepreciation = maxDepreciable;
            this.currentBookValue = salvageValue;
            this.status = AssetStatus.FULLY_DEPRECIATED;
        } else {
            this.accumulatedDepreciation = newAccumulated;
            this.currentBookValue = purchaseValue.subtract(newAccumulated);
        }

        registerEvent(AssetDepreciatedEvent.of(
                id,
                organizationId,
                amount.amount(),
                currentBookValue.amount(),
                accumulatedDepreciation.amount(),
                purchaseValue.currency().name(),
                LocalDate.now()
        ));
    }

    public void dispose(Money saleValue, LocalDate disposalDate) {
        if (status == AssetStatus.DISPOSED) {
            throw new AssetAlreadyDisposedException(id);
        }
        if (saleValue == null || saleValue.isNegative()) {
            throw new InvalidAssetOperationException("Sale value cannot be negative");
        }
        if (disposalDate == null) {
            throw new InvalidAssetOperationException("Disposal date is required");
        }

        this.saleValue = saleValue;
        this.disposalDate = disposalDate;
        this.status = AssetStatus.DISPOSED;

        BigDecimal gainOrLoss = saleValue.amount().subtract(currentBookValue.amount());

        registerEvent(AssetDisposedEvent.of(
                id,
                organizationId,
                saleValue.amount(),
                currentBookValue.amount(),
                gainOrLoss,
                purchaseValue.currency().name(),
                disposalDate
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
