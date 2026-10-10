package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.CustomerCredit;
import com.atlashub.commerce.storefront.domain.repositories.CustomerCreditRepository;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.CustomerCreditJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.CustomerCreditMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataCustomerCreditRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomerCreditRepositoryAdapter
        extends JpaBaseRepository<CustomerCredit, CustomerCreditJpa>
        implements CustomerCreditRepository {

    private final SpringDataCustomerCreditRepository springDataRepo;

    public CustomerCreditRepositoryAdapter(
            SpringDataCustomerCreditRepository springDataRepo,
            CustomerCreditMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "commerce_customer_credit_seq";
    }

    @Override
    public Optional<CustomerCredit> findByOrganizationIdAndCustomerId(Long organizationId, Long customerId) {
        return springDataRepo.findByOrganizationIdAndCustomerId(organizationId, customerId)
                .map(mapper::toDomain);
    }
}
