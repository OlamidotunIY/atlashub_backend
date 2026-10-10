package com.atlashub.pay.charges.presentation.dto;

import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;

public record RefundChargeResponse(
        Long chargeId,
        String reference,
        ChargeStatus status,
        String reason
) {
}
