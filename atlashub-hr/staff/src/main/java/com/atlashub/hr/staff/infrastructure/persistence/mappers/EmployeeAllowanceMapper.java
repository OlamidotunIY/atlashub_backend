package com.atlashub.hr.staff.infrastructure.persistence.mappers;

import com.atlashub.hr.staff.domain.entities.EmployeeAllowance;
import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeAllowanceJpaEntity;
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
public interface EmployeeAllowanceMapper extends DomainMapper<EmployeeAllowance, EmployeeAllowanceJpaEntity> {

    @Override
    EmployeeAllowance toDomain(EmployeeAllowanceJpaEntity record);

    @Override
    @Mapping(target = "version", ignore = true)
    EmployeeAllowanceJpaEntity toPersistence(EmployeeAllowance domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(EmployeeAllowance domain, @MappingTarget EmployeeAllowanceJpaEntity record);
}
