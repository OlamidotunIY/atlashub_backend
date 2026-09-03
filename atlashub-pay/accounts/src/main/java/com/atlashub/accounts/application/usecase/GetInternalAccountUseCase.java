package com.atlashub.accounts.application.usecase;

import com.atlashub.accounts.application.query.GetInternalAccountQuery;
import com.atlashub.accounts.application.result.InternalAccountDto;
import com.atlashub.accounts.domain.model.InternalAccount;
import com.atlashub.accounts.domain.repository.InternalAccountDomainRepository;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import org.springframework.stereotype.Service;

@Service
public class GetInternalAccountUseCase extends BaseUseCase<GetInternalAccountQuery, InternalAccountDto> {

    private final InternalAccountDomainRepository repository;

    public GetInternalAccountUseCase(InternalAccountDomainRepository repository) {
        this.repository = repository;
    }

    @Override
    public InternalAccountDto execute(GetInternalAccountQuery query) {
        InternalAccount account = repository.findByOrganizationIdAndType(query.organizationId(), query.accountType())
                .orElseThrow(() -> new NotFoundException(SharedErrorCode.INVALID_ID, "Internal account not found for type " + query.accountType()));

        return new InternalAccountDto(
                account.getId(),
                account.getOrganizationId(),
                account.getType(),
                account.getCurrency(),
                account.getStatus()
        );
    }
}