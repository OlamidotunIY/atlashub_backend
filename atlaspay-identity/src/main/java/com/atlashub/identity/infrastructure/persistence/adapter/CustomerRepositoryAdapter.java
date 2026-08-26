package com.atlashub.identity.infrastructure.persistence.adapter;

import com.atlashub.identity.domain.model.Customer;
import com.atlashub.identity.domain.repository.CustomerRepository;
import com.atlashub.identity.infrastructure.persistence.entity.CustomerJpaEntity;
import com.atlashub.identity.infrastructure.persistence.repository.SpringDataCustomerRepository;
import com.atlashub.identity.infrastructure.persistence.mapper.CustomerMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final SpringDataCustomerRepository jpaRepository;
    private final com.atlashub.shared.infrastructure.DomainSequenceGenerator sequenceGenerator;
    private final CustomerMapper mapper;

    public CustomerRepositoryAdapter(SpringDataCustomerRepository jpaRepository, com.atlashub.shared.infrastructure.DomainSequenceGenerator sequenceGenerator, CustomerMapper mapper) {
        this.sequenceGenerator = sequenceGenerator;
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity = mapper.toEntity(customer);
        jpaRepository.save(entity);
        return customer;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByMerchantIdAndEmail(Long merchantId, String email) {
        return jpaRepository.findByIntegrationAndEmail(merchantId, email).map(mapper::toDomain);
    }


    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("customer_seq");
    }

}
