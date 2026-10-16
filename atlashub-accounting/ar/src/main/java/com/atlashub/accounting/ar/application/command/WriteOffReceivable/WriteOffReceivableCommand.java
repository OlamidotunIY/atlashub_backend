package com.atlashub.accounting.ar.application.command.WriteOffReceivable;

public record WriteOffReceivableCommand(
        Long receivableId,
        String reason
) {}
