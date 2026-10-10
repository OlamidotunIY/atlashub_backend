package com.atlashub.pay.charges.application.commands.RefundCharge;

import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;

public record RefundChargeResult(
        Long chargeId,
        String reference,
        ChargeStatus status,
        String reason
) {
}
