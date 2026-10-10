package com.atlashub.pay.settlement.domain.repositories;

import com.atlashub.pay.settlement.domain.entities.SettlementPollCursor;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface SettlementPollCursorRepository extends Repository<SettlementPollCursor> {
    Optional<SettlementPollCursor> findByEnvironmentAndProviderAndSubaccountCode(ApiEnvironment environment,
                                                                                 String provider,
                                                                                 String subaccountCode);
}
