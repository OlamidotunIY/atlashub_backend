package com.atlashub.accounting.cash.infrastructure.persistence.repositories;

import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.BankReconciliationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataBankReconciliationRepository extends JpaRepository<BankReconciliationJpaEntity, Long> {

    List<BankReconciliationJpaEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    List<BankReconciliationJpaEntity> findByOrganizationIdAndStatusOrderByCreatedAtDesc(
            Long organizationId,
            ReconciliationStatus status
    );
}
