package com.atlashub.pay.controls.application.commands.ResolveControlCase;

import com.atlashub.pay.controls.domain.entities.ControlCase;
import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.pay.controls.domain.exceptions.ControlCaseNotFoundException;
import com.atlashub.pay.controls.domain.repositories.ControlCaseRepository;
import com.atlashub.pay.controls.domain.repositories.PaymentControlDecisionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class ResolveControlCaseHandler extends Command<ResolveControlCaseCommand, Void> {

    private final ControlCaseRepository caseRepository;
    private final PaymentControlDecisionRepository decisionRepository;

    public ResolveControlCaseHandler(ControlCaseRepository caseRepository,
                                     PaymentControlDecisionRepository decisionRepository) {
        this.caseRepository = caseRepository;
        this.decisionRepository = decisionRepository;
    }

    @Override
    @Transactional
    public Void execute(ResolveControlCaseCommand input) {
        ControlCase controlCase = caseRepository.findById(input.caseId())
                .orElseThrow(() -> new ControlCaseNotFoundException("Control case not found: " + input.caseId()));

        if (input.approve()) {
            controlCase.approveRelease(input.reviewerId(), input.reason());
            Optional<PaymentControlDecision> decisionOpt = decisionRepository.findById(controlCase.getDecisionId());
            if (decisionOpt.isPresent()) {
                PaymentControlDecision decision = decisionOpt.get();
                decision.release();
                decisionRepository.save(decision);
            }
        } else {
            controlCase.rejectRelease(input.reviewerId(), input.reason());
        }

        caseRepository.save(controlCase);
        return null;
    }
}
