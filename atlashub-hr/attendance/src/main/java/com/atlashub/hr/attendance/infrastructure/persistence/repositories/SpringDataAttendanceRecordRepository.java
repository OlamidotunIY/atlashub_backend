package com.atlashub.hr.attendance.infrastructure.persistence.repositories;

import com.atlashub.hr.attendance.infrastructure.persistence.entities.AttendanceRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataAttendanceRecordRepository extends JpaRepository<AttendanceRecordJpaEntity, Long> {

    Optional<AttendanceRecordJpaEntity> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<AttendanceRecordJpaEntity> findByOrganizationIdAndEmployeeIdAndDateBetween(
            Long organizationId,
            Long employeeId,
            LocalDate from,
            LocalDate to
    );

    List<AttendanceRecordJpaEntity> findByOrganizationIdAndDate(Long organizationId, LocalDate date);
}
