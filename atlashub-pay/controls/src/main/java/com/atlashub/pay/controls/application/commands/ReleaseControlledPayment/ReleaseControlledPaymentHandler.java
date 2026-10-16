package com.atlashub.pay.controls.application.commands.ReleaseControlledPayment;

import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.pay.controls.domain.exceptions.DecisionNotFoundException;
import com.atlashub.pay.controls.domain.repositories.PaymentControlDecisionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReleaseControlledPaymentHandler extends Command<ReleaseControlledPaymentCommand, Void> {

    private final PaymentControlDecisionRepository decisionRepository;

    public ReleaseControlledPaymentHandler(PaymentControlDecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    @Override
    @Transactional
    public Void execute(ReleaseControlledPaymentCommand input) {
        PaymentControlDecision decision = decisionRepository.findById(input.decisionId())
                .orElseThrow(() -> new DecisionNotFoundException("Decision not found: " + input.decisionId()));

        decision.release();
        decisionRepository.save(decision);
        return null;
    }
}
