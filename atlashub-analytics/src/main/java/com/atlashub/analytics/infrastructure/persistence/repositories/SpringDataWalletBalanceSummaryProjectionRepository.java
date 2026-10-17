package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.WalletBalanceSummaryProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataWalletBalanceSummaryProjectionRepository extends JpaRepository<WalletBalanceSummaryProjectionJpa, Long> {

    Optional<WalletBalanceSummaryProjectionJpa> findByOrganizationIdAndEnvironment(
            Long organizationId,
            String environment
    );
}
