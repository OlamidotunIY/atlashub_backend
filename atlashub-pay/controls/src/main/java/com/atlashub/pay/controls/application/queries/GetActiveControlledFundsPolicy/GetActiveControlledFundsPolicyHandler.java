package com.atlashub.pay.controls.application.queries.GetActiveControlledFundsPolicy;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.exceptions.PolicyNotFoundException;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.pay.controls.domain.valueobject.PermittedCategory;
import com.atlashub.pay.controls.domain.valueobject.RestrictedCategory;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GetActiveControlledFundsPolicyHandler extends Query<GetActiveControlledFundsPolicyQuery, ControlledFundsPolicyResult> {

    private final ControlledFundsPolicyRepository policyRepository;

    public GetActiveControlledFundsPolicyHandler(ControlledFundsPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ControlledFundsPolicyResult execute(GetActiveControlledFundsPolicyQuery input) {
        ControlledFundsPolicy policy = policyRepository.findActiveByFacilityId(input.facilityId())
                .orElseThrow(() -> new PolicyNotFoundException("Active controlled funds policy not found for facility: " + input.facilityId()));

        Set<String> permitted = policy.getPermittedCategories().stream()
                .map(PermittedCategory::code)
                .collect(Collectors.toSet());
        Set<String> restricted = policy.getRestrictedCategories().stream()
                .map(RestrictedCategory::code)
                .collect(Collectors.toSet());

        return new ControlledFundsPolicyResult(
                policy.getId(),
                policy.getFacilityId(),
                policy.getOrganizationId(),
                policy.getEnvironment().name(),
                policy.getPolicyVersion(),
                policy.getMode().name(),
                permitted,
                restricted,
                policy.getStatus().name(),
                policy.getEffectiveFrom(),
                policy.getEffectiveUntil(),
                policy.getActivatedBy()
        );
    }
}
