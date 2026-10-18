package com.atlashub.support.application.command.MarkSlaBreached;

public record MarkSlaBreachedResult(
        Long ticketId,
        boolean slaBreached
) {}
