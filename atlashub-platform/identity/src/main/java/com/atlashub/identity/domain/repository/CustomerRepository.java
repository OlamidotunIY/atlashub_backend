package com.atlashub.identity.domain.repository;

import com.atlashub.identity.domain.model.Customer;

import java.util.Optional;

public interface CustomerRepository {
    Long nextIdentity();
    Customer save(Customer customer);
    Optional<Customer> findById(Long id);
    Optional<Customer> findByMerchantIdAndEmail(Long merchantId, String email);
}
