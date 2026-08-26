package com.atlashub.auth.domain.repository;

import com.atlashub.auth.domain.model.AuthAccount;
import com.atlashub.auth.domain.model.PrincipalType;

import java.util.Optional;

public interface AuthAccountRepository {
    Long nextIdentity();
    AuthAccount save(AuthAccount authAccount);
    Optional<AuthAccount> findById(Long id);
    Optional<AuthAccount> findByPrincipalIdAndType(Long principalId, PrincipalType type);
    Optional<AuthAccount> findByIdentifier(String identifier);
    boolean existsByPrincipalIdAndType(Long principalId, PrincipalType type);
}
