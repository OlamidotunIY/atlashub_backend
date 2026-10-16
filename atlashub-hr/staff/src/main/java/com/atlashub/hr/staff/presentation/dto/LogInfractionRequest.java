package com.atlashub.hr.staff.presentation.dto;

import com.atlashub.hr.staff.domain.valueobject.InfractionSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogInfractionRequest(
        @NotBlank(message = "Description is required")
        String description,
        @NotNull(message = "Severity is required")
        InfractionSeverity severity
) {}
