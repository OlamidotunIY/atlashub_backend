package com.atlashub.pay.txquery.infrastructure.persistence.mappers;

import com.atlashub.pay.txquery.domain.entities.TransactionAccountEntry;
import com.atlashub.pay.txquery.infrastructure.persistence.entities.TransactionAccountEntryJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = {Money.class, CurrencyCode.class})
public interface TransactionAccountEntryMapper {
    @Mapping(target = "amount", expression = "java(Money.of(record.getAmount(), CurrencyCode.valueOf(record.getCurrency())))")
    TransactionAccountEntry toDomain(TransactionAccountEntryJpa record);

    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    TransactionAccountEntryJpa toPersistence(TransactionAccountEntry domain);
}
