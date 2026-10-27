package com.atlashub.eventbus.application.command;

import java.time.ZonedDateTime;

public record ReplayEventsCommand(
        String topic,
        ZonedDateTime from,
        ZonedDateTime to,
        int limit,
        boolean dryRun,
        Long requestedBy
) {
    public ReplayEventsCommand {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Replay topic is required");
        }
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("Replay window must have a valid from and to time");
        }
        if (limit < 1 || limit > 1_000) {
            throw new IllegalArgumentException("Replay limit must be between 1 and 1000");
        }
        if (requestedBy == null) {
            throw new IllegalArgumentException("Replay operator is required");
        }
    }
}
