package com.atlashub.pay.ledger.infrastructure.persistence.repositories;

import com.atlashub.pay.ledger.infrastructure.persistence.entities.LedgerAccountJpa;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;
import java.util.Optional;

public interface SpringDataLedgerAccountRepository extends JpaRepository<LedgerAccountJpa, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<LedgerAccountJpa> findById(Long id); 

    List<LedgerAccountJpa> findAllByOrganizationId(Long organizationId);
    
    Optional<LedgerAccountJpa> findByOrganizationIdAndAccountType(Long organizationId, String accountType);
    
    Optional<LedgerAccountJpa> findByOrganizationIdAndOutletId(Long organizationId, Long outletId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    List<LedgerAccountJpa> findAllByIdIn(List<Long> ids); 
}
