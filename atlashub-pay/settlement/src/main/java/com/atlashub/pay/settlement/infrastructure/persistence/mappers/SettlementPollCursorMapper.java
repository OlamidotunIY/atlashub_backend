package com.atlashub.pay.settlement.infrastructure.persistence.mappers;

import com.atlashub.pay.settlement.domain.entities.SettlementPollCursor;
import com.atlashub.pay.settlement.infrastructure.persistence.entities.SettlementPollCursorJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SettlementPollCursorMapper extends DomainMapper<SettlementPollCursor, SettlementPollCursorJpa> {
}
