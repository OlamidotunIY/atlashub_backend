package com.atlashub.commerce.storefront.infrastructure.persistence.repositories;

import com.atlashub.commerce.storefront.infrastructure.persistence.entities.CustomerDepositJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCustomerDepositRepository extends JpaRepository<CustomerDepositJpa, Long> {

    Optional<CustomerDepositJpa> findBySalesOrderId(Long salesOrderId);
}
