package com.atlashub.accounting.cash.infrastructure.persistence.repositories;

import com.atlashub.accounting.cash.infrastructure.persistence.entities.ReconciliationItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataReconciliationItemRepository extends JpaRepository<ReconciliationItemJpaEntity, Long> {

    List<ReconciliationItemJpaEntity> findByReconciliationIdOrderById(Long reconciliationId);

    void deleteByReconciliationId(Long reconciliationId);
}
