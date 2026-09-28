package com.atlashub.pay.ledger.application.commands.CloseAccount;

import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotFoundException;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
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

    public CloseAccountHandler(LedgerAccountRepository ledgerAccountRepository, BalanceSnapshotRepository balanceSnapshotRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.balanceSnapshotRepository = balanceSnapshotRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:ledger:close')")
    public Void execute(CloseAccountCommand command) {
        log.info("Executing CloseAccountCommand for account id: {}", command.ledgerAccountId());

        LedgerAccount account = ledgerAccountRepository.findByIdWithLock(command.ledgerAccountId())
                .orElseThrow(() -> new LedgerAccountNotFoundException(command.ledgerAccountId().toString()));

        Money balance = balanceSnapshotRepository.findLatestByAccountId(account.getId())
                .map(BalanceSnapshot::getBalance)
                .orElseGet(() -> Money.zero(account.getCurrency()));

        account.close(balance);
        ledgerAccountRepository.save(account);

        return null;
    }
}
