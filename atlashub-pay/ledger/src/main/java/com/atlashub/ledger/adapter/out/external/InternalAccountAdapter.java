package com.atlashub.ledger.adapter.out.external;

import com.atlashub.ledger.application.port.out.InternalAccountPort;
import org.springframework.stereotype.Component;

@Component
public class InternalAccountAdapter implements InternalAccountPort {

    @Override
    public Long getAccountId(Long organizationId, String accountType) {
        // In a real modular monolith, this might use a direct service call if modules share a context,
        // or a REST call/internal event. For simplicity in the mock adapter, we derive a pseudo ID.
        // Usually, the ID is fetched via the GetInternalAccountQuery.
        // We'll just return a hash of orgId and type as the pseudo ID for demonstration.
        return (long) (organizationId.hashCode() + accountType.hashCode());
    }
}