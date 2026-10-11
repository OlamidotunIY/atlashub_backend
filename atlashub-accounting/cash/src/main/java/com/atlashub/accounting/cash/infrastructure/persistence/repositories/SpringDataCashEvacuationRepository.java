package com.atlashub.accounting.cash.infrastructure.persistence.repositories;

import com.atlashub.accounting.cash.domain.valueobject.EvacuationStatus;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.CashEvacuationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCashEvacuationRepository extends JpaRepository<CashEvacuationJpaEntity, Long> {

    List<CashEvacuationJpaEntity> findByOrganizationIdOrderByEvacuatedAtDesc(Long organizationId);

    List<CashEvacuationJpaEntity> findByOrganizationIdAndStatusOrderByEvacuatedAtDesc(
            Long organizationId,
            EvacuationStatus status
    );
}
