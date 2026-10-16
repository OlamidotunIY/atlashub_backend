package com.atlashub.hr.staff.infrastructure.persistence.adapters;

import com.atlashub.hr.staff.domain.entities.EmployeeDeduction;
import com.atlashub.hr.staff.domain.repositories.EmployeeDeductionRepository;
import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeDeductionJpaEntity;
import com.atlashub.hr.staff.infrastructure.persistence.mappers.EmployeeDeductionMapper;
import com.atlashub.hr.staff.infrastructure.persistence.repositories.SpringDataEmployeeDeductionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeDeductionRepositoryAdapter
        extends JpaBaseRepository<EmployeeDeduction, EmployeeDeductionJpaEntity>
        implements EmployeeDeductionRepository {

    private final SpringDataEmployeeDeductionRepository springDataRepo;

    public EmployeeDeductionRepositoryAdapter(
            SpringDataEmployeeDeductionRepository springDataRepo,
            EmployeeDeductionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_employee_deduction_seq";
    }

    @Override
    public List<EmployeeDeduction> findByEmployeeId(Long employeeId) {
        return springDataRepo.findByEmployeeId(employeeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
