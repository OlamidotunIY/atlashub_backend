package com.atlashub.pay.ledger.application.commands.FreezeAccount;

import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotFoundException;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.valueobject.LedgerRestrictionType;
import com.atlashub.shared.application.usecase.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class FreezeAccountHandler extends Command<FreezeAccountCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(FreezeAccountHandler.class);
    
    private final LedgerAccountRepository ledgerAccountRepository;

    public FreezeAccountHandler(LedgerAccountRepository ledgerAccountRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:ledger:freeze')")
    public Void execute(FreezeAccountCommand command) {
        log.info("Executing FreezeAccountCommand for account id: {}", command.ledgerAccountId());

        LedgerAccount account = ledgerAccountRepository.findByIdWithLock(command.ledgerAccountId())
                .orElseThrow(() -> new LedgerAccountNotFoundException(command.ledgerAccountId().toString()));

        if (!account.getOrganizationId().equals(command.organizationId())) {
            throw new IllegalArgumentException("Account does not belong to the active organization");
        }

        account.freeze(LedgerRestrictionType.valueOf(command.restrictionType().toUpperCase()));
        ledgerAccountRepository.save(account);

        return null;
    }
}
