package com.atlashub.accounting.cash.application.command.StartBankReconciliation;

import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.entities.BankStatementLine;
import com.atlashub.accounting.cash.domain.repositories.BankReconciliationRepository;
import com.atlashub.shared.application.port.GlAccountQueryPort;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StartBankReconciliationHandler extends Command<StartBankReconciliationCommand, Long> {

    private final BankReconciliationRepository bankReconciliationRepository;
    private final GlAccountQueryPort glAccountQueryPort;

    public StartBankReconciliationHandler(BankReconciliationRepository bankReconciliationRepository,
                                          GlAccountQueryPort glAccountQueryPort) {
        this.bankReconciliationRepository = bankReconciliationRepository;
        this.glAccountQueryPort = glAccountQueryPort;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:accounts:create')")
    public Long execute(StartBankReconciliationCommand command) {
        Long reconciliationId = bankReconciliationRepository.nextIdentity();

        Money bookBalance = glAccountQueryPort.getAccountBalance(
                command.organizationId(),
                command.bankAccountCode(),
                command.statementDate()
        ).orElseGet(() -> Money.zero(command.statementClosingBalance().currency()));

        List<Long> bookItems = glAccountQueryPort.getJournalLineIds(
                command.organizationId(),
                command.bankAccountCode(),
                command.statementDate()
        );

        List<BankStatementLine> bankLines = new ArrayList<>();
        if (command.bankLines() != null) {
            for (BankStatementLineInput line : command.bankLines()) {
                Long lineId = bankReconciliationRepository.nextIdentity();
                bankLines.add(BankStatementLine.create(
                        lineId,
                        reconciliationId,
                        line.transactionDate(),
                        line.description(),
                        line.amount(),
                        line.type()
                ));
            }
        }

        BankReconciliation reconciliation = BankReconciliation.create(
                reconciliationId,
                command.organizationId(),
                command.bankAccountCode(),
                command.statementDate(),
                command.statementClosingBalance(),
                bookBalance,
                bankLines,
                bookItems
        );

        BankReconciliation saved = bankReconciliationRepository.save(reconciliation);
        return saved.getId();
    }
}
