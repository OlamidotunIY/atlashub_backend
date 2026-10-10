package com.atlashub.pay.settlement.application.commands.ConfirmSettlement;

import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.entities.SettlementCreditEvidence;
import com.atlashub.pay.settlement.domain.exceptions.InvalidSettlementStateException;
import com.atlashub.pay.settlement.domain.exceptions.SettlementNotFoundException;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.pay.settlement.domain.repositories.SettlementCreditEvidenceRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ConfirmSettlementHandler extends Command<ConfirmSettlementCommand, Void> {

    private final SettlementRepository settlementRepository;
    private final SettlementCreditEvidenceRepository evidenceRepository;

    public ConfirmSettlementHandler(SettlementRepository settlementRepository,
                                    SettlementCreditEvidenceRepository evidenceRepository) {
        this.settlementRepository = Objects.requireNonNull(settlementRepository, "SettlementRepository must not be null");
        this.evidenceRepository = Objects.requireNonNull(evidenceRepository, "Evidence repository must not be null");
    }

    @Override
    public Void execute(ConfirmSettlementCommand command) {
        SettlementCreditEvidence evidence = evidenceRepository
                .findByEnvironmentAndAnchorTransferReference(command.environment(), command.anchorTransferReference())
                .orElseGet(() -> evidenceRepository.save(SettlementCreditEvidence.record(
                        evidenceRepository.nextIdentity(), command.organizationId(), command.environment(),
                        command.anchorDepositAccountId(), command.anchorTransferReference(), command.receivedAmount(),
                        command.receivedAt())));
        if (evidence.isMatched()) return null;

        var candidates = settlementRepository.findAwaitingAnchorCredit(command.anchorDepositAccountId(),
                command.environment(), command.receivedAmount()).stream()
                .filter(s -> s.getOrganizationId().equals(command.organizationId())).toList();
        if (candidates.isEmpty()) return null;
        if (candidates.size() > 1) {
            candidates.forEach(s -> { s.requireReconciliation("Ambiguous Anchor credit match"); settlementRepository.save(s); });
            throw new InvalidSettlementStateException("Anchor credit matches more than one settlement");
        }
        Settlement settlement = candidates.getFirst();
        settlement.confirm(command.anchorTransferReference());
        settlementRepository.save(settlement);
        evidence.match(settlement.getId());
        evidenceRepository.save(evidence);
        return null;
    }
}
