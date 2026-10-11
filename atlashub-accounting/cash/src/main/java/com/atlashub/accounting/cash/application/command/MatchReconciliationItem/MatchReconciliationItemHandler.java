package com.atlashub.accounting.cash.application.command.MatchReconciliationItem;

import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.exceptions.BankReconciliationNotFoundException;
import com.atlashub.accounting.cash.domain.repositories.BankReconciliationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class MatchReconciliationItemHandler extends Command<MatchReconciliationItemCommand, Void> {

    private final BankReconciliationRepository bankReconciliationRepository;

    public MatchReconciliationItemHandler(BankReconciliationRepository bankReconciliationRepository) {
        this.bankReconciliationRepository = bankReconciliationRepository;
    }

    @Override
    public Void execute(MatchReconciliationItemCommand command) {
        BankReconciliation reconciliation = bankReconciliationRepository.findById(command.reconciliationId())
                .filter(recon -> recon.getOrganizationId().equals(command.organizationId()))
                .orElseThrow(() -> new BankReconciliationNotFoundException(command.reconciliationId()));

        Long itemId = bankReconciliationRepository.nextIdentity();
        reconciliation.matchItem(itemId, command.bankLineId(), command.journalLineId());

        bankReconciliationRepository.save(reconciliation);
        return null;
    }
}
