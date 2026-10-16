package com.atlashub.hr.leave.infrastructure.persistence.adapters;

import com.atlashub.hr.leave.domain.entities.LeaveBalance;
import com.atlashub.hr.leave.domain.repositories.LeaveBalanceRepository;
import com.atlashub.hr.leave.infrastructure.persistence.entities.LeaveBalanceJpaEntity;
import com.atlashub.hr.leave.infrastructure.persistence.mappers.LeaveBalanceMapper;
import com.atlashub.hr.leave.infrastructure.persistence.repositories.SpringDataLeaveBalanceRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LeaveBalanceRepositoryAdapter
        extends JpaBaseRepository<LeaveBalance, LeaveBalanceJpaEntity>
        implements LeaveBalanceRepository {

    private final SpringDataLeaveBalanceRepository springDataRepo;

    public LeaveBalanceRepositoryAdapter(
            SpringDataLeaveBalanceRepository springDataRepo,
            LeaveBalanceMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_leave_balance_seq";
    }

    @Override
    public Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(Long employeeId, Long leaveTypeId, int year) {
        return springDataRepo.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYearWithLock(Long employeeId, Long leaveTypeId, int year) {
        return springDataRepo.findByEmployeeIdAndLeaveTypeIdAndYearWithLock(employeeId, leaveTypeId, year)
                .map(mapper::toDomain);
    }

    @Override
    public List<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, int year) {
        return springDataRepo.findByEmployeeIdAndYear(employeeId, year)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<LeaveBalance> findByEmployeeId(Long employeeId) {
        return springDataRepo.findByEmployeeId(employeeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
