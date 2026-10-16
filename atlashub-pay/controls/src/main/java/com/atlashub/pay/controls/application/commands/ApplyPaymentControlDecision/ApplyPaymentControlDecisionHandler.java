package com.atlashub.pay.controls.application.commands.ApplyPaymentControlDecision;

import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.pay.controls.domain.exceptions.DecisionNotFoundException;
import com.atlashub.pay.controls.domain.repositories.PaymentControlDecisionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApplyPaymentControlDecisionHandler extends Command<ApplyPaymentControlDecisionCommand, Void> {

    private final PaymentControlDecisionRepository decisionRepository;

    public ApplyPaymentControlDecisionHandler(PaymentControlDecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    @Override
    @Transactional
    public Void execute(ApplyPaymentControlDecisionCommand input) {
        PaymentControlDecision decision = decisionRepository.findById(input.decisionId())
                .orElseThrow(() -> new DecisionNotFoundException("Decision not found: " + input.decisionId()));

        decision.confirmApplied(input.providerReference());
        decisionRepository.save(decision);
        return null;
    }
}
