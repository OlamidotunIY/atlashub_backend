package com.atlashub.pay.accounts.domain.repositories;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface BusinessDepositAccountRepository extends Repository<BusinessDepositAccount> {
    Optional<BusinessDepositAccount> findByOrganizationId(Long organizationId);
    Optional<BusinessDepositAccount> findByAnchorAccountId(String anchorAccountId);
}
