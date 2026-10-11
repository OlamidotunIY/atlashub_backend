package com.atlashub.accounting.cash.application.command.FinalizeReconciliation;

import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.exceptions.BankReconciliationNotFoundException;
import com.atlashub.accounting.cash.domain.repositories.BankReconciliationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class FinalizeReconciliationHandler extends Command<FinalizeReconciliationCommand, Void> {

    private final BankReconciliationRepository bankReconciliationRepository;

    public FinalizeReconciliationHandler(BankReconciliationRepository bankReconciliationRepository) {
        this.bankReconciliationRepository = bankReconciliationRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:accounts:create')")
    public Void execute(FinalizeReconciliationCommand command) {
        BankReconciliation reconciliation = bankReconciliationRepository.findById(command.reconciliationId())
                .filter(recon -> recon.getOrganizationId().equals(command.organizationId()))
                .orElseThrow(() -> new BankReconciliationNotFoundException(command.reconciliationId()));

        reconciliation.finalizeReconciliation(command.forceFinalize());

        bankReconciliationRepository.save(reconciliation);
        return null;
    }
}
