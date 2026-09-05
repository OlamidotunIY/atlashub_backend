package com.atlashub.pay.providers.routing;

import org.springframework.stereotype.Component;

@Component
public class RoutingPolicy {

    public String getProviderId(String capability, String country) {
        if ("CHARGE".equals(capability)) {
            if ("NG".equalsIgnoreCase(country)) {
                return "paystackGatewayAdapter";
            }
            return "stripeGatewayAdapter";
        }
        
        if ("ACCOUNT_ISSUANCE".equals(capability)) {
            if ("NG".equalsIgnoreCase(country)) {
                return "anchorVirtualAccountAdapter";
            }
            return "stripeVirtualAccountAdapter";
        }
        
        throw new IllegalArgumentException("No provider mapped for capability: " + capability + " in country: " + country);
    }
}
