package com.atlashub.accounting.ar.infrastructure.persistence.mappers;

import com.atlashub.accounting.ar.domain.entities.CustomerReceivable;
import com.atlashub.accounting.ar.infrastructure.persistence.entities.CustomerReceivableJpaEntity;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerReceivableMapper extends DomainMapper<CustomerReceivable, CustomerReceivableJpaEntity> {

    @Override
    @Mapping(target = "amount", expression = "java(com.atlashub.shared.domain.valueobject.Money.of(record.getAmount(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "paidAmount", expression = "java(com.atlashub.shared.domain.valueobject.Money.of(record.getPaidAmount(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())))")
    CustomerReceivable toDomain(CustomerReceivableJpaEntity record);

    @Override
    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "paidAmount", source = "paidAmount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    @Mapping(target = "version", ignore = true)
    CustomerReceivableJpaEntity toPersistence(CustomerReceivable domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "paidAmount", source = "paidAmount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    @Mapping(target = "version", ignore = true)
    void updatePersistence(CustomerReceivable domain, @MappingTarget CustomerReceivableJpaEntity record);
}
