package com.atlashub.shared.application.port;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

public interface FinancingPolicyQueryPort {

    record FinancingPolicyDto(
            Long facilityId,
            Long organizationId,
            String policyVersion,
            String mode,
            Set<String> permittedCategories,
            Set<String> restrictedCategories,
            String status,
            ZonedDateTime effectiveFrom,
            ZonedDateTime effectiveUntil
    ) {}

    Optional<FinancingPolicyDto> findActivePolicyByFacilityId(Long facilityId);
}
