package com.atlashub.shared.application.port;

import java.math.BigDecimal;

public interface PayoutGatewayPort {

    String resolveAccountName(String accountNumber, String bankCode);

    String initiateTransfer(String reference, BigDecimal amount, String currency,
                            String bankCode, String accountNumber, String narration);
}
