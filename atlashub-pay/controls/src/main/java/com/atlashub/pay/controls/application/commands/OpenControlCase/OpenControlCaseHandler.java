package com.atlashub.pay.controls.application.commands.OpenControlCase;

import com.atlashub.pay.controls.domain.entities.ControlCase;
import com.atlashub.pay.controls.domain.repositories.ControlCaseRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OpenControlCaseHandler extends Command<OpenControlCaseCommand, OpenControlCaseResult> {

    private final ControlCaseRepository caseRepository;

    public OpenControlCaseHandler(ControlCaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    @Override
    @Transactional
    public OpenControlCaseResult execute(OpenControlCaseCommand input) {
        Long caseId = caseRepository.nextIdentity();
        ControlCase controlCase = ControlCase.open(
                caseId,
                input.decisionId(),
                input.organizationId(),
                input.requestedEvidence()
        );

        caseRepository.save(controlCase);

        return new OpenControlCaseResult(
                controlCase.getId(),
                controlCase.getDecisionId(),
                controlCase.getStatus().name()
        );
    }
}
