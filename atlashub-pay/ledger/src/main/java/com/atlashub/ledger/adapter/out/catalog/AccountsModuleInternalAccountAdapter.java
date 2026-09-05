package com.atlashub.ledger.adapter.out.catalog;

import com.atlashub.ledger.application.port.out.InternalAccountPort;
import com.atlashub.ledger.domain.valueobject.InternalAccountType;
import com.atlashub.accounts.application.port.AccountQueryService;
import org.springframework.stereotype.Component;

@Component
public class AccountsModuleInternalAccountAdapter implements InternalAccountPort {

    private final AccountQueryService AccountQueryService;

    public AccountsModuleInternalAccountAdapter(AccountQueryService AccountQueryService) {
        this.AccountQueryService = AccountQueryService;
    }

    @Override
    public Long getAccountId(Long organizationId, InternalAccountType type) {
        return AccountQueryService.getAccountId(organizationId, type.name());
    }

    @Override
    public Long getPlatformAccountId(InternalAccountType type) {
        return AccountQueryService.getPlatformAccountId(type.name());
    }
}
