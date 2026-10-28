package com.atlashub.integrations.infrastructure.persistence.adapters;

import com.atlashub.integrations.application.query.ListEligibleAuthorizations.ListEligibleAuthorizationsHandler;
import com.atlashub.integrations.application.query.ListEligibleAuthorizations.ListEligibleAuthorizationsQuery;
import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.shared.application.port.ProviderConnectionQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProviderConnectionQueryPortAdapter implements ProviderConnectionQueryPort {
    private final ListEligibleAuthorizationsHandler queryHandler;

    public ProviderConnectionQueryPortAdapter(ListEligibleAuthorizationsHandler queryHandler) {
        this.queryHandler = queryHandler;
    }

    @Override
    public List<ProviderConnection> findEligible(Long organizationId, ApiEnvironment environment,
                                                 String currency, String capability) {
        Capability required = Capability.valueOf(capability);
        return queryHandler.execute(new ListEligibleAuthorizationsQuery(
                        organizationId, environment, currency, required)).stream()
                .map(value -> new ProviderConnection(value.authorization().id(), value.connection().provider().name(),
                        value.authorization().externalMerchantId(), value.authorization().activeCapabilities().stream()
                        .map(Enum::name).collect(Collectors.toUnmodifiableSet())))
                .toList();
    }
}
