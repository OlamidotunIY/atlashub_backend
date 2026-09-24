package com.atlashub.accounts.infrastructure.persistence.repositories;

import com.atlashub.accounts.infrastructure.persistence.entities.OrganizationJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrganizationRepository extends JpaRepository<OrganizationJPA, Long> {
}
