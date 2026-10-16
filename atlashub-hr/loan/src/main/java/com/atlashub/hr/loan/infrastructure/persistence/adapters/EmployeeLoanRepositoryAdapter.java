package com.atlashub.hr.loan.infrastructure.persistence.adapters;

import com.atlashub.hr.loan.domain.entities.EmployeeLoan;
import com.atlashub.hr.loan.domain.repositories.EmployeeLoanRepository;
import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.hr.loan.infrastructure.persistence.entities.EmployeeLoanJpaEntity;
import com.atlashub.hr.loan.infrastructure.persistence.mappers.EmployeeLoanMapper;
import com.atlashub.hr.loan.infrastructure.persistence.repositories.SpringDataEmployeeLoanRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeeLoanRepositoryAdapter
        extends JpaBaseRepository<EmployeeLoan, EmployeeLoanJpaEntity>
        implements EmployeeLoanRepository {

    private final SpringDataEmployeeLoanRepository springDataRepo;

    public EmployeeLoanRepositoryAdapter(
            SpringDataEmployeeLoanRepository springDataRepo,
            EmployeeLoanMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_employee_loan_seq";
    }

    @Override
    public List<EmployeeLoan> findByEmployeeIdAndStatus(Long employeeId, LoanStatus status) {
        return springDataRepo.findByEmployeeIdAndStatus(employeeId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<EmployeeLoan> findByEmployeeIdAndStatusIn(Long employeeId, List<LoanStatus> statuses) {
        return springDataRepo.findByEmployeeIdAndStatusIn(employeeId, statuses)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<EmployeeLoan> findByIdWithLock(Long loanId) {
        return springDataRepo.findByIdWithLock(loanId)
                .map(mapper::toDomain);
    }

    @Override
    public List<EmployeeLoan> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<EmployeeLoan> findByOrganizationIdAndStatus(Long organizationId, LoanStatus status) {
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<EmployeeLoan> findByOrganizationIdAndEmployeeIdAndStatus(Long organizationId, Long employeeId, LoanStatus status) {
        return springDataRepo.findByOrganizationIdAndEmployeeIdAndStatus(organizationId, employeeId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<EmployeeLoan> findByOrganizationIdAndEmployeeId(Long organizationId, Long employeeId) {
        return springDataRepo.findByOrganizationIdAndEmployeeId(organizationId, employeeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
