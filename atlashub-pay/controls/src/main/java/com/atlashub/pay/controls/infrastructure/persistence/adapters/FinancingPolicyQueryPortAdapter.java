package com.atlashub.pay.controls.infrastructure.persistence.adapters;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.pay.controls.domain.valueobject.PermittedCategory;
import com.atlashub.pay.controls.domain.valueobject.RestrictedCategory;
import com.atlashub.shared.application.port.FinancingPolicyQueryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FinancingPolicyQueryPortAdapter implements FinancingPolicyQueryPort {

    private final ControlledFundsPolicyRepository policyRepository;

    public FinancingPolicyQueryPortAdapter(ControlledFundsPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    public Optional<FinancingPolicyDto> findActivePolicyByFacilityId(Long facilityId) {
        return policyRepository.findActiveByFacilityId(facilityId)
                .map(this::toDto);
    }

    private FinancingPolicyDto toDto(ControlledFundsPolicy policy) {
        Set<String> permitted = policy.getPermittedCategories().stream()
                .map(PermittedCategory::code)
                .collect(Collectors.toSet());
        Set<String> restricted = policy.getRestrictedCategories().stream()
                .map(RestrictedCategory::code)
                .collect(Collectors.toSet());

        return new FinancingPolicyDto(
                policy.getFacilityId(),
                policy.getOrganizationId(),
                policy.getPolicyVersion(),
                policy.getMode().name(),
                permitted,
                restricted,
                policy.getStatus().name(),
                policy.getEffectiveFrom(),
                policy.getEffectiveUntil()
        );
    }
}
