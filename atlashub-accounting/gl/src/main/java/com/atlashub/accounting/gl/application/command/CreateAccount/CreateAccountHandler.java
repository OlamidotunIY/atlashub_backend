package com.atlashub.accounting.gl.application.command.CreateAccount;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.exceptions.DuplicateAccountCodeException;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class CreateAccountHandler extends Command<CreateAccountCommand, CreateAccountResult> {

    private final AccountRepository accountRepository;

    public CreateAccountHandler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:accounts:create')")
    public CreateAccountResult execute(CreateAccountCommand command) {
        if (accountRepository.findByOrganizationIdAndCode(command.organizationId(), command.code()).isPresent()) {
            throw new DuplicateAccountCodeException(command.code());
        }

        Long id = accountRepository.nextIdentity();
        Account account = Account.create(
                id,
                command.organizationId(),
                command.code(),
                command.name(),
                command.type(),
                command.parentAccountId(),
                command.isSystemAccount()
        );

        Account saved = accountRepository.save(account);

        return new CreateAccountResult(
                saved.getId(),
                saved.getOrganizationId(),
                saved.getCode(),
                saved.getName(),
                saved.getType(),
                saved.getParentAccountId(),
                saved.getIsSystemAccount(),
                saved.getIsActive(),
                saved.getCreatedAt()
        );
    }
}
