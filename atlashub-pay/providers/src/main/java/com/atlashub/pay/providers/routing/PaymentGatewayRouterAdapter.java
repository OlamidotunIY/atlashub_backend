package com.atlashub.pay.providers.routing;

import com.atlashub.charges.application.port.out.PaymentGatewayPort;
import com.atlashub.charges.application.port.out.PaymentGatewayRouterPort;
import com.atlashub.shared.application.port.out.OrganizationContextQueryPort;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PaymentGatewayRouterAdapter implements PaymentGatewayRouterPort {

    private final RoutingPolicy routingPolicy;
    private final Map<String, PaymentGatewayPort> gateways;
    private final OrganizationContextQueryPort orgQueryPort;

    public PaymentGatewayRouterAdapter(RoutingPolicy routingPolicy, Map<String, PaymentGatewayPort> gateways, OrganizationContextQueryPort orgQueryPort) {
        this.routingPolicy = routingPolicy;
        this.gateways = gateways;
        this.orgQueryPort = orgQueryPort;
    }

    @Override
    public PaymentGatewayPort resolve(Long organizationId) {
        var context = orgQueryPort.getOrganizationContext(organizationId);
        // Fallback to "NG" if country is not set for now
        String country = context.country() != null ? context.country() : "NG";
        String providerId = routingPolicy.getProviderId("CHARGE", country);
        PaymentGatewayPort provider = gateways.get(providerId);
        if (provider == null) {
             throw new IllegalStateException("Provider bean not found: " + providerId);
        }
        return provider;
    }
}
