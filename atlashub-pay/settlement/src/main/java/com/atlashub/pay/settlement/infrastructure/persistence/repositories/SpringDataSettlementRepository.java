package com.atlashub.pay.settlement.infrastructure.persistence.repositories;

import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.pay.settlement.infrastructure.persistence.entities.SettlementJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.List;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;

public interface SpringDataSettlementRepository extends JpaRepository<SettlementJpa, Long> {

    Optional<SettlementJpa> findByProviderAndEnvironmentAndProviderSettlementId(
            PaymentProvider provider, ApiEnvironment environment, String providerSettlementId);

    List<SettlementJpa> findByAnchorDepositAccountIdAndEnvironmentAndStatusAndNetAmountAndCurrency(
            Long anchorDepositAccountId, ApiEnvironment environment, SettlementStatus status,
            BigDecimal netAmount, CurrencyCode currency);

    List<SettlementJpa> findByStatusAndCreatedAtBefore(SettlementStatus status, ZonedDateTime cutoff);

    @Query("SELECT s FROM SettlementJpa s WHERE s.organizationId = :organizationId AND s.environment = :environment " +
            "AND (:status IS NULL OR s.status = :status) " +
            "AND (CAST(:dateFrom AS timestamp) IS NULL OR s.settledAt >= :dateFrom) " +
            "AND (CAST(:dateTo AS timestamp) IS NULL OR s.settledAt <= :dateTo)")
    Page<SettlementJpa> searchSettlements(@Param("organizationId") Long organizationId,
                                          @Param("environment") ApiEnvironment environment,
                                          @Param("status") SettlementStatus status,
                                          @Param("dateFrom") ZonedDateTime dateFrom,
                                          @Param("dateTo") ZonedDateTime dateTo, Pageable pageable);
}
