package com.atlashub.hr.leave.infrastructure.persistence.mappers;

import com.atlashub.hr.leave.domain.entities.LeaveBalance;
import com.atlashub.hr.leave.infrastructure.persistence.entities.LeaveBalanceJpaEntity;
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
public interface LeaveBalanceMapper extends DomainMapper<LeaveBalance, LeaveBalanceJpaEntity> {

    @Override
    LeaveBalance toDomain(LeaveBalanceJpaEntity record);

    @Override
    @Mapping(target = "version", ignore = true)
    LeaveBalanceJpaEntity toPersistence(LeaveBalance domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(LeaveBalance domain, @MappingTarget LeaveBalanceJpaEntity record);
}
