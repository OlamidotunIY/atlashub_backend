package com.atlashub.accounting.gl.application.query.ListAccounts;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListAccountsHandler extends Query<ListAccountsQuery, List<AccountSummaryResult>> {

    private final AccountRepository accountRepository;

    public ListAccountsHandler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:accounts:read')")
    public List<AccountSummaryResult> execute(ListAccountsQuery query) {
        return accountRepository.findByOrganizationId(query.organizationId()).stream()
                .map(this::toResult)
                .toList();
    }

    private AccountSummaryResult toResult(Account account) {
        return new AccountSummaryResult(
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
