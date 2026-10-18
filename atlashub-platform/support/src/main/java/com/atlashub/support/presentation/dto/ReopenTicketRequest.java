package com.atlashub.support.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record ReopenTicketRequest(
        @NotBlank(message = "Reopen reason is required")
        String reason
) {}
