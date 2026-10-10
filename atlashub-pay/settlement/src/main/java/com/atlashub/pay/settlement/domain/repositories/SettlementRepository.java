package com.atlashub.pay.settlement.domain.repositories;

import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PageResult;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends Repository<Settlement> {

    Optional<Settlement> findByProviderAndEnvironmentAndProviderSettlementId(
            com.atlashub.pay.settlement.domain.valueobject.PaymentProvider provider, ApiEnvironment environment,
            String providerSettlementId);

    List<Settlement> findAwaitingAnchorCredit(Long anchorDepositAccountId, ApiEnvironment environment,
                                              Money receivedAmount);

    List<Settlement> findAwaitingAnchorCreditCreatedBefore(ZonedDateTime cutoff);

    PageResult<Settlement> findByOrganizationId(Long organizationId, ApiEnvironment environment,
                                                SettlementStatus status, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                                int page, int size);
}
