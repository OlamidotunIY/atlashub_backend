package com.atlashub.commerce.inventory.infrastructure.persistence.repositories;

import com.atlashub.commerce.inventory.domain.valueobject.ReturnStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.CustomerReturnJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCustomerReturnRepository extends JpaRepository<CustomerReturnJpa, Long> {

    Optional<CustomerReturnJpa> findByOrganizationIdAndSalesOrderId(Long organizationId, Long salesOrderId);

    List<CustomerReturnJpa> findByOrganizationIdAndOutletIdAndStatus(Long organizationId, Long outletId, ReturnStatus status);
}
