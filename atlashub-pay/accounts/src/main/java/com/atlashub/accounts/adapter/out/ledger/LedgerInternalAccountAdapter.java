package com.atlashub.accounts.adapter.out.ledger;

import com.atlashub.accounts.domain.repository.InternalAccountDomainRepository;
import com.atlashub.ledger.application.port.out.InternalAccountPort;
import com.atlashub.ledger.domain.valueobject.InternalAccountType;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import org.springframework.stereotype.Component;

@Component
public class LedgerInternalAccountAdapter implements InternalAccountPort {

    private final InternalAccountDomainRepository accountRepository;

    public LedgerInternalAccountAdapter(InternalAccountDomainRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Long getAccountId(Long organizationId, InternalAccountType type) {
        com.atlashub.accounts.domain.valueobject.InternalAccountType localType =
                com.atlashub.accounts.domain.valueobject.InternalAccountType.valueOf(type.name());
        
        return accountRepository.findByOrganizationIdAndType(organizationId, localType)
                .orElseThrow(() -> new NotFoundException(SharedErrorCode.INVALID_ID, "Account not found for org " + organizationId))
                .getId();
    }

    @Override
    public Long getPlatformAccountId(InternalAccountType type) {
        // Assuming orgId = 1 for Platform (or pass null if global)
        // Adjust according to how Platform accounts are stored. For now, assuming orgId 1.
        com.atlashub.accounts.domain.valueobject.InternalAccountType localType =
                com.atlashub.accounts.domain.valueobject.InternalAccountType.valueOf(type.name());
        
        return accountRepository.findByOrganizationIdAndType(1L, localType)
                .orElseThrow(() -> new NotFoundException(SharedErrorCode.INVALID_ID, "Platform account not found"))
                .getId();
    }
}
