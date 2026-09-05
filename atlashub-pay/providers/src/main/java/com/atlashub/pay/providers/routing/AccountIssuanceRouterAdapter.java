package com.atlashub.pay.providers.routing;

import com.atlashub.accounts.application.port.AccountIssuancePort;
import com.atlashub.accounts.application.port.AccountIssuanceRouterPort;
import com.atlashub.identity.application.port.OrganizationQueryService;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AccountIssuanceRouterAdapter implements AccountIssuanceRouterPort {

    private final RoutingPolicy routingPolicy;
    private final Map<String, AccountIssuancePort> providers;
    private final OrganizationQueryService orgQueryPort;

    public AccountIssuanceRouterAdapter(RoutingPolicy routingPolicy, Map<String, AccountIssuancePort> providers, OrganizationQueryService orgQueryPort) {
        this.routingPolicy = routingPolicy;
        this.providers = providers;
        this.orgQueryPort = orgQueryPort;
    }

    @Override
    public AccountIssuancePort resolve(Long organizationId) {
        var context = orgQueryPort.getOrganizationById(organizationId);
        String country = context.country() != null ? context.country() : "NG";
        String providerId = routingPolicy.getProviderId("ACCOUNT_ISSUANCE", country);
        AccountIssuancePort provider = providers.get(providerId);
        if (provider == null) {
             throw new IllegalStateException("Provider bean not found: " + providerId);
        }
        return provider;
    }
}
