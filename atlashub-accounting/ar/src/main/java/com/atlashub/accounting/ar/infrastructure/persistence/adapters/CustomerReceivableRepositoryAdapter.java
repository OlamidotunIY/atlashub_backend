package com.atlashub.accounting.ar.infrastructure.persistence.adapters;

import com.atlashub.accounting.ar.domain.entities.CustomerReceivable;
import com.atlashub.accounting.ar.domain.repositories.CustomerReceivableRepository;
import com.atlashub.accounting.ar.domain.valueobject.ArStatus;
import com.atlashub.accounting.ar.infrastructure.persistence.entities.CustomerReceivableJpaEntity;
import com.atlashub.accounting.ar.infrastructure.persistence.mappers.CustomerReceivableMapper;
import com.atlashub.accounting.ar.infrastructure.persistence.repositories.SpringDataCustomerReceivableRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CustomerReceivableRepositoryAdapter
        extends JpaBaseRepository<CustomerReceivable, CustomerReceivableJpaEntity>
        implements CustomerReceivableRepository {

    private final SpringDataCustomerReceivableRepository springDataRepository;

    public CustomerReceivableRepositoryAdapter(SpringDataCustomerReceivableRepository springDataRepository,
                                               CustomerReceivableMapper mapper,
                                               DomainSequenceGenerator sequenceGenerator,
                                               DomainEventPublisher eventPublisher) {
        super(springDataRepository, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepository = springDataRepository;
    }

    @Override
    protected String getSequenceName() {
        return "accounting_customer_receivable_seq";
    }

    @Override
    public Optional<CustomerReceivable> findByOrganizationIdAndSalesOrderId(Long organizationId, Long salesOrderId) {
        return springDataRepository.findByOrganizationIdAndSalesOrderId(organizationId, salesOrderId)
                .map(mapper::toDomain);
    }

    @Override
    public List<CustomerReceivable> findByOrganizationIdAndCustomerId(Long organizationId, Long customerId) {
        return springDataRepository.findByOrganizationIdAndCustomerId(organizationId, customerId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<CustomerReceivable> findByOrganizationIdAndStatus(Long organizationId, ArStatus status) {
        return springDataRepository.findByOrganizationIdAndStatus(organizationId, status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<CustomerReceivable> findByOrganizationId(Long organizationId) {
        return springDataRepository.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
