package com.atlashub.pay.ledger.infrastructure.persistence.mappers;

import com.atlashub.pay.ledger.domain.entities.LedgerEntry;
import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.pay.ledger.infrastructure.persistence.entities.LedgerTransactionJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface LedgerTransactionMapper {
    
    @Mapping(target = "entries", source = "entries")
    LedgerTransaction toDomain(LedgerTransactionJpa record, List<LedgerEntry> entries);
    
    LedgerTransactionJpa toPersistence(LedgerTransaction domain);
}
