package com.atlaspay.accounts.application.port;

import com.atlaspay.accounts.application.dto.AccountIssuanceRequestDto;
import com.atlaspay.shared.domain.valueobject.NUBAN;

public interface AccountIssuancePort {
    NUBAN issueVirtualAccount(AccountIssuanceRequestDto request);
}
