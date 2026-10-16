package com.atlashub.accounting.cash.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MatchReconciliationItemRequest(
        @NotNull(message = "Bank statement line ID is required")
        @Positive(message = "Bank statement line ID must be positive")
        Long bankLineId,

        @NotNull(message = "Journal line ID is required")
        @Positive(message = "Journal line ID must be positive")
        Long journalLineId
) {}
