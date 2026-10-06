package com.atlashub.pay.accounts.application.queries.ListReservedAccounts;

import com.atlashub.pay.accounts.application.queries.AccountResults.ReservedAccountResult;
import com.atlashub.pay.accounts.application.queries.GetReservedAccount.GetReservedAccountHandler;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import com.atlashub.shared.application.security.ApiEnvironment;

@Component
public class ListReservedAccountsHandler
        extends Query<ListReservedAccountsQuery, PageResult<ReservedAccountResult>> {
    private final ReservedAccountRepository repository;

    public ListReservedAccountsHandler(ReservedAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:accounts:read')")
    public PageResult<ReservedAccountResult> execute(ListReservedAccountsQuery query) {
        ReservedAccountOwnerType ownerType = query.ownerType() == null ? null
                : ReservedAccountOwnerType.valueOf(query.ownerType().toUpperCase());
        ExternalAccountStatus status = query.status() == null ? null
                : ExternalAccountStatus.valueOf(query.status().toUpperCase());
        Page<ReservedAccount> page = repository.search(
                query.organizationId(), ApiEnvironment.parse(query.environment()), ownerType, query.ownerReferenceId(), status,
                PageRequest.of(query.page(), query.size()));
        return new PageResult<>(page.getContent().stream().map(GetReservedAccountHandler::map).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
