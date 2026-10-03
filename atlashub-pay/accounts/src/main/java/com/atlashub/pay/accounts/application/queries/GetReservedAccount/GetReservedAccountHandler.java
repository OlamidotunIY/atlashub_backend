package com.atlashub.pay.accounts.application.queries.GetReservedAccount;

import com.atlashub.pay.accounts.application.queries.AccountResults.ReservedAccountResult;
import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import com.atlashub.shared.application.security.ApiEnvironment;

@Component
public class GetReservedAccountHandler extends Query<GetReservedAccountQuery, ReservedAccountResult> {
    private final ReservedAccountRepository repository;

    public GetReservedAccountHandler(ReservedAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:accounts:read')")
    public ReservedAccountResult execute(GetReservedAccountQuery query) {
        return map(repository.findByOrganizationIdAndEnvironmentAndId(
                        query.organizationId(), ApiEnvironment.parse(query.environment()), query.reservedAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Reserved account not found")));
    }

    public static ReservedAccountResult map(ReservedAccount value) {
        return new ReservedAccountResult(value.getId(), value.getOwnerType().name(), value.getOwnerReferenceId(),
                value.getProvider(), value.getAccountName(), value.getMaskedAccountNumber(), value.getBankName(),
                value.getBankCode(), value.getCurrency().name(), value.getStatus().name(), value.getActivatedAt());
    }
}
