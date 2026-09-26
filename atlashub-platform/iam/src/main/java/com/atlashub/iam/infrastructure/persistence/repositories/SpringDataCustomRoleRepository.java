package com.atlashub.iam.infrastructure.persistence.repositories;

import com.atlashub.iam.infrastructure.persistence.entities.CustomRoleJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCustomRoleRepository extends JpaRepository<CustomRoleJpa, Long> {
    List<CustomRoleJpa> findByOrganizationId(Long organizationId);
}
