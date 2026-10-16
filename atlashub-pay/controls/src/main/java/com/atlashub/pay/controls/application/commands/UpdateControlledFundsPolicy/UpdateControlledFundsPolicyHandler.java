package com.atlashub.pay.controls.application.commands.UpdateControlledFundsPolicy;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.exceptions.PolicyNotFoundException;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateControlledFundsPolicyHandler extends Command<UpdateControlledFundsPolicyCommand, Void> {

    private final ControlledFundsPolicyRepository policyRepository;

    public UpdateControlledFundsPolicyHandler(ControlledFundsPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    @Transactional
    public Void execute(UpdateControlledFundsPolicyCommand input) {
        ControlledFundsPolicy policy = policyRepository.findActiveByFacilityId(input.facilityId())
                .orElseThrow(() -> new PolicyNotFoundException("Active policy not found for facility: " + input.facilityId()));

        policy.replaceRules(
                input.beneficiaryRules(),
                input.withdrawalRules(),
                input.amountAndFrequencyRules(),
                input.permittedCategories(),
                input.restrictedCategories()
        );

        policyRepository.save(policy);
        return null;
    }
}
