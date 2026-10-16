package com.atlashub.pay.controls.application.commands.SubmitControlEvidence;

public record SubmitControlEvidenceCommand(
        Long caseId,
        String evidenceReference,
        String description
) {}
