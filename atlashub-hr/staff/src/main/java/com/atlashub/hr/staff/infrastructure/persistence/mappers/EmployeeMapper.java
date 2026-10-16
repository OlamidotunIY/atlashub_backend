package com.atlashub.hr.staff.infrastructure.persistence.mappers;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeJpaEntity;
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
public interface EmployeeMapper extends DomainMapper<Employee, EmployeeJpaEntity> {

    @Override
    @Mapping(target = "baseSalary", expression = "java(com.atlashub.shared.domain.valueobject.Money.of(record.getBaseSalary(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())))")
    Employee toDomain(EmployeeJpaEntity record);

    @Override
    @Mapping(target = "baseSalary", source = "baseSalary.amount")
    @Mapping(target = "currency", source = "baseSalary.currency")
    @Mapping(target = "version", ignore = true)
    EmployeeJpaEntity toPersistence(Employee domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "baseSalary", source = "baseSalary.amount")
    @Mapping(target = "currency", source = "baseSalary.currency")
    @Mapping(target = "version", ignore = true)
    void updatePersistence(Employee domain, @MappingTarget EmployeeJpaEntity record);
}
