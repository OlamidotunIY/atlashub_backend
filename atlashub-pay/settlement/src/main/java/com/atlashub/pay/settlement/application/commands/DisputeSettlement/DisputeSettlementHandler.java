package com.atlashub.pay.settlement.application.commands.DisputeSettlement;

import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.exceptions.SettlementNotFoundException;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DisputeSettlementHandler extends Command<DisputeSettlementCommand, Void> {

    private final SettlementRepository settlementRepository;

    public DisputeSettlementHandler(SettlementRepository settlementRepository) {
        this.settlementRepository =
                Objects.requireNonNull(settlementRepository, "SettlementRepository must not be null");
    }

    @PreAuthorize("hasAuthority('pay:settlement:manage')")
    @Override
    public Void execute(DisputeSettlementCommand command) {
        Settlement settlement = settlementRepository.findById(command.settlementId())
                .orElseThrow(() -> new SettlementNotFoundException(command.settlementId()));
        if (!settlement.getOrganizationId().equals(command.organizationId()) ||
                settlement.getEnvironment() != command.environment()) {
            throw new SettlementNotFoundException(command.settlementId());
        }

        settlement.dispute(command.reason());
        settlementRepository.save(settlement);
        return null;
    }
}
