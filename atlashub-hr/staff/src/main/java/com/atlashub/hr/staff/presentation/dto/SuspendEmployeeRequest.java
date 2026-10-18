package com.atlashub.hr.staff.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record SuspendEmployeeRequest(
        @NotBlank(message = "Suspension reason is required")
        String reason
) {}
