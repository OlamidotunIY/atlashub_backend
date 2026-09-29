package com.atlashub.pay.ledger.infrastructure.persistence.mappers;

import com.atlashub.pay.ledger.domain.entities.LedgerEntry;
import com.atlashub.pay.ledger.infrastructure.persistence.entities.LedgerEntryJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface LedgerEntryMapper {
    LedgerEntry toDomain(LedgerEntryJpa record);
    LedgerEntryJpa toPersistence(LedgerEntry domain);
}
