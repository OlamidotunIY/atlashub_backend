package com.atlashub.pay.controls.application.commands.SubmitControlEvidence;

import com.atlashub.pay.controls.domain.entities.ControlCase;
import com.atlashub.pay.controls.domain.exceptions.ControlCaseNotFoundException;
import com.atlashub.pay.controls.domain.repositories.ControlCaseRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SubmitControlEvidenceHandler extends Command<SubmitControlEvidenceCommand, Void> {

    private final ControlCaseRepository caseRepository;

    public SubmitControlEvidenceHandler(ControlCaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    @Override
    @Transactional
    public Void execute(SubmitControlEvidenceCommand input) {
        ControlCase controlCase = caseRepository.findById(input.caseId())
                .orElseThrow(() -> new ControlCaseNotFoundException("Control case not found: " + input.caseId()));

        controlCase.submitEvidence(input.evidenceReference());
        caseRepository.save(controlCase);
        return null;
    }
}
