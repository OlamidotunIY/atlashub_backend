package com.atlashub.pay.providers.paystack;

import com.atlashub.charges.application.port.out.PaymentGatewayPort;
import com.atlashub.charges.domain.valueobject.ChargePurpose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("paystackGatewayAdapter")
public class PaystackGatewayAdapter implements PaymentGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(PaystackGatewayAdapter.class);

    private final String paystackSecretKey;

    public PaystackGatewayAdapter() {
        this.paystackSecretKey = "dummy_secret_key";
    }

    @Override
    public String initializeCharge(BigDecimal amount, String currency, String email, String reference, String metadata, String redirectUrl) {
        return "";
    }
}
