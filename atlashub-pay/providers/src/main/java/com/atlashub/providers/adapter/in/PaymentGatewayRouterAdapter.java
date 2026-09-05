package com.atlashub.providers.adapter.in;

import com.atlashub.charges.application.port.out.PaymentGatewayRouterPort;
import com.atlashub.charges.application.port.out.PaymentGatewayPort;
import com.atlashub.providers.routing.PaymentCapability;
import com.atlashub.providers.routing.RoutingPolicy;
import com.atlashub.identity.application.port.OrganizationQueryService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayRouterAdapter implements PaymentGatewayRouterPort {

    private final RoutingPolicy routingPolicy;
    private final ApplicationContext applicationContext;
    private final OrganizationQueryService organizationQueryService;

    public PaymentGatewayRouterAdapter(RoutingPolicy routingPolicy, ApplicationContext applicationContext, OrganizationQueryService organizationQueryService) {
        this.routingPolicy = routingPolicy;
        this.applicationContext = applicationContext;
        this.organizationQueryService = organizationQueryService;
    }

    @Override
    public PaymentGatewayPort resolve(Long organizationId) {
        var profile = organizationQueryService.getOrganizationById(organizationId).orElseThrow();
        String beanName = routingPolicy.resolveProviderBeanName(PaymentCapability.CHARGE, profile.currency());
        return applicationContext.getBean(beanName, PaymentGatewayPort.class);
    }
}
