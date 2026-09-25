package com.atlashub.iam.domain.repositories;

import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.iam.domain.entities.CustomRole;
import java.util.Optional;
import java.util.List;

public interface CustomRoleRepository extends Repository<CustomRole> {
    List<CustomRole> findByOrganizationId(Long organizationId);
}
