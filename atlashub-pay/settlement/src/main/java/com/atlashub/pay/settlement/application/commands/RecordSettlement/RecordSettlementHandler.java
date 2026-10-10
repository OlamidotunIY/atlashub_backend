package com.atlashub.pay.settlement.application.commands.RecordSettlement;

import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.exceptions.DuplicateSettlementException;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.pay.settlement.domain.repositories.SettlementCreditEvidenceRepository;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RecordSettlementHandler extends Command<RecordSettlementCommand, Long> {

    private final SettlementRepository settlementRepository;
    private final SettlementCreditEvidenceRepository evidenceRepository;

    public RecordSettlementHandler(SettlementRepository settlementRepository,
                                   SettlementCreditEvidenceRepository evidenceRepository) {
        this.settlementRepository =
                Objects.requireNonNull(settlementRepository, "SettlementRepository must not be null");
        this.evidenceRepository = Objects.requireNonNull(evidenceRepository, "Evidence repository must not be null");
    }

    @Override
    public Long execute(RecordSettlementCommand command) {
        var existing = settlementRepository.findByProviderAndEnvironmentAndProviderSettlementId(
                command.provider(), command.environment(), command.providerSettlementId());
        if (existing.isPresent()) return existing.get().getId();

        Long settlementId = settlementRepository.nextIdentity();
        Settlement settlement = Settlement.create(settlementId, command.organizationId(), command.environment(),
                command.provider(), command.providerSettlementId(), command.providerSubaccountCode(),
                command.grossAmount(), command.netAmount(), command.providerFeeAmount(),
                command.anchorDepositAccountId(), command.transactionReferences(), command.providerSettledAt(),
                SettlementStatus.AWAITING_ANCHOR_CREDIT,
                "Settlement batch recorded for " + command.provider());

        settlement = settlementRepository.save(settlement);
        var evidence = evidenceRepository.findUnmatched(command.anchorDepositAccountId(), command.environment(),
                command.netAmount()).stream()
                .filter(item -> item.getOrganizationId().equals(command.organizationId())).toList();
        if (evidence.size() == 1) {
            settlement.confirm(evidence.getFirst().getAnchorTransferReference());
            settlementRepository.save(settlement);
            evidence.getFirst().match(settlementId);
            evidenceRepository.save(evidence.getFirst());
        } else if (evidence.size() > 1) {
            settlement.requireReconciliation("Multiple unmatched Anchor credits fit this settlement");
            settlementRepository.save(settlement);
        }
        return settlementId;
    }
}
