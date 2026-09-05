package com.atlashub.billing.adapter.out.persistence.repository;

import com.atlashub.billing.adapter.out.persistence.mapper.BillingInvoiceMapper;
import com.atlashub.billing.domain.model.BillingInvoice;
import com.atlashub.billing.domain.repository.BillingInvoiceRepository;
import com.atlashub.shared.adapter.out.external.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BillingInvoiceRepositoryAdapter implements BillingInvoiceRepository {

    private final SpringDataBillingInvoiceRepository jpaRepository;
    private final BillingInvoiceMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public BillingInvoiceRepositoryAdapter(
            SpringDataBillingInvoiceRepository jpaRepository,
            BillingInvoiceMapper mapper,
            DomainSequenceGenerator sequenceGenerator) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("billing_invoice_seq");
    }

    @Override
    public BillingInvoice save(BillingInvoice invoice) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(invoice)));
    }

    @Override
    public Optional<BillingInvoice> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}
