package com.atlashub.hr.staff.presentation.dto;

import com.atlashub.hr.staff.domain.valueobject.AllowanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AddGradeAllowanceRequest(
        @NotBlank(message = "Allowance name is required")
        String name,
        @NotNull(message = "Allowance type is required")
        AllowanceType type,
        @NotNull(message = "Allowance value is required")
        @Positive(message = "Allowance value must be positive")
        BigDecimal value
) {}
