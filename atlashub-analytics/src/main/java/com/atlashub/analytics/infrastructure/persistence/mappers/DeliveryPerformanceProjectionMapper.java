package com.atlashub.analytics.infrastructure.persistence.mappers;

import com.atlashub.analytics.domain.entities.DeliveryPerformanceProjection;
import com.atlashub.analytics.infrastructure.persistence.entities.DeliveryPerformanceProjectionJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface DeliveryPerformanceProjectionMapper extends DomainMapper<DeliveryPerformanceProjection, DeliveryPerformanceProjectionJpa> {
}
