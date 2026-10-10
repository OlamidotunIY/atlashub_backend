package com.atlashub.pay.settlement.infrastructure.persistence.repositories;

import com.atlashub.pay.settlement.infrastructure.persistence.entities.SettlementCreditEvidenceJpa;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SpringDataSettlementCreditEvidenceRepository extends JpaRepository<SettlementCreditEvidenceJpa, Long> {
    Optional<SettlementCreditEvidenceJpa> findByEnvironmentAndAnchorTransferReference(
            ApiEnvironment environment, String anchorTransferReference);
    List<SettlementCreditEvidenceJpa> findByAnchorDepositAccountIdAndEnvironmentAndAmountAndCurrencyAndMatchedSettlementIdIsNull(
            Long accountId, ApiEnvironment environment, BigDecimal amount, CurrencyCode currency);
}
