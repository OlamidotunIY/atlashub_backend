package com.atlashub.pay.controls.application.commands.ResolveControlCase;

public record ResolveControlCaseCommand(
        Long caseId,
        Long reviewerId,
        boolean approve,
        String reason
) {}
