package com.atlashub.accounts.domain.repository;

import com.atlashub.accounts.domain.model.InternalAccount;
import com.atlashub.accounts.domain.valueobject.InternalAccountType;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface InternalAccountDomainRepository extends Repository<InternalAccount> {
    List<InternalAccount> findByOrganizationId(Long organizationId);
    Optional<InternalAccount> findByOrganizationIdAndType(Long organizationId, InternalAccountType type);
    boolean existsByOrganizationIdAndType(Long organizationId, InternalAccountType type);
}
