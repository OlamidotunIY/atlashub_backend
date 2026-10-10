package com.atlashub.pay.txquery.infrastructure.persistence.mappers;

import com.atlashub.pay.txquery.domain.entities.TransactionAccountEntry;
import com.atlashub.pay.txquery.domain.entities.TransactionRecord;
import com.atlashub.pay.txquery.infrastructure.persistence.entities.TransactionRecordJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = {Money.class, CurrencyCode.class})
public interface TransactionRecordMapper {
    @Mapping(target = "amount", expression = "java(Money.of(record.getAmount(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "fee", expression = "java(record.getFee() == null ? null : Money.of(record.getFee(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "netAmount", expression = "java(record.getNetAmount() == null ? null : Money.of(record.getNetAmount(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "accountEntries", source = "entries")
    TransactionRecord toDomain(TransactionRecordJpa record, List<TransactionAccountEntry> entries);

    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "fee", source = "fee.amount")
    @Mapping(target = "netAmount", source = "netAmount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    @Mapping(target = "version", ignore = true)
    TransactionRecordJpa toPersistence(TransactionRecord domain);
}
