package com.atlashub.commerce.storefront.infrastructure.persistence.repositories;

import com.atlashub.commerce.storefront.infrastructure.persistence.entities.CustomerCreditJpa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataCustomerCreditRepository extends JpaRepository<CustomerCreditJpa, Long> {

    Optional<CustomerCreditJpa> findByOrganizationIdAndCustomerId(Long organizationId, Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CustomerCreditJpa c WHERE c.organizationId = :organizationId AND c.customerId = :customerId")
    Optional<CustomerCreditJpa> findByOrganizationIdAndCustomerIdForUpdate(
            @Param("organizationId") Long organizationId,
            @Param("customerId") Long customerId
    );
}
