package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.HeadcountProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataHeadcountProjectionRepository extends JpaRepository<HeadcountProjectionJpa, Long> {

    Optional<HeadcountProjectionJpa> findByOrganizationIdAndEnvironment(
            Long organizationId,
            String environment
    );
}
