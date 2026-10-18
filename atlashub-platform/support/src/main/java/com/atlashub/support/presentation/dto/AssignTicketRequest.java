package com.atlashub.support.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record AssignTicketRequest(
        @NotNull(message = "Agent ID is required")
        Long agentId
) {}
