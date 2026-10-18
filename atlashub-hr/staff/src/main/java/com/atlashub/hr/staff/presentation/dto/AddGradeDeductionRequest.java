package com.atlashub.hr.staff.presentation.dto;

import com.atlashub.hr.staff.domain.valueobject.DeductionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AddGradeDeductionRequest(
        @NotBlank(message = "Deduction name is required")
        String name,
        @NotNull(message = "Deduction type is required")
        DeductionType type,
        @NotNull(message = "Deduction value is required")
        @Positive(message = "Deduction value must be positive")
        BigDecimal value,
        boolean isStatutory
) {}
