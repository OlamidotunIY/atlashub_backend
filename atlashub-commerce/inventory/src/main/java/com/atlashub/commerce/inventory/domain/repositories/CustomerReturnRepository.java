package com.atlashub.commerce.inventory.domain.repositories;

import com.atlashub.commerce.inventory.domain.entities.CustomerReturn;
import com.atlashub.commerce.inventory.domain.valueobject.ReturnStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface CustomerReturnRepository extends Repository<CustomerReturn> {

    Optional<CustomerReturn> findByOrganizationIdAndSalesOrderId(Long orgId, Long salesOrderId);

    List<CustomerReturn> findByOrganizationIdAndOutletIdAndStatus(Long orgId, Long outletId, ReturnStatus status);
}
