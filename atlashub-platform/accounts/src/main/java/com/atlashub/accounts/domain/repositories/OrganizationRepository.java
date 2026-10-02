package com.atlashub.accounts.domain.repositories;

import com.atlashub.accounts.domain.entities.Organization;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface OrganizationRepository extends Repository<Organization> {
    List<Organization> findAllByIds(List<Long> ids);
}
