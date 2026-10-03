package com.atlashub.pay.accounts.domain.repositories;

import com.atlashub.pay.accounts.domain.entities.BusinessSubAccount;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;

public interface BusinessSubAccountRepository extends Repository<BusinessSubAccount> {
    Optional<BusinessSubAccount> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
    Optional<BusinessSubAccount> findByAnchorSubAccountIdAndEnvironment(String anchorSubAccountId, ApiEnvironment environment);
}
