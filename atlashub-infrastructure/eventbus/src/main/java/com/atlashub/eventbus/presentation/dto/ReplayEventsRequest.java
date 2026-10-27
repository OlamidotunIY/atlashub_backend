package com.atlashub.eventbus.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public record ReplayEventsRequest(
        @NotBlank String topic,
        @NotNull ZonedDateTime from,
        @NotNull ZonedDateTime to,
        @Min(1) @Max(1_000) int limit,
        boolean dryRun
) {}
