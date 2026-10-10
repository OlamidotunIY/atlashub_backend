package com.atlashub.pay.charges.application.queries.GetChargeDetails;

import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.pay.charges.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record ChargeResult(
        Long id,
        Long organizationId,
        ApiEnvironment environment,
        String reference,
        Money amount,
        ChargeChannel channel,
        PaymentProvider provider,
        Long providerProfileId,
        String providerReference,
        String sourceSystem,
        String sourceReferenceId,
        String customerReferenceId,
        Money providerFee,
        ChargeStatus status,
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
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
}
