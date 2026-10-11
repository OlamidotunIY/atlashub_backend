package com.atlashub.accounting.cash.application.command.RecordCashEvacuation;

import com.atlashub.accounting.cash.domain.entities.CashEvacuation;
import com.atlashub.accounting.cash.domain.repositories.CashEvacuationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class RecordCashEvacuationHandler extends Command<RecordCashEvacuationCommand, Long> {

    private final CashEvacuationRepository cashEvacuationRepository;

    public RecordCashEvacuationHandler(CashEvacuationRepository cashEvacuationRepository) {
        this.cashEvacuationRepository = cashEvacuationRepository;
    }

    @Override
    public Long execute(RecordCashEvacuationCommand command) {
        Long evacuationId = cashEvacuationRepository.nextIdentity();

        CashEvacuation evacuation = CashEvacuation.create(
                evacuationId,
                command.organizationId(),
                command.outletId(),
                command.tillId(),
                command.amount(),
                command.evacuatedBy()
        );

        CashEvacuation saved = cashEvacuationRepository.save(evacuation);
        return saved.getId();
    }
}
