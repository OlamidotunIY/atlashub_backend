package com.atlashub.pay.accounts.domain.repositories;

import com.atlashub.pay.accounts.domain.entities.VirtualAccount;
import com.atlashub.pay.accounts.domain.valueobject.OwnerType;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface VirtualAccountRepository extends Repository<VirtualAccount> {
    Optional<VirtualAccount> findByAnchorAccountId(String anchorAccountId);
    Optional<VirtualAccount> findByOrganizationIdAndOwnerType(Long organizationId, OwnerType ownerType);
    Optional<VirtualAccount> findByOrganizationIdAndCustomerId(Long organizationId, String customerId);
    List<VirtualAccount> findAllByOrganizationId(Long organizationId);
}
