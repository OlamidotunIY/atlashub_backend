package com.atlashub.commerce.storefront.domain.repositories;

import com.atlashub.commerce.storefront.domain.entities.CustomerCredit;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface CustomerCreditRepository extends Repository<CustomerCredit> {

    Optional<CustomerCredit> findByOrganizationIdAndCustomerId(Long organizationId, Long customerId);
}
