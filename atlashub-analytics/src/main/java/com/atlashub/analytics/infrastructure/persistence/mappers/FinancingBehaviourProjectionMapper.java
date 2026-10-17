package com.atlashub.analytics.infrastructure.persistence.mappers;

import com.atlashub.analytics.domain.entities.FinancingBehaviourProjection;
import com.atlashub.analytics.infrastructure.persistence.entities.FinancingBehaviourProjectionJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface FinancingBehaviourProjectionMapper extends DomainMapper<FinancingBehaviourProjection, FinancingBehaviourProjectionJpa> {
}
