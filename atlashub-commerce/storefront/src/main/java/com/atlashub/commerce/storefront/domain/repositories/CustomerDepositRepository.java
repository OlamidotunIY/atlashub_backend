package com.atlashub.commerce.storefront.domain.repositories;

import com.atlashub.commerce.storefront.domain.entities.CustomerDeposit;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface CustomerDepositRepository extends Repository<CustomerDeposit> {

    Optional<CustomerDeposit> findBySalesOrderId(Long salesOrderId);
}
