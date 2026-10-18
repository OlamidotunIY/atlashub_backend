package com.atlashub.hr.leave.infrastructure.persistence.mappers;

import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.infrastructure.persistence.entities.LeaveTypeJpaEntity;
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
public interface LeaveTypeMapper extends DomainMapper<LeaveType, LeaveTypeJpaEntity> {

    @Override
    LeaveType toDomain(LeaveTypeJpaEntity record);

    @Override
    @Mapping(target = "version", ignore = true)
    LeaveTypeJpaEntity toPersistence(LeaveType domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(LeaveType domain, @MappingTarget LeaveTypeJpaEntity record);
}
