package com.atlashub.pay.controls.presentation.dto;

import java.time.ZonedDateTime;
import java.util.Set;

public record ControlledFundsPolicyResponse(
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
