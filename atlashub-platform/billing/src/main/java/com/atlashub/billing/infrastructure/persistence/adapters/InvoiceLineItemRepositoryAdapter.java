package com.atlashub.billing.infrastructure.persistence.adapters;

import com.atlashub.billing.domain.entities.InvoiceLineItem;
import com.atlashub.billing.domain.repositories.InvoiceLineItemRepository;
import com.atlashub.billing.infrastructure.persistence.entities.InvoiceLineItemJpa;
import com.atlashub.billing.infrastructure.persistence.mappers.InvoiceLineItemMapper;
import com.atlashub.billing.infrastructure.persistence.repositories.SpringDataInvoiceLineItemRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoiceLineItemRepositoryAdapter
        extends JpaBaseRepository<InvoiceLineItem, InvoiceLineItemJpa>
        implements InvoiceLineItemRepository {

    private final SpringDataInvoiceLineItemRepository springDataRepo;

    public InvoiceLineItemRepositoryAdapter(
            SpringDataInvoiceLineItemRepository springDataRepo,
            InvoiceLineItemMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "billing_invoice_line_item_seq";
    }

    @Override
    public List<InvoiceLineItem> findByInvoiceId(Long invoiceId) {
        if (invoiceId == null) {
            return List.of();
        }
        return springDataRepo.findByInvoiceId(invoiceId).stream().map(mapper::toDomain).toList();
    }
}
