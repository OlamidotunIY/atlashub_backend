package com.atlashub.hr.staff.infrastructure.persistence.adapters;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;
import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeJpaEntity;
import com.atlashub.hr.staff.infrastructure.persistence.mappers.EmployeeMapper;
import com.atlashub.hr.staff.infrastructure.persistence.repositories.SpringDataEmployeeRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeeRepositoryAdapter
        extends JpaBaseRepository<Employee, EmployeeJpaEntity>
        implements EmployeeRepository {

    private final SpringDataEmployeeRepository springDataRepo;

    public EmployeeRepositoryAdapter(
            SpringDataEmployeeRepository springDataRepo,
            EmployeeMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_employee_seq";
    }

    @Override
    public List<Employee> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Employee> findByOrganizationIdAndStatus(Long organizationId, EmployeeStatus status) {
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Employee> findByOrganizationIdAndDepartment(Long organizationId, String department) {
        return springDataRepo.findByOrganizationIdAndDepartment(organizationId, department)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Employee> findByOrganizationIdAndStatusAndDepartment(
            Long organizationId,
            EmployeeStatus status,
            String department
    ) {
        return springDataRepo.findByOrganizationIdAndStatusAndDepartment(organizationId, status, department)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Employee> findByOrganizationIdAndUserId(Long organizationId, Long userId) {
        return springDataRepo.findByOrganizationIdAndUserId(organizationId, userId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Employee> findByOrganizationIdAndEmployeeNumber(Long organizationId, String employeeNumber) {
        return springDataRepo.findByOrganizationIdAndEmployeeNumber(organizationId, employeeNumber)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByOrganizationIdAndUserId(Long organizationId, Long userId) {
        return springDataRepo.existsByOrganizationIdAndUserId(organizationId, userId);
    }
}
