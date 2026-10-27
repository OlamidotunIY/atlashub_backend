package com.atlashub.eventbus.application.command;

import java.time.ZonedDateTime;

public record ReplayEventsResult(
        String topic,
        ZonedDateTime from,
        ZonedDateTime to,
        int matched,
        int replayed,
        boolean dryRun
) {}
