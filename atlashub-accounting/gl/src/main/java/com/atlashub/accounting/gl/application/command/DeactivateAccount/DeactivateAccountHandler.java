package com.atlashub.accounting.gl.application.command.DeactivateAccount;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.exceptions.AccountNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class DeactivateAccountHandler extends Command<DeactivateAccountCommand, DeactivateAccountResult> {

    private final AccountRepository accountRepository;

    public DeactivateAccountHandler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:accounts:update')")
    public DeactivateAccountResult execute(DeactivateAccountCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .filter(a -> a.getOrganizationId().equals(command.organizationId()))
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.deactivate();
        Account saved = accountRepository.save(account);

        return new DeactivateAccountResult(saved.getId(), saved.getIsActive());
    }
}
