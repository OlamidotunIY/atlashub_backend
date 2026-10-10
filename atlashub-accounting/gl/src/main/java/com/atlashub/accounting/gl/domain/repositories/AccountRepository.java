package com.atlashub.accounting.gl.domain.repositories;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends Repository<Account> {

    Optional<Account> findByOrganizationIdAndCode(Long organizationId, String code);

    List<Account> findByOrganizationId(Long organizationId);

    List<Account> findByOrganizationIdAndIsActiveTrue(Long organizationId);
}
