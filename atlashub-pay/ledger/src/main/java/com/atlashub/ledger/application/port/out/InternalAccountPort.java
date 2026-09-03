package com.atlashub.ledger.application.port.out;

import com.atlashub.ledger.domain.valueobject.InternalAccountType;

public interface InternalAccountPort {
    Long getAccountId(Long organizationId, InternalAccountType type);
    Long getPlatformAccountId(InternalAccountType type);
}
