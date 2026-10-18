package com.atlashub.hr.attendance.infrastructure.persistence.adapters;

import com.atlashub.hr.attendance.domain.entities.AttendanceRecord;
import com.atlashub.hr.attendance.domain.repositories.AttendanceRecordRepository;
import com.atlashub.hr.attendance.infrastructure.persistence.entities.AttendanceRecordJpaEntity;
import com.atlashub.hr.attendance.infrastructure.persistence.mappers.AttendanceRecordMapper;
import com.atlashub.hr.attendance.infrastructure.persistence.repositories.SpringDataAttendanceRecordRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class AttendanceRecordRepositoryAdapter
        extends JpaBaseRepository<AttendanceRecord, AttendanceRecordJpaEntity>
        implements AttendanceRecordRepository {

    private final SpringDataAttendanceRecordRepository springDataRepo;

    public AttendanceRecordRepositoryAdapter(
            SpringDataAttendanceRecordRepository springDataRepo,
            AttendanceRecordMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_attendance_seq";
    }

    @Override
    public Optional<AttendanceRecord> findByEmployeeIdAndDate(Long employeeId, LocalDate date) {
        return springDataRepo.findByEmployeeIdAndDate(employeeId, date)
                .map(mapper::toDomain);
    }

    @Override
    public List<AttendanceRecord> findByOrganizationIdAndEmployeeIdAndDateBetween(
            Long organizationId,
            Long employeeId,
            LocalDate from,
            LocalDate to
    ) {
        return springDataRepo.findByOrganizationIdAndEmployeeIdAndDateBetween(organizationId, employeeId, from, to)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AttendanceRecord> findByOrganizationIdAndDate(Long organizationId, LocalDate date) {
        return springDataRepo.findByOrganizationIdAndDate(organizationId, date)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
