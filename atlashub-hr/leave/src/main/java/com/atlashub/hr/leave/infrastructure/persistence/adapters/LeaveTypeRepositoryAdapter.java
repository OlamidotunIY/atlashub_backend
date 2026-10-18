package com.atlashub.hr.leave.infrastructure.persistence.adapters;

import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.domain.repositories.LeaveTypeRepository;
import com.atlashub.hr.leave.infrastructure.persistence.entities.LeaveTypeJpaEntity;
import com.atlashub.hr.leave.infrastructure.persistence.mappers.LeaveTypeMapper;
import com.atlashub.hr.leave.infrastructure.persistence.repositories.SpringDataLeaveTypeRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeaveTypeRepositoryAdapter
        extends JpaBaseRepository<LeaveType, LeaveTypeJpaEntity>
        implements LeaveTypeRepository {

    private final SpringDataLeaveTypeRepository springDataRepo;

    public LeaveTypeRepositoryAdapter(
            SpringDataLeaveTypeRepository springDataRepo,
            LeaveTypeMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_leave_type_seq";
    }

    @Override
    public List<LeaveType> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
