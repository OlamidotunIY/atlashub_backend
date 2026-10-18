package com.atlashub.hr.staff.infrastructure.persistence.mappers;

import com.atlashub.hr.staff.domain.entities.EmployeeBank;
import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeBankJpaEntity;
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
public interface EmployeeBankMapper extends DomainMapper<EmployeeBank, EmployeeBankJpaEntity> {

    @Override
    @Mapping(target = "isVerified", source = "verified")
    @Mapping(target = "isPrimary", source = "primary")
    EmployeeBank toDomain(EmployeeBankJpaEntity record);

    @Override
    @Mapping(target = "isVerified", source = "verified")
    @Mapping(target = "isPrimary", source = "primary")
    @Mapping(target = "version", ignore = true)
    EmployeeBankJpaEntity toPersistence(EmployeeBank domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "verified", source = "verified")
    @Mapping(target = "primary", source = "primary")
    @Mapping(target = "version", ignore = true)
    void updatePersistence(EmployeeBank domain, @MappingTarget EmployeeBankJpaEntity record);
}
