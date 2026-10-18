package com.atlashub.pay.transfers.infrastructure.services;

import com.atlashub.shared.application.port.PayoutGatewayPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component("paystackPayoutAdapter")
@Primary
public class PaystackPayoutAdapter implements PayoutGatewayPort {

    @Override
    public String resolveAccountName(String accountNumber, String bankCode) {
        if (accountNumber == null || accountNumber.isBlank() || bankCode == null || bankCode.isBlank()) {
            return null;
        }
        return "VERIFIED ACCOUNT " + accountNumber;
    }

    @Override
    public String initiateTransfer(String reference, BigDecimal amount, String currency,
                                    String bankCode, String accountNumber, String narration) {
        return "TRF_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
