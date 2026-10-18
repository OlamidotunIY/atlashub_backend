package com.atlashub.billing.infrastructure.persistence.repositories;

import com.atlashub.billing.infrastructure.persistence.entities.TransactionVolumeLedgerJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataTransactionVolumeLedgerRepository extends JpaRepository<TransactionVolumeLedgerJpa, Long> {

    Optional<TransactionVolumeLedgerJpa> findByOrganizationIdAndMonth(Long organizationId, String month);
}
