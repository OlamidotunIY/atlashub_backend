package com.atlashub.pay.accounts.domain.repositories;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;

public interface BusinessDepositAccountRepository extends Repository<BusinessDepositAccount> {
    Optional<BusinessDepositAccount> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
    Optional<BusinessDepositAccount> findByAnchorAccountIdAndEnvironment(String anchorAccountId, ApiEnvironment environment);
    List<BusinessDepositAccount> findPendingReconciliation(int limit);
}
