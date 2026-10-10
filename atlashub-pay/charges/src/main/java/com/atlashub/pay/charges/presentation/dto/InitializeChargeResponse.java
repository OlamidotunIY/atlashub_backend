package com.atlashub.pay.charges.presentation.dto;

import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record InitializeChargeResponse(
        Long chargeId,
        String reference,
        BigDecimal amount,
        CurrencyCode currency,
        ChargeChannel channel,
        ChargeStatus status,
        String authorizationUrl,
        String accessCode,
        ZonedDateTime expiresAt
) {
}
