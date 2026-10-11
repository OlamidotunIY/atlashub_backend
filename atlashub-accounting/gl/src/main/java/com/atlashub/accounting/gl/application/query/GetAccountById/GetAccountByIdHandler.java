package com.atlashub.accounting.gl.application.query.GetAccountById;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.exceptions.AccountNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class GetAccountByIdHandler extends Query<GetAccountByIdQuery, AccountResult> {

    private final AccountRepository accountRepository;

    public GetAccountByIdHandler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:accounts:read')")
    public AccountResult execute(GetAccountByIdQuery query) {
        Account account = accountRepository.findById(query.accountId())
                .filter(a -> a.getOrganizationId().equals(query.organizationId()))
                .orElseThrow(() -> new AccountNotFoundException(query.accountId()));

        return new AccountResult(
                account.getId(),
                account.getOrganizationId(),
                account.getCode(),
                account.getName(),
                account.getType(),
                account.getParentAccountId(),
                account.getIsSystemAccount(),
                account.getIsActive(),
                account.getCreatedAt()
        );
    }
}
