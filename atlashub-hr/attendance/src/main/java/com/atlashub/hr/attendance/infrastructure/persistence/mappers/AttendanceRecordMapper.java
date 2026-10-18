package com.atlashub.hr.attendance.infrastructure.persistence.mappers;

import com.atlashub.hr.attendance.domain.entities.AttendanceRecord;
import com.atlashub.hr.attendance.infrastructure.persistence.entities.AttendanceRecordJpaEntity;
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
public interface AttendanceRecordMapper extends DomainMapper<AttendanceRecord, AttendanceRecordJpaEntity> {

    @Override
    AttendanceRecord toDomain(AttendanceRecordJpaEntity record);

    @Override
    @Mapping(target = "version", ignore = true)
    AttendanceRecordJpaEntity toPersistence(AttendanceRecord domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(AttendanceRecord domain, @MappingTarget AttendanceRecordJpaEntity record);
}
