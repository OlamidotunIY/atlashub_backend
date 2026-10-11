package com.atlashub.accounting.cash.application.command.ConfirmCashEvacuation;

import com.atlashub.accounting.cash.domain.entities.CashEvacuation;
import com.atlashub.accounting.cash.domain.exceptions.CashEvacuationNotFoundException;
import com.atlashub.accounting.cash.domain.repositories.CashEvacuationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class ConfirmCashEvacuationHandler extends Command<ConfirmCashEvacuationCommand, Void> {

    private final CashEvacuationRepository cashEvacuationRepository;

    public ConfirmCashEvacuationHandler(CashEvacuationRepository cashEvacuationRepository) {
        this.cashEvacuationRepository = cashEvacuationRepository;
    }

    @Override
    public Void execute(ConfirmCashEvacuationCommand command) {
        CashEvacuation evacuation = cashEvacuationRepository.findById(command.evacuationId())
                .filter(e -> e.getOrganizationId().equals(command.organizationId()))
                .orElseThrow(() -> new CashEvacuationNotFoundException(command.evacuationId()));

        evacuation.confirm();

        cashEvacuationRepository.save(evacuation);
        return null;
    }
}
