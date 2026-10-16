package com.atlashub.pay.controls.application.commands.OpenControlCase;

public record OpenControlCaseResult(
        Long caseId,
        Long decisionId,
        String status
) {}
