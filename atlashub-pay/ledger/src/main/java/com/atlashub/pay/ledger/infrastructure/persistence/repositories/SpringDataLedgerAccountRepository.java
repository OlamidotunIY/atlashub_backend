package com.atlashub.pay.ledger.infrastructure.persistence.repositories;

import com.atlashub.pay.ledger.infrastructure.persistence.entities.LedgerAccountJpa;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataLedgerAccountRepository extends JpaRepository<LedgerAccountJpa, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<LedgerAccountJpa> findById(Long id); 

    List<LedgerAccountJpa> findAllByOrganizationId(Long organizationId);
    
    Optional<LedgerAccountJpa> findByOrganizationIdAndAccountType(Long organizationId, String accountType);
    
    Optional<LedgerAccountJpa> findByOrganizationIdAndOutletId(Long organizationId, Long outletId);

    Optional<LedgerAccountJpa> findByOrganizationIdAndPartyTypeAndPartyReferenceIdAndAccountType(
            Long organizationId, String partyType, String partyReferenceId, String accountType);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("select account from LedgerAccountJpa account where account.id in :ids order by account.id")
    List<LedgerAccountJpa> findAllByIdIn(@Param("ids") List<Long> ids); 
}
