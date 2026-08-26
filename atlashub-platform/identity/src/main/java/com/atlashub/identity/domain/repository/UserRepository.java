package com.atlashub.identity.domain.repository;

import com.atlashub.identity.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Long nextIdentity();
    User save(User User);
    Optional<User> findById(Long id);
    Optional<User> findByOrganizationIdAndEmail(Long OrganizationId, String email);
}
