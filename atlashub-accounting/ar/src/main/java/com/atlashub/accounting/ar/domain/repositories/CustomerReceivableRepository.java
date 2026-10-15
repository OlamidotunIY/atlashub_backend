package com.atlashub.accounting.ar.domain.repositories;

import com.atlashub.accounting.ar.domain.entities.CustomerReceivable;
import com.atlashub.accounting.ar.domain.valueobject.ArStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface CustomerReceivableRepository extends Repository<CustomerReceivable> {

    Optional<CustomerReceivable> findByOrganizationIdAndSalesOrderId(Long organizationId, Long salesOrderId);

    List<CustomerReceivable> findByOrganizationIdAndCustomerId(Long organizationId, Long customerId);

    List<CustomerReceivable> findByOrganizationIdAndStatus(Long organizationId, ArStatus status);

    List<CustomerReceivable> findByOrganizationId(Long organizationId);
}
