package com.atlashub.accounting.ar.infrastructure.persistence.repositories;

import com.atlashub.accounting.ar.domain.valueobject.ArStatus;
import com.atlashub.accounting.ar.infrastructure.persistence.entities.CustomerReceivableJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataCustomerReceivableRepository extends JpaRepository<CustomerReceivableJpaEntity, Long> {

    Optional<CustomerReceivableJpaEntity> findByOrganizationIdAndSalesOrderId(Long organizationId, Long salesOrderId);

    List<CustomerReceivableJpaEntity> findByOrganizationIdAndCustomerId(Long organizationId, Long customerId);

    List<CustomerReceivableJpaEntity> findByOrganizationIdAndStatus(Long organizationId, ArStatus status);

    List<CustomerReceivableJpaEntity> findByOrganizationId(Long organizationId);
}
