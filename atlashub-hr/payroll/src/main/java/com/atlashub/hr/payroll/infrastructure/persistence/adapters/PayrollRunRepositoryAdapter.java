package com.atlashub.hr.payroll.infrastructure.persistence.adapters;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.hr.payroll.infrastructure.persistence.entities.PayrollRunJpaEntity;
import com.atlashub.hr.payroll.infrastructure.persistence.mappers.PayrollRunMapper;
import com.atlashub.hr.payroll.infrastructure.persistence.repositories.SpringDataPayrollRunRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class PayrollRunRepositoryAdapter
        extends JpaBaseRepository<PayrollRun, PayrollRunJpaEntity>
        implements PayrollRunRepository {

    private final SpringDataPayrollRunRepository springDataRepo;

    public PayrollRunRepositoryAdapter(
            SpringDataPayrollRunRepository springDataRepo,
            PayrollRunMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_payroll_run_seq";
    }

    @Override
    public Optional<PayrollRun> findByOrganizationIdAndPeriod(Long organizationId, YearMonth period) {
        return springDataRepo.findByOrganizationIdAndPeriod(organizationId, period.toString())
                .map(mapper::toDomain);
    }

    @Override
    public List<PayrollRun> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<PayrollRun> findByOrganizationIdAndStatus(Long organizationId, PayrollStatus status) {
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<PayrollRun> findByStatusAndCreatedAtBefore(PayrollStatus status, ZonedDateTime threshold) {
        return springDataRepo.findByStatusAndCreatedAtBefore(status, threshold)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
