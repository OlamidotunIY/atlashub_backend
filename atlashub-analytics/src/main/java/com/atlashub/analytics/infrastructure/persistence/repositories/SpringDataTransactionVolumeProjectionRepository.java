package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.TransactionVolumeProjectionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataTransactionVolumeProjectionRepository extends JpaRepository<TransactionVolumeProjectionJpa, Long> {

    Optional<TransactionVolumeProjectionJpa> findByOrganizationIdAndEnvironmentAndMetricMonth(
            Long organizationId,
            String environment,
            String metricMonth
    );

    @Query("SELECT t FROM TransactionVolumeProjectionJpa t WHERE t.metricMonth = :month")
    List<TransactionVolumeProjectionJpa> findAllByMonth(@Param("month") String month);
}
