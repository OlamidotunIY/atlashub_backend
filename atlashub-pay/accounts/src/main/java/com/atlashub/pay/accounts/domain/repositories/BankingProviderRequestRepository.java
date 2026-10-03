package com.atlashub.pay.accounts.domain.repositories;

import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface BankingProviderRequestRepository extends Repository<BankingProviderRequest> {
    Optional<BankingProviderRequest> findByRequestReferenceAndApiEnvironment(String requestReference, String apiEnvironment);
    List<BankingProviderRequest> findPending(int limit);
}
