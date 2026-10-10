package com.atlashub.pay.settlement.application.queries.GetSettlementDetails;

import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.exceptions.SettlementNotFoundException;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GetSettlementDetailsHandler extends Query<GetSettlementDetailsQuery, SettlementResult> {

    private final SettlementRepository settlementRepository;

    public GetSettlementDetailsHandler(SettlementRepository settlementRepository) {
        this.settlementRepository =
                Objects.requireNonNull(settlementRepository, "SettlementRepository must not be null");
    }

    @PreAuthorize("hasAuthority('pay:settlement:manage')")
    @Override
    public SettlementResult execute(GetSettlementDetailsQuery query) {
        Settlement settlement = settlementRepository.findById(query.settlementId())
                .orElseThrow(() -> new SettlementNotFoundException(query.settlementId()));
        if (!settlement.getOrganizationId().equals(query.organizationId()) ||
                settlement.getEnvironment() != query.environment()) {
            throw new SettlementNotFoundException(query.settlementId());
        }

        return new SettlementResult(settlement.getId(), settlement.getOrganizationId(), settlement.getProvider(),
                settlement.getProviderSettlementId(), settlement.getNetAmount(), settlement.getSettledAt(),
                settlement.getStatus(), settlement.getDescription());
    }
}
