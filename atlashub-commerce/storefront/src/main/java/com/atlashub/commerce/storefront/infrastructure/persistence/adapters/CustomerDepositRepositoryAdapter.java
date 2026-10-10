package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.CustomerDeposit;
import com.atlashub.commerce.storefront.domain.repositories.CustomerDepositRepository;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.CustomerDepositJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.CustomerDepositMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataCustomerDepositRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomerDepositRepositoryAdapter
        extends JpaBaseRepository<CustomerDeposit, CustomerDepositJpa>
        implements CustomerDepositRepository {

    private final SpringDataCustomerDepositRepository springDataRepo;

    public CustomerDepositRepositoryAdapter(
            SpringDataCustomerDepositRepository springDataRepo,
            CustomerDepositMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "commerce_customer_deposit_seq";
    }

    @Override
    public Optional<CustomerDeposit> findBySalesOrderId(Long salesOrderId) {
        return springDataRepo.findBySalesOrderId(salesOrderId)
                .map(mapper::toDomain);
    }
}
