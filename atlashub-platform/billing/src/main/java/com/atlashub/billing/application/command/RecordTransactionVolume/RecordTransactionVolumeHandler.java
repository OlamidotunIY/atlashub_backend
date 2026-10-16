package com.atlashub.billing.application.command.RecordTransactionVolume;

import com.atlashub.billing.domain.entities.TransactionVolumeLedger;
import com.atlashub.billing.domain.repositories.TransactionVolumeLedgerRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RecordTransactionVolumeHandler extends Command<RecordTransactionVolumeCommand, RecordTransactionVolumeResult> {

    private final TransactionVolumeLedgerRepository ledgerRepository;

    public RecordTransactionVolumeHandler(TransactionVolumeLedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @Override
    public RecordTransactionVolumeResult execute(RecordTransactionVolumeCommand command) {
        Optional<TransactionVolumeLedger> existing = ledgerRepository.findByOrganizationIdAndMonth(
                command.organizationId(), command.month()
        );

        TransactionVolumeLedger ledger;
        if (existing.isPresent()) {
            ledger = existing.get();
        } else {
            Long id = ledgerRepository.nextIdentity();
            ledger = TransactionVolumeLedger.create(
                    id,
                    command.organizationId(),
                    command.month(),
                    command.currency() != null ? command.currency() : "NGN"
            );
        }

        ledger.recordTransaction(command.amount());
        ledgerRepository.save(ledger);

        return new RecordTransactionVolumeResult(
                ledger.getId(),
                ledger.getTotalVolume(),
                ledger.getTotalTransactionCount()
        );
    }
}
