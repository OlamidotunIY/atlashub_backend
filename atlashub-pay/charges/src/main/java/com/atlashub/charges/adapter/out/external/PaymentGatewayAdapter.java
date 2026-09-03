package com.atlashub.charges.adapter.out.external;

import com.atlashub.charges.application.port.out.PaymentGatewayPort;
import com.atlashub.charges.domain.valueobject.ChargePurpose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentGatewayAdapter implements PaymentGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayAdapter.class);

    private final String paystackSecretKey;

    public PaymentGatewayAdapter() {
        this.paystackSecretKey = "dummy_secret_key";
    }

    @Override
    public String initializeCharge(BigDecimal amount, String currency, String email, String reference, String metadata, String redirectUrl) {
        return "";
    }
}