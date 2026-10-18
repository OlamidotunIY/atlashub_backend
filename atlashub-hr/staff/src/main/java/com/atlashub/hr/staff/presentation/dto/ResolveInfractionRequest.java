package com.atlashub.hr.staff.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveInfractionRequest(
        @NotBlank(message = "Resolution notes are required")
        String resolutionNotes
) {}
