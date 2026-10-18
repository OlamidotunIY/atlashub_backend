package com.atlashub.hr.attendance.domain.repositories;

import com.atlashub.hr.attendance.domain.entities.AttendanceRecord;
import com.atlashub.shared.domain.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends Repository<AttendanceRecord> {

    Optional<AttendanceRecord> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<AttendanceRecord> findByOrganizationIdAndEmployeeIdAndDateBetween(
            Long organizationId,
            Long employeeId,
            LocalDate from,
            LocalDate to
    );

    List<AttendanceRecord> findByOrganizationIdAndDate(Long organizationId, LocalDate date);
}
