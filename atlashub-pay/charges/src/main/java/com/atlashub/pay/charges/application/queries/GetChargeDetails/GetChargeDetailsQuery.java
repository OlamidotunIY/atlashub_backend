package com.atlashub.pay.charges.application.queries.GetChargeDetails;

import com.atlashub.shared.application.security.ApiEnvironment;

public record GetChargeDetailsQuery(
        Long chargeId,
        Long organizationId,
        ApiEnvironment environment
) {
}
