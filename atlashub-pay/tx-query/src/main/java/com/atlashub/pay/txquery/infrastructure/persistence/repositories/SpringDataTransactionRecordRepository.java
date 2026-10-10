package com.atlashub.pay.txquery.infrastructure.persistence.repositories;

import com.atlashub.pay.txquery.infrastructure.persistence.entities.TransactionRecordJpa;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

public interface SpringDataTransactionRecordRepository
        extends JpaRepository<TransactionRecordJpa, Long>, JpaSpecificationExecutor<TransactionRecordJpa> {
    Optional<TransactionRecordJpa> findByOrganizationIdAndEnvironmentAndReference(
            Long organizationId, ApiEnvironment environment, String reference);

    @Query("select count(t) from TransactionRecordJpa t where t.organizationId=:organizationId " +
            "and t.environment=:environment and t.type=:type and t.createdAt>=:from and t.createdAt<:to")
    long countVolume(@Param("organizationId") Long organizationId, @Param("environment") ApiEnvironment environment,
                     @Param("type") String type, @Param("from") ZonedDateTime from,
                     @Param("to") ZonedDateTime to);

    @Query("select coalesce(sum(t.amount),0) from TransactionRecordJpa t where t.organizationId=:organizationId " +
            "and t.environment=:environment and t.type=:type and t.createdAt>=:from and t.createdAt<:to")
    BigDecimal sumVolume(@Param("organizationId") Long organizationId,
                         @Param("environment") ApiEnvironment environment, @Param("type") String type,
                         @Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);
}
