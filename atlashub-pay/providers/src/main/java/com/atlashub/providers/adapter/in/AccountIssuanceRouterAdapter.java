package com.atlashub.providers.adapter.in;

import com.atlashub.accounts.application.port.AccountIssuanceRouterPort;
import com.atlashub.accounts.application.port.AccountIssuancePort;
import com.atlashub.providers.routing.PaymentCapability;
import com.atlashub.providers.routing.RoutingPolicy;
import com.atlashub.identity.application.port.OrganizationQueryService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AccountIssuanceRouterAdapter implements AccountIssuanceRouterPort {

    private final RoutingPolicy routingPolicy;
    private final ApplicationContext applicationContext;
    private final OrganizationQueryService organizationQueryService;

    public AccountIssuanceRouterAdapter(RoutingPolicy routingPolicy, ApplicationContext applicationContext, OrganizationQueryService organizationQueryService) {
        this.routingPolicy = routingPolicy;
        this.applicationContext = applicationContext;
        this.organizationQueryService = organizationQueryService;
    }

    @Override
    public AccountIssuancePort resolve(Long organizationId) {
        var profile = organizationQueryService.getOrganizationById(organizationId).orElseThrow();
        String beanName = routingPolicy.resolveProviderBeanName(PaymentCapability.ACCOUNT_ISSUANCE, profile.currency());
        return applicationContext.getBean(beanName, AccountIssuancePort.class);
    }
}
