package com.atlashub.accounts.application.port;

import com.atlashub.accounts.application.dto.AccountIssuanceRequestDto;
import com.atlashub.shared.domain.valueobject.NUBAN;

public interface AccountIssuancePort {
    NUBAN issueVirtualAccount(AccountIssuanceRequestDto request);
}
