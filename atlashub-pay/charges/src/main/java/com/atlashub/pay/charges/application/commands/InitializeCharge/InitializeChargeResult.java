package com.atlashub.pay.charges.application.commands.InitializeCharge;

import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record InitializeChargeResult(
        Long chargeId,
        Long organizationId,
        ApiEnvironment environment,
        String reference,
        Money amount,
        ChargeChannel channel,
        ChargeStatus status,
        String providerReference,
        String authorizationUrl,
        String accessCode,
        ZonedDateTime expiresAt
) {
}
