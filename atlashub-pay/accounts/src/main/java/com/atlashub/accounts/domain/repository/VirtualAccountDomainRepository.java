package com.atlashub.accounts.domain.repository;

import com.atlashub.accounts.domain.model.VirtualAccount;
import com.atlashub.shared.domain.valueobject.NUBAN;
import com.atlashub.shared.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface VirtualAccountDomainRepository extends Repository<VirtualAccount, Long> {
    Long nextIdentity();
    Optional<VirtualAccount> findByNuban(NUBAN nuban);
    List<VirtualAccount> findByIntegration(Long integration);
    boolean existsByNuban(NUBAN nuban);
}
