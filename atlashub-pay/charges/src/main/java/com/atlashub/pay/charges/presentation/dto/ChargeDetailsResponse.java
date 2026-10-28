package com.atlashub.pay.charges.presentation.dto;

import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.pay.charges.domain.valueobject.PaymentProvider;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ChargeDetailsResponse(
        Long id,
        String reference,
        BigDecimal amount,
        CurrencyCode currency,
        ChargeChannel channel,
        PaymentProvider provider,
        Long providerConnectionId,
        ChargeStatus status,
        String customerReferenceId,
        BigDecimal providerFee,
        String authorizationUrl,
        String accessCode,
        String failureMessage,
        String providerRefundReference,
        String refundReason,
        ZonedDateTime refundedAt,
        String disputeReference,
        String disputeStatus,
        String disputeReason,
        ZonedDateTime successfulAt,
        ZonedDateTime expiresAt,
        ZonedDateTime createdAt
) {
}
