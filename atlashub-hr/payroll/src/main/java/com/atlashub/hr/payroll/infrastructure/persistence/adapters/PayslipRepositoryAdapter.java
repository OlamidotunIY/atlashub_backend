package com.atlashub.hr.payroll.infrastructure.persistence.adapters;

import com.atlashub.hr.payroll.domain.entities.Payslip;
import com.atlashub.hr.payroll.domain.entities.PayslipLineItem;
import com.atlashub.hr.payroll.domain.repositories.PayslipRepository;
import com.atlashub.hr.payroll.infrastructure.persistence.entities.PayslipJpaEntity;
import com.atlashub.hr.payroll.infrastructure.persistence.entities.PayslipLineItemJpaEntity;
import com.atlashub.hr.payroll.infrastructure.persistence.mappers.PayslipMapper;
import com.atlashub.hr.payroll.infrastructure.persistence.repositories.SpringDataPayslipLineItemRepository;
import com.atlashub.hr.payroll.infrastructure.persistence.repositories.SpringDataPayslipRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PayslipRepositoryAdapter
        extends JpaBaseRepository<Payslip, PayslipJpaEntity>
        implements PayslipRepository {

    private final SpringDataPayslipRepository springDataRepo;
    private final SpringDataPayslipLineItemRepository lineItemRepo;

    public PayslipRepositoryAdapter(
            SpringDataPayslipRepository springDataRepo,
            PayslipMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher,
            SpringDataPayslipLineItemRepository lineItemRepo
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
        this.lineItemRepo = lineItemRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_payslip_seq";
    }

    @Override
    public Optional<Payslip> findById(Long id) {
        return super.findById(id).map(this::enrichWithLineItems);
    }

    @Override
    public List<Payslip> findByPayrollRunId(Long payrollRunId) {
        return springDataRepo.findByPayrollRunId(payrollRunId)
                .stream()
                .map(mapper::toDomain)
                .map(this::enrichWithLineItems)
                .toList();
    }

    @Override
    public List<Payslip> findByEmployeeIdAndYear(Long employeeId, int year) {
        return springDataRepo.findByEmployeeId(employeeId)
                .stream()
                .map(mapper::toDomain)
                .map(this::enrichWithLineItems)
                .toList();
    }

    private Payslip enrichWithLineItems(Payslip payslip) {
        if (payslip == null || payslip.getId() == null) {
            return payslip;
        }
        List<PayslipLineItem> items = lineItemRepo.findByPayslipId(payslip.getId()).stream()
                .map(this::toLineItemDomain)
                .toList();

        return new Payslip(
                payslip.getId(),
                payslip.getPayrollRunId(),
                payslip.getEmployeeId(),
                payslip.getEmployeeName(),
                payslip.getGrossPay(),
                payslip.getTotalAllowances(),
                payslip.getTotalDeductions(),
                payslip.getNetPay(),
                items,
                payslip.getStatus()
        );
    }

    private PayslipLineItem toLineItemDomain(PayslipLineItemJpaEntity jpa) {
        CurrencyCode currency = jpa.getCurrency() != null
                ? CurrencyCode.valueOf(jpa.getCurrency())
                : CurrencyCode.NGN;
        Money amount = jpa.getAmount() != null
                ? Money.of(jpa.getAmount(), currency)
                : Money.zero(currency);

        return new PayslipLineItem(
                jpa.getId(),
                jpa.getPayslipId(),
                jpa.getName(),
                jpa.getType(),
                amount
        );
    }
}
