package com.atlashub.hr.staff.infrastructure.persistence.mappers;

import com.atlashub.hr.staff.domain.entities.Infraction;
import com.atlashub.hr.staff.infrastructure.persistence.entities.InfractionJpaEntity;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface InfractionMapper extends DomainMapper<Infraction, InfractionJpaEntity> {

    @Override
    Infraction toDomain(InfractionJpaEntity record);

    @Override
    @Mapping(target = "version", ignore = true)
    InfractionJpaEntity toPersistence(Infraction domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(Infraction domain, @MappingTarget InfractionJpaEntity record);
}
