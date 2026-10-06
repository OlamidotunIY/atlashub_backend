package com.atlashub.accounts.domain.repositories;

import com.atlashub.accounts.domain.entities.User;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface UserRepository extends Repository<User> {
    Optional<User> findByEmail(String email);
}
