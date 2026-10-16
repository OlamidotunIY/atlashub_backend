package com.atlashub.accounting.ap.infrastructure.persistence.adapters;

import com.atlashub.accounting.ap.domain.entities.SupplierInvoice;
import com.atlashub.accounting.ap.domain.repositories.SupplierInvoiceRepository;
import com.atlashub.accounting.ap.domain.valueobject.ApStatus;
import com.atlashub.accounting.ap.infrastructure.persistence.entities.SupplierInvoiceJpaEntity;
import com.atlashub.accounting.ap.infrastructure.persistence.mappers.SupplierInvoiceMapper;
import com.atlashub.accounting.ap.infrastructure.persistence.repositories.SpringDataSupplierInvoiceRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SupplierInvoiceRepositoryAdapter
        extends JpaBaseRepository<SupplierInvoice, SupplierInvoiceJpaEntity>
        implements SupplierInvoiceRepository {

    private final SpringDataSupplierInvoiceRepository springDataRepository;

    public SupplierInvoiceRepositoryAdapter(SpringDataSupplierInvoiceRepository springDataRepository,
                                           SupplierInvoiceMapper mapper,
                                           DomainSequenceGenerator sequenceGenerator,
                                           DomainEventPublisher eventPublisher) {
        super(springDataRepository, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepository = springDataRepository;
    }

    @Override
    protected String getSequenceName() {
        return "accounting_supplier_invoice_seq";
    }

    @Override
    public Optional<SupplierInvoice> findByOrganizationIdAndPurchaseOrderId(Long organizationId, Long purchaseOrderId) {
        return springDataRepository.findByOrganizationIdAndPurchaseOrderId(organizationId, purchaseOrderId)
                .map(mapper::toDomain);
    }

    @Override
    public List<SupplierInvoice> findByOrganizationIdAndSupplierId(Long organizationId, Long supplierId) {
        return springDataRepository.findByOrganizationIdAndSupplierId(organizationId, supplierId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SupplierInvoice> findByOrganizationIdAndStatus(Long organizationId, ApStatus status) {
        return springDataRepository.findByOrganizationIdAndStatus(organizationId, status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SupplierInvoice> findByOrganizationId(Long organizationId) {
        return springDataRepository.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
