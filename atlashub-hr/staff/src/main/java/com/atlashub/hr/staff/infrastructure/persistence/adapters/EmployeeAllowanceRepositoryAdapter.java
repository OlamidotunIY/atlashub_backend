package com.atlashub.hr.staff.infrastructure.persistence.adapters;

import com.atlashub.hr.staff.domain.entities.EmployeeAllowance;
import com.atlashub.hr.staff.domain.repositories.EmployeeAllowanceRepository;
import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeAllowanceJpaEntity;
import com.atlashub.hr.staff.infrastructure.persistence.mappers.EmployeeAllowanceMapper;
import com.atlashub.hr.staff.infrastructure.persistence.repositories.SpringDataEmployeeAllowanceRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeAllowanceRepositoryAdapter
        extends JpaBaseRepository<EmployeeAllowance, EmployeeAllowanceJpaEntity>
        implements EmployeeAllowanceRepository {

    private final SpringDataEmployeeAllowanceRepository springDataRepo;

    public EmployeeAllowanceRepositoryAdapter(
            SpringDataEmployeeAllowanceRepository springDataRepo,
            EmployeeAllowanceMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_employee_allowance_seq";
    }

    @Override
    public List<EmployeeAllowance> findByEmployeeId(Long employeeId) {
        return springDataRepo.findByEmployeeId(employeeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
