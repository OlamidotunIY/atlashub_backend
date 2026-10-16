package com.atlashub.accounting.ap.infrastructure.persistence.mappers;

import com.atlashub.accounting.ap.domain.entities.SupplierInvoice;
import com.atlashub.accounting.ap.infrastructure.persistence.entities.SupplierInvoiceJpaEntity;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SupplierInvoiceMapper extends DomainMapper<SupplierInvoice, SupplierInvoiceJpaEntity> {

    @Override
    @Mapping(target = "amount", expression = "java(com.atlashub.shared.domain.valueobject.Money.of(record.getAmount(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())))")
    SupplierInvoice toDomain(SupplierInvoiceJpaEntity record);

    @Override
    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    @Mapping(target = "version", ignore = true)
    SupplierInvoiceJpaEntity toPersistence(SupplierInvoice domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    @Mapping(target = "version", ignore = true)
    void updatePersistence(SupplierInvoice domain, @MappingTarget SupplierInvoiceJpaEntity record);
}
