package com.atlashub.hr.staff.infrastructure.persistence.adapters;

import com.atlashub.hr.staff.domain.entities.EmployeeBank;
import com.atlashub.hr.staff.domain.repositories.EmployeeBankRepository;
import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeBankJpaEntity;
import com.atlashub.hr.staff.infrastructure.persistence.mappers.EmployeeBankMapper;
import com.atlashub.hr.staff.infrastructure.persistence.repositories.SpringDataEmployeeBankRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeeBankRepositoryAdapter
        extends JpaBaseRepository<EmployeeBank, EmployeeBankJpaEntity>
        implements EmployeeBankRepository {

    private final SpringDataEmployeeBankRepository springDataRepo;

    public EmployeeBankRepositoryAdapter(
            SpringDataEmployeeBankRepository springDataRepo,
            EmployeeBankMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_employee_bank_seq";
    }

    @Override
    public List<EmployeeBank> findByEmployeeId(Long employeeId) {
        return springDataRepo.findByEmployeeId(employeeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<EmployeeBank> findByEmployeeIdAndIsPrimaryTrue(Long employeeId) {
        return springDataRepo.findByEmployeeIdAndIsPrimaryTrue(employeeId)
                .map(mapper::toDomain);
    }
}
