package com.atlashub.authentication.domain.repositories;

import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface AuthAccountRepository extends Repository<AuthAccount> {
    Optional<AuthAccount> findByEmail(String email);
}
