package com.atlashub.accounting.ar.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record WriteOffRequest(
        @NotBlank(message = "Write-off reason is required")
        String reason
) {}
