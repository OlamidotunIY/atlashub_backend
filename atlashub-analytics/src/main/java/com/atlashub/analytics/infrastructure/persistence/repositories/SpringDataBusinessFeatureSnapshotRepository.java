package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.BusinessFeatureSnapshotJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataBusinessFeatureSnapshotRepository extends JpaRepository<BusinessFeatureSnapshotJpa, Long> {

    @Query("SELECT b FROM BusinessFeatureSnapshotJpa b WHERE b.organizationId = :orgId AND b.environment = :env " +
           "AND b.schemaVersion = :schema ORDER BY b.asOf DESC LIMIT 1")
    Optional<BusinessFeatureSnapshotJpa> findLatest(
            @Param("orgId") Long orgId,
            @Param("env") String env,
            @Param("schema") String schema
    );
}
