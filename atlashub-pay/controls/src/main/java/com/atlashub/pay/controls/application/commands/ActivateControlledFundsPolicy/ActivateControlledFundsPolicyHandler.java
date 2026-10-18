package com.atlashub.pay.controls.application.commands.ActivateControlledFundsPolicy;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ActivateControlledFundsPolicyHandler extends Command<ActivateControlledFundsPolicyCommand, Void> {

    private final ControlledFundsPolicyRepository policyRepository;

    public ActivateControlledFundsPolicyHandler(ControlledFundsPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    @Transactional
    public Void execute(ActivateControlledFundsPolicyCommand input) {
        Long id = policyRepository.nextIdentity();
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                id,
                input.facilityId(),
                input.organizationId(),
                input.environment(),
                input.policyVersion(),
                input.mode(),
                input.permittedCategories(),
                input.restrictedCategories(),
                input.beneficiaryRules(),
                input.withdrawalRules(),
                input.amountAndFrequencyRules(),
                input.providerCapabilities(),
                input.effectiveFrom(),
                input.effectiveUntil()
        );

        policy.activate(input.activatedBy());
        policyRepository.save(policy);
        return null;
    }
}
