package com.atlashub.hr.leave.infrastructure.persistence.adapters;

import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.repositories.LeaveApplicationRepository;
import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;
import com.atlashub.hr.leave.infrastructure.persistence.entities.LeaveApplicationJpaEntity;
import com.atlashub.hr.leave.infrastructure.persistence.mappers.LeaveApplicationMapper;
import com.atlashub.hr.leave.infrastructure.persistence.repositories.SpringDataLeaveApplicationRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class LeaveApplicationRepositoryAdapter
        extends JpaBaseRepository<LeaveApplication, LeaveApplicationJpaEntity>
        implements LeaveApplicationRepository {

    private final SpringDataLeaveApplicationRepository springDataRepo;

    public LeaveApplicationRepositoryAdapter(
            SpringDataLeaveApplicationRepository springDataRepo,
            LeaveApplicationMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_leave_application_seq";
    }

    @Override
    public List<LeaveApplication> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<LeaveApplication> findByOrganizationIdAndStatus(Long organizationId, LeaveStatus status) {
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<LeaveApplication> findByEmployeeId(Long employeeId) {
        return springDataRepo.findByEmployeeId(employeeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<LeaveApplication> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status) {
        return springDataRepo.findByEmployeeIdAndStatus(employeeId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<LeaveApplication> findByOrganizationIdAndEmployeeIdAndStatus(
            Long organizationId,
            Long employeeId,
            LeaveStatus status
    ) {
        return springDataRepo.findByOrganizationIdAndEmployeeIdAndStatus(organizationId, employeeId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<LeaveApplication> findByOrganizationIdAndEmployeeId(Long organizationId, Long employeeId) {
        return springDataRepo.findByOrganizationIdAndEmployeeId(organizationId, employeeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEmployeeIdAndStatusInAndDateRange(
            Long employeeId,
            List<LeaveStatus> statuses,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return springDataRepo.existsByEmployeeIdAndStatusInAndDateRange(employeeId, statuses, startDate, endDate);
    }
}
