package com.atlashub.pay.ledger.domain.repositories;

import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.shared.domain.repository.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LedgerTransactionRepository extends Repository<LedgerTransaction> {
    Optional<LedgerTransaction> findByReference(String reference);
    List<LedgerTransaction> findByOrganizationId(Long organizationId, Pageable pageable);
}
