package com.atlashub.support.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EscalateTicketRequest(
        @NotNull(message = "New agent ID is required")
        Long newAgentId,

        @NotBlank(message = "Escalation reason is required")
        String escalationReason
) {}
