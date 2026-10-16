package com.atlashub.hr.leave.infrastructure.persistence.mappers;

import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.infrastructure.persistence.entities.LeaveApplicationJpaEntity;
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
public interface LeaveApplicationMapper extends DomainMapper<LeaveApplication, LeaveApplicationJpaEntity> {

    @Override
    LeaveApplication toDomain(LeaveApplicationJpaEntity record);

    @Override
    @Mapping(target = "version", ignore = true)
    LeaveApplicationJpaEntity toPersistence(LeaveApplication domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(LeaveApplication domain, @MappingTarget LeaveApplicationJpaEntity record);
}
