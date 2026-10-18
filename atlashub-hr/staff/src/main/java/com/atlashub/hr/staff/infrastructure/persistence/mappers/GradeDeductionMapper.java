package com.atlashub.hr.staff.infrastructure.persistence.mappers;

import com.atlashub.hr.staff.domain.entities.GradeDeduction;
import com.atlashub.hr.staff.infrastructure.persistence.entities.GradeDeductionJpaEntity;
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
public interface GradeDeductionMapper extends DomainMapper<GradeDeduction, GradeDeductionJpaEntity> {

    @Override
    @Mapping(target = "isStatutory", source = "statutory")
    GradeDeduction toDomain(GradeDeductionJpaEntity record);

    @Override
    @Mapping(target = "isStatutory", source = "statutory")
    @Mapping(target = "version", ignore = true)
    GradeDeductionJpaEntity toPersistence(GradeDeduction domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statutory", source = "statutory")
    @Mapping(target = "version", ignore = true)
    void updatePersistence(GradeDeduction domain, @MappingTarget GradeDeductionJpaEntity record);
}
