package com.atlashub.pay.ledger.infrastructure.persistence.mappers;

import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.pay.ledger.infrastructure.persistence.entities.BalanceSnapshotJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface BalanceSnapshotMapper {
    BalanceSnapshot toDomain(BalanceSnapshotJpa record);
    BalanceSnapshotJpa toPersistence(BalanceSnapshot domain);
}
