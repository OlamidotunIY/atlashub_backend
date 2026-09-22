package com.atlashub.accounts.domain.repository;

import com.atlashub.accounts.domain.model.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository {
    Long nextIdentity();
    Organization save(Organization organization);
    Optional<Organization> findById(Long id);
    List<Organization> findAllByIds(List<Long> ids);
}
