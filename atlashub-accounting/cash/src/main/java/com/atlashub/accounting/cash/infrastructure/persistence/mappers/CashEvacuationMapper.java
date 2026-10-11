package com.atlashub.accounting.cash.infrastructure.persistence.mappers;

import com.atlashub.accounting.cash.domain.entities.CashEvacuation;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.CashEvacuationJpaEntity;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CashEvacuationMapper extends DomainMapper<CashEvacuation, CashEvacuationJpaEntity> {

    @Override
    @Mapping(target = "amount", expression = "java(com.atlashub.shared.domain.valueobject.Money.of(record.getAmount(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())))")
    CashEvacuation toDomain(CashEvacuationJpaEntity record);

    @Override
    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    CashEvacuationJpaEntity toPersistence(CashEvacuation domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    void updatePersistence(CashEvacuation domain, @MappingTarget CashEvacuationJpaEntity record);
}
