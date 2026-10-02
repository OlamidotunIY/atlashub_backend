package com.atlashub.pay.accounts.application.commands.ChangeReservedAccountStatus;

import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.pay.accounts.domain.valueobject.BankingRestrictionType;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.security.access.prepost.PreAuthorize;

@Component
public class ChangeReservedAccountStatusHandler extends Command<ChangeReservedAccountStatusCommand, Void> {
    private final ReservedAccountRepository repository;

    public ChangeReservedAccountStatusHandler(ReservedAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @PreAuthorize("(#command.action().name() == 'SUSPEND' and hasAuthority('pay:accounts:suspend'))"
            + " or (#command.action().name() == 'REACTIVATE' and hasAuthority('pay:accounts:reactivate'))"
            + " or (#command.action().name() == 'CLOSE' and hasAuthority('pay:accounts:close'))")
    public Void execute(ChangeReservedAccountStatusCommand command) {
        ReservedAccount account = repository.findByOrganizationIdAndId(
                        command.organizationId(), command.reservedAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Reserved account not found"));
        switch (command.action()) {
            case SUSPEND -> account.restrict(BankingRestrictionType.MANUAL);
            case REACTIVATE -> account.removeRestriction(BankingRestrictionType.MANUAL);
            case CLOSE -> account.close();
        }
        repository.save(account);
        return null;
    }
}
