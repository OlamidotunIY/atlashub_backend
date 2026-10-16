package com.atlashub.pay.transfers.infrastructure.services;

import com.atlashub.shared.application.port.PayoutGatewayPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component("moniepointPayoutAdapter")
public class MoniepointPayoutAdapter implements PayoutGatewayPort {

    @Override
    public String resolveAccountName(String accountNumber, String bankCode) {
        if (accountNumber == null || accountNumber.isBlank() || bankCode == null || bankCode.isBlank()) {
            return null;
        }
        return "MONIEPOINT RESOLVED " + accountNumber;
    }

    @Override
    public String initiateTransfer(String reference, BigDecimal amount, String currency,
                                    String bankCode, String accountNumber, String narration) {
        return "MNP_TRF_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
