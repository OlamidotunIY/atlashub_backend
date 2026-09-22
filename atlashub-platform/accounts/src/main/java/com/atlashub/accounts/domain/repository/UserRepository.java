package com.atlashub.accounts.domain.repository;

import com.atlashub.accounts.domain.model.User;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface UserRepository extends Repository<User> {
    Optional<User> findByEmail(String email);
}
