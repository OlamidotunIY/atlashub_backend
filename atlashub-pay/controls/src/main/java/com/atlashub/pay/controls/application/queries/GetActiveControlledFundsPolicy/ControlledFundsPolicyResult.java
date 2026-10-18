package com.atlashub.pay.controls.application.queries.GetActiveControlledFundsPolicy;

import java.time.ZonedDateTime;
import java.util.Set;

public record ControlledFundsPolicyResult(
        Long id,
        Long facilityId,
        Long organizationId,
        String environment,
        String policyVersion,
        String mode,
        Set<String> permittedCategories,
        Set<String> restrictedCategories,
        String status,
        ZonedDateTime effectiveFrom,
        ZonedDateTime effectiveUntil,
        String activatedBy
) {}
