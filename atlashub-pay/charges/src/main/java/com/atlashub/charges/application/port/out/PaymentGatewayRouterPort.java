package com.atlashub.charges.application.port.out;

import com.atlashub.charges.application.port.out.PaymentGatewayPort;

public interface PaymentGatewayRouterPort {
    PaymentGatewayPort resolve(Long organizationId);
}
