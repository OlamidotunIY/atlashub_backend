package com.atlashub.pay.controls.domain.valueobject;

import java.util.Collections;
import java.util.Set;

public record BeneficiaryRuleSet(
        boolean allowAnyBeneficiary,
        Set<String> approvedBeneficiaries,
        Set<String> blockedBeneficiaries
) {
    public BeneficiaryRuleSet {
        approvedBeneficiaries = approvedBeneficiaries == null ? Collections.emptySet() : Collections.unmodifiableSet(approvedBeneficiaries);
        blockedBeneficiaries = blockedBeneficiaries == null ? Collections.emptySet() : Collections.unmodifiableSet(blockedBeneficiaries);
    }
}
