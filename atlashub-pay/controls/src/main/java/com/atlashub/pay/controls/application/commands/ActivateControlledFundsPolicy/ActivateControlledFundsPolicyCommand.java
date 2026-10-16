package com.atlashub.pay.controls.application.commands.ActivateControlledFundsPolicy;

import com.atlashub.pay.controls.domain.valueobject.AmountFrequencyRuleSet;
import com.atlashub.pay.controls.domain.valueobject.BeneficiaryRuleSet;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import com.atlashub.pay.controls.domain.valueobject.PermittedCategory;
import com.atlashub.pay.controls.domain.valueobject.ProviderControlCapability;
import com.atlashub.pay.controls.domain.valueobject.RestrictedCategory;
import com.atlashub.pay.controls.domain.valueobject.WithdrawalRuleSet;

import java.time.ZonedDateTime;
import java.util.Set;

public record ActivateControlledFundsPolicyCommand(
        Long facilityId,
        Long organizationId,
        Environment environment,
        String policyVersion,
        ControlMode mode,
        Set<PermittedCategory> permittedCategories,
        Set<RestrictedCategory> restrictedCategories,
        BeneficiaryRuleSet beneficiaryRules,
        WithdrawalRuleSet withdrawalRules,
        AmountFrequencyRuleSet amountAndFrequencyRules,
        Set<ProviderControlCapability> providerCapabilities,
        ZonedDateTime effectiveFrom,
        ZonedDateTime effectiveUntil,
        String activatedBy
) {}
