package com.atlashub.pay.accounts.domain.repositories;

import com.atlashub.pay.accounts.domain.entities.BusinessSubAccount;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface BusinessSubAccountRepository extends Repository<BusinessSubAccount> {
    Optional<BusinessSubAccount> findByOrganizationId(Long organizationId);
    Optional<BusinessSubAccount> findByAnchorSubAccountId(String anchorSubAccountId);
}
