package com.atlashub.accounting.cash.infrastructure.persistence.mappers;

import com.atlashub.accounting.cash.domain.entities.BankStatementLine;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.BankStatementLineJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BankStatementLineMapper {

    @Mapping(target = "amount", expression = "java(com.atlashub.shared.domain.valueobject.Money.of(record.getAmount(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())))")
    BankStatementLine toDomain(BankStatementLineJpaEntity record);

    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    BankStatementLineJpaEntity toPersistence(BankStatementLine domain);
}
