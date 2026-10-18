package com.atlashub.pay.controls.application.commands.UpdateControlledFundsPolicy;

import com.atlashub.pay.controls.domain.valueobject.AmountFrequencyRuleSet;
import com.atlashub.pay.controls.domain.valueobject.BeneficiaryRuleSet;
import com.atlashub.pay.controls.domain.valueobject.PermittedCategory;
import com.atlashub.pay.controls.domain.valueobject.RestrictedCategory;
import com.atlashub.pay.controls.domain.valueobject.WithdrawalRuleSet;

import java.util.Set;

public record UpdateControlledFundsPolicyCommand(
        Long facilityId,
        BeneficiaryRuleSet beneficiaryRules,
        WithdrawalRuleSet withdrawalRules,
        AmountFrequencyRuleSet amountAndFrequencyRules,
        Set<PermittedCategory> permittedCategories,
        Set<RestrictedCategory> restrictedCategories
) {}
