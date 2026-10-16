package com.atlashub.pay.mandates.infrastructure.persistence.adapters;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
import com.atlashub.pay.mandates.infrastructure.persistence.entities.PaymentMandateJpa;
import com.atlashub.pay.mandates.infrastructure.persistence.mappers.PaymentMandateMapper;
import com.atlashub.pay.mandates.infrastructure.persistence.repositories.SpringDataPaymentMandateRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PaymentMandateRepositoryAdapter
        extends JpaBaseRepository<PaymentMandate, PaymentMandateJpa>
        implements PaymentMandateRepository {

    private final SpringDataPaymentMandateRepository springDataRepo;

    public PaymentMandateRepositoryAdapter(
            SpringDataPaymentMandateRepository springDataRepo,
            PaymentMandateMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "mandate_id_seq";
    }

    @Override
    public Optional<PaymentMandate> findByIdAndOrganizationId(Long id, Long organizationId) {
        return springDataRepo.findByIdAndOrganizationId(id, organizationId).map(mapper::toDomain);
    }

    @Override
    public List<PaymentMandate> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentMandate> findByOrganizationIdAndCustomerId(Long organizationId, String customerId) {
        return springDataRepo.findByOrganizationIdAndCustomerId(organizationId, customerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentMandate> findByOrganizationIdAndStatus(Long organizationId, MandateStatus status) {
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentMandate> findByOrganizationIdAndCustomerIdAndStatus(Long organizationId, String customerId, MandateStatus status) {
        return springDataRepo.findByOrganizationIdAndCustomerIdAndStatus(organizationId, customerId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentMandate> findActiveMandatesDueForCharge(LocalDate today) {
        return springDataRepo.findActiveMandatesDueForCharge(today).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
