package com.atlashub.hr.staff.infrastructure.persistence.mappers;

import com.atlashub.hr.staff.domain.entities.GradeAllowance;
import com.atlashub.hr.staff.infrastructure.persistence.entities.GradeAllowanceJpaEntity;
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
public interface GradeAllowanceMapper extends DomainMapper<GradeAllowance, GradeAllowanceJpaEntity> {

    @Override
    GradeAllowance toDomain(GradeAllowanceJpaEntity record);

    @Override
    @Mapping(target = "version", ignore = true)
    GradeAllowanceJpaEntity toPersistence(GradeAllowance domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(GradeAllowance domain, @MappingTarget GradeAllowanceJpaEntity record);
}
