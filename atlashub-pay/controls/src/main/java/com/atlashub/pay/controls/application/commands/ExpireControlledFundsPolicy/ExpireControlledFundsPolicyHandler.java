package com.atlashub.pay.controls.application.commands.ExpireControlledFundsPolicy;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class ExpireControlledFundsPolicyHandler extends Command<ExpireControlledFundsPolicyCommand, Void> {

    private final ControlledFundsPolicyRepository policyRepository;

    public ExpireControlledFundsPolicyHandler(ControlledFundsPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    @Transactional
    public Void execute(ExpireControlledFundsPolicyCommand input) {
        if (input.facilityId() != null) {
            policyRepository.findActiveByFacilityId(input.facilityId())
                    .ifPresent(policy -> {
                        policy.expire();
                        policyRepository.save(policy);
                    });
        } else {
            List<ControlledFundsPolicy> all = policyRepository.findAll();
            ZonedDateTime now = ZonedDateTime.now();
            for (ControlledFundsPolicy policy : all) {
                if (policy.getEffectiveUntil() != null && now.isAfter(policy.getEffectiveUntil())) {
                    policy.expire();
                    policyRepository.save(policy);
                }
            }
        }
        return null;
    }
}
