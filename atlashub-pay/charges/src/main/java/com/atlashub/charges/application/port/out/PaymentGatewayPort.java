package com.atlashub.charges.application.port.out;

import java.math.BigDecimal;

public interface PaymentGatewayPort {
    String initializeCharge(
            BigDecimal amount,
            String currency,
            String email,
            String reference,
            String metadata,
            String redirectUrl
    );
}
