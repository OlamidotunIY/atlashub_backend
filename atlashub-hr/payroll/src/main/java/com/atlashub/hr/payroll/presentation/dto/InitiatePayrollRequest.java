package com.atlashub.hr.payroll.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record InitiatePayrollRequest(
        Long organizationId,
        @NotBlank @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Period must be in YYYY-MM format") String period
) {
}
