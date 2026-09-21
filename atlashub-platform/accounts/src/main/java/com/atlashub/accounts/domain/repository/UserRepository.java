package com.atlashub.accounts.domain.repository;

import com.atlashub.accounts.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Long nextIdentity();
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
}
