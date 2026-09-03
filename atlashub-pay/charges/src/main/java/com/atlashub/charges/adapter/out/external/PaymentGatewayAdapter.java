package com.atlashub.charges.adapter.out.external;

import com.atlashub.charges.application.port.out.PaymentGatewayPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class PaymentGatewayAdapter implements PaymentGatewayPort {

    @Override
    public String initializeCharge(BigDecimal amount, String currency, String email, String reference, String purpose, String metadata, String redirectUrl) {
        log.info("Initializing Paystack charge for amount {} {} reference {}", amount, currency, reference);
        // Normally calls Paystack API and returns authorization_url
        return "https://checkout.paystack.com/" + reference;
    }
}