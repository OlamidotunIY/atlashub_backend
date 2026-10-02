package com.atlashub.pay.ledger.application.commands.CloseAccount;

import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotFoundException;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.services.BalanceCalculator;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class CloseAccountHandler extends Command<CloseAccountCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(CloseAccountHandler.class);
    
    private final LedgerAccountRepository ledgerAccountRepository;
    private final BalanceSnapshotRepository balanceSnapshotRepository;
    private final LedgerTransactionRepository transactionRepository;
    private final BalanceCalculator balanceCalculator;

    public CloseAccountHandler(LedgerAccountRepository ledgerAccountRepository,
                               BalanceSnapshotRepository balanceSnapshotRepository,
                               LedgerTransactionRepository transactionRepository,
                               BalanceCalculator balanceCalculator) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.transactionRepository = transactionRepository;
        this.balanceCalculator = balanceCalculator;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:ledger:close')")
    public Void execute(CloseAccountCommand command) {
        log.info("Executing CloseAccountCommand for account id: {}", command.ledgerAccountId());

        LedgerAccount account = ledgerAccountRepository.findByIdWithLock(command.ledgerAccountId())
                .orElseThrow(() -> new LedgerAccountNotFoundException(command.ledgerAccountId().toString()));

        if (!account.getOrganizationId().equals(command.organizationId())) {
            throw new IllegalArgumentException("Account does not belong to the active organization");
        }

        BalanceSnapshot snapshot = balanceSnapshotRepository.findLatestByAccountId(account.getId())
                .orElseThrow(() -> new IllegalStateException("Account missing initial balance snapshot"));
        Money balance = new Money(
                balanceCalculator.calculateRunningBalance(
                        account.getId(), account.getNormalBalance(), snapshot.getBalance().amount(),
                        transactionRepository.findByAccountIdAndPostedAtAfter(
                                account.getId(), snapshot.getSnapshotAt())),
                account.getCurrency());

        account.close(balance);
        ledgerAccountRepository.save(account);

        return null;
    }
}
