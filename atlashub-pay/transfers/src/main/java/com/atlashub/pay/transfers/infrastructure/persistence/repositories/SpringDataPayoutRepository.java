package com.atlashub.pay.transfers.infrastructure.persistence.repositories;

import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.pay.transfers.infrastructure.persistence.entities.PayoutJpa;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataPayoutRepository extends JpaRepository<PayoutJpa, Long> {

    Optional<PayoutJpa> findByReference(String reference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PayoutJpa p WHERE p.id = :id")
    Optional<PayoutJpa> findByIdWithPessimisticLock(@Param("id") Long id);

    Page<PayoutJpa> findByOrganizationIdAndStatus(Long organizationId, PayoutStatus status, Pageable pageable);

    Page<PayoutJpa> findByOrganizationId(Long organizationId, Pageable pageable);

    List<PayoutJpa> findBySourceSystemAndSourceReferenceId(SourceSystem sourceSystem, String sourceReferenceId);
}
