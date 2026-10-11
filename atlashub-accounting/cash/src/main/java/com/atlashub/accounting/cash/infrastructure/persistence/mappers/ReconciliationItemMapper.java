package com.atlashub.accounting.cash.infrastructure.persistence.mappers;

import com.atlashub.accounting.cash.domain.entities.ReconciliationItem;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.ReconciliationItemJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReconciliationItemMapper {

    ReconciliationItem toDomain(ReconciliationItemJpaEntity record);

    ReconciliationItemJpaEntity toPersistence(ReconciliationItem domain);
}
