package com.atlashub.identity.domain.repository;

import com.atlashub.identity.domain.model.Organization;

import java.util.Optional;

public interface OrganizationRepository {
    Long nextIdentity();
    Organization save(Organization Organization);
    Optional<Organization> findById(Long id);
    Optional<Organization> findByEmail(String email);
}
