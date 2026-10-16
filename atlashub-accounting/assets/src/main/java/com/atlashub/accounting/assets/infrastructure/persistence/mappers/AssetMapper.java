package com.atlashub.accounting.assets.infrastructure.persistence.mappers;

import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.infrastructure.persistence.entities.AssetJpaEntity;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AssetMapper extends DomainMapper<Asset, AssetJpaEntity> {

    @Override
    @Mapping(target = "purchaseValue", expression = "java(com.atlashub.shared.domain.valueobject.Money.of(record.getPurchaseValue(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "salvageValue", expression = "java(com.atlashub.shared.domain.valueobject.Money.of(record.getSalvageValue(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "accumulatedDepreciation", expression = "java(com.atlashub.shared.domain.valueobject.Money.of(record.getAccumulatedDepreciation(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "currentBookValue", expression = "java(com.atlashub.shared.domain.valueobject.Money.of(record.getCurrentBookValue(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "saleValue", expression = "java(record.getSaleValue() != null ? com.atlashub.shared.domain.valueobject.Money.of(record.getSaleValue(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())) : null)")
    Asset toDomain(AssetJpaEntity record);

    @Override
    @Mapping(target = "purchaseValue", source = "purchaseValue.amount")
    @Mapping(target = "salvageValue", source = "salvageValue.amount")
    @Mapping(target = "accumulatedDepreciation", source = "accumulatedDepreciation.amount")
    @Mapping(target = "currentBookValue", source = "currentBookValue.amount")
    @Mapping(target = "currency", source = "purchaseValue.currency")
    @Mapping(target = "saleValue", expression = "java(domain.getSaleValue() != null ? domain.getSaleValue().amount() : null)")
    @Mapping(target = "version", ignore = true)
    AssetJpaEntity toPersistence(Asset domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchaseValue", source = "purchaseValue.amount")
    @Mapping(target = "salvageValue", source = "salvageValue.amount")
    @Mapping(target = "accumulatedDepreciation", source = "accumulatedDepreciation.amount")
    @Mapping(target = "currentBookValue", source = "currentBookValue.amount")
    @Mapping(target = "currency", source = "purchaseValue.currency")
    @Mapping(target = "saleValue", expression = "java(domain.getSaleValue() != null ? domain.getSaleValue().amount() : null)")
    @Mapping(target = "version", ignore = true)
    void updatePersistence(Asset domain, @MappingTarget AssetJpaEntity record);
}
