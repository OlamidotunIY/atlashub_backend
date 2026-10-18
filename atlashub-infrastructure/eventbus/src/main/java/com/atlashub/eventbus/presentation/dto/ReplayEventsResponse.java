package com.atlashub.eventbus.presentation.dto;

import com.atlashub.eventbus.application.command.ReplayEventsResult;

import java.time.ZonedDateTime;

public record ReplayEventsResponse(
        String topic,
        ZonedDateTime from,
        ZonedDateTime to,
        int matched,
        int replayed,
        boolean dryRun
) {
    public static ReplayEventsResponse from(ReplayEventsResult result) {
        return new ReplayEventsResponse(result.topic(), result.from(), result.to(), result.matched(),
                result.replayed(), result.dryRun());
    }
}
