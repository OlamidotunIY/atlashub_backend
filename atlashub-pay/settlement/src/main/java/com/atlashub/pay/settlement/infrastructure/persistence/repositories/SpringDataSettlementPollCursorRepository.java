package com.atlashub.pay.settlement.infrastructure.persistence.repositories;

import com.atlashub.pay.settlement.infrastructure.persistence.entities.SettlementPollCursorJpa;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataSettlementPollCursorRepository extends JpaRepository<SettlementPollCursorJpa,Long> {
    Optional<SettlementPollCursorJpa> findByEnvironmentAndProviderAndSubaccountCode(
            ApiEnvironment environment,String provider,String subaccountCode);
}
