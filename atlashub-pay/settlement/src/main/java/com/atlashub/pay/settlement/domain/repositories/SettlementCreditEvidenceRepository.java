package com.atlashub.pay.settlement.domain.repositories;

import com.atlashub.pay.settlement.domain.entities.SettlementCreditEvidence;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.shared.domain.valueobject.Money;

import java.util.List;
import java.util.Optional;

public interface SettlementCreditEvidenceRepository extends Repository<SettlementCreditEvidence> {
    Optional<SettlementCreditEvidence> findByEnvironmentAndAnchorTransferReference(ApiEnvironment environment,
                                                                                   String transferReference);

    List<SettlementCreditEvidence> findUnmatched(Long anchorDepositAccountId, ApiEnvironment environment, Money amount);
}
