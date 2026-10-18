package com.atlashub.billing.infrastructure.persistence.adapters;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import com.atlashub.billing.infrastructure.persistence.entities.BillingInvoiceJpa;
import com.atlashub.billing.infrastructure.persistence.mappers.BillingInvoiceMapper;
import com.atlashub.billing.infrastructure.persistence.repositories.SpringDataBillingInvoiceRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class BillingInvoiceRepositoryAdapter
        extends JpaBaseRepository<BillingInvoice, BillingInvoiceJpa>
        implements BillingInvoiceRepository {

    private final SpringDataBillingInvoiceRepository springDataRepo;

    public BillingInvoiceRepositoryAdapter(
            SpringDataBillingInvoiceRepository springDataRepo,
            BillingInvoiceMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "billing_invoice_seq";
    }

    @Override
    public Optional<BillingInvoice> findByInvoiceNumber(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            return Optional.empty();
        }
        return springDataRepo.findByInvoiceNumber(invoiceNumber).map(mapper::toDomain);
    }

    @Override
    public Optional<BillingInvoice> findByPaymentReference(String paymentReference) {
        if (paymentReference == null || paymentReference.isBlank()) {
            return Optional.empty();
        }
        return springDataRepo.findByPaymentReference(paymentReference).map(mapper::toDomain);
    }

    @Override
    public List<BillingInvoice> findByOrganizationId(Long organizationId) {
        if (organizationId == null) {
            return List.of();
        }
        return springDataRepo.findByOrganizationId(organizationId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<BillingInvoice> findByOrganizationIdAndStatus(Long organizationId, InvoiceStatus status) {
        if (organizationId == null || status == null) {
            return List.of();
        }
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<BillingInvoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date) {
        if (status == null || date == null) {
            return List.of();
        }
        return springDataRepo.findByStatusAndDueDateBefore(status, date).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<BillingInvoice> findOverdueInvoicesOlderThan(LocalDate date) {
        if (date == null) {
            return List.of();
        }
        return springDataRepo.findOverdueInvoicesOlderThan(date).stream().map(mapper::toDomain).toList();
    }
}
