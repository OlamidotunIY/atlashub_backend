package com.atlashub.pay.charges.application.commands.RefundCharge;

import com.atlashub.shared.application.security.ApiEnvironment;

public record RefundChargeCommand(
        Long chargeId,
        Long organizationId,
        ApiEnvironment environment,
        String reason
) {
}
