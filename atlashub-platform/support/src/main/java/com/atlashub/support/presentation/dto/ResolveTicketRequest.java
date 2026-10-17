package com.atlashub.support.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveTicketRequest(
        @NotBlank(message = "Resolution note is required")
        String resolutionNote
) {}
