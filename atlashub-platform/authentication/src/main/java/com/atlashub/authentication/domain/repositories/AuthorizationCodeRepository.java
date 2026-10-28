package com.atlashub.authentication.domain.repositories;

import com.atlashub.authentication.domain.entities.AuthorizationCode;
import com.atlashub.shared.domain.repository.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface AuthorizationCodeRepository extends Repository<AuthorizationCode> {
    Optional<AuthorizationCode> findByCodeHash(String codeHash);
    void deleteExpiredBefore(ZonedDateTime threshold);
}
