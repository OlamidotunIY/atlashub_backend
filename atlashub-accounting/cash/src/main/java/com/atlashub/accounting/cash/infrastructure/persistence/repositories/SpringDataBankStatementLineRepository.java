package com.atlashub.accounting.cash.infrastructure.persistence.repositories;

import com.atlashub.accounting.cash.infrastructure.persistence.entities.BankStatementLineJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataBankStatementLineRepository extends JpaRepository<BankStatementLineJpaEntity, Long> {

    List<BankStatementLineJpaEntity> findByReconciliationIdOrderById(Long reconciliationId);

    void deleteByReconciliationId(Long reconciliationId);
}
