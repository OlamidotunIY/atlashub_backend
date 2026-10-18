package com.atlashub.hr.staff.infrastructure.persistence.mappers;

import com.atlashub.hr.staff.domain.entities.EmployeeDeduction;
import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeDeductionJpaEntity;
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
public interface EmployeeDeductionMapper extends DomainMapper<EmployeeDeduction, EmployeeDeductionJpaEntity> {

    @Override
    EmployeeDeduction toDomain(EmployeeDeductionJpaEntity record);

    @Override
    @Mapping(target = "version", ignore = true)
    EmployeeDeductionJpaEntity toPersistence(EmployeeDeduction domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(EmployeeDeduction domain, @MappingTarget EmployeeDeductionJpaEntity record);
}
