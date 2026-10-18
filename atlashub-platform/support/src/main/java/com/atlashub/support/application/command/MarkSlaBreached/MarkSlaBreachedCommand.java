package com.atlashub.support.application.command.MarkSlaBreached;

public record MarkSlaBreachedCommand(
        Long ticketId,
        String breachType,
        String correlationId
) {}
