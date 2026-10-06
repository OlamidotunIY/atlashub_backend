package com.atlashub.pay.ledger.application.commands.UnfreezeAccount;

import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotFoundException;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.valueobject.LedgerRestrictionType;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UnfreezeAccountHandler extends Command<UnfreezeAccountCommand, Void> {
    private final LedgerAccountRepository repository;

    public UnfreezeAccountHandler(LedgerAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('platform:pay:operations')")
    public Void execute(UnfreezeAccountCommand command) {
        LedgerAccount account = repository.findByIdWithLock(command.ledgerAccountId())
                .orElseThrow(() -> new LedgerAccountNotFoundException(command.ledgerAccountId().toString()));
        account.unfreeze(LedgerRestrictionType.valueOf(command.restrictionType().toUpperCase()));
        repository.save(account);
        return null;
    }
}
