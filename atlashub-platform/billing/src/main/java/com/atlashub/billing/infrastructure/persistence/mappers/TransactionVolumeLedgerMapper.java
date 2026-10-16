package com.atlashub.billing.infrastructure.persistence.mappers;

import com.atlashub.billing.domain.entities.TransactionVolumeLedger;
import com.atlashub.billing.infrastructure.persistence.entities.TransactionVolumeLedgerJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface TransactionVolumeLedgerMapper extends DomainMapper<TransactionVolumeLedger, TransactionVolumeLedgerJpa> {
}
