package com.atlashub.hr.staff.presentation.dto;

import com.atlashub.hr.staff.domain.valueobject.DeductionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddEmployeeDeductionRequest(
        @NotBlank(message = "Deduction name is required")
        String name,
        @NotNull(message = "Deduction type is required")
        DeductionType type,
        @NotNull(message = "Deduction value is required")
        @Positive(message = "Deduction value must be positive")
        BigDecimal value,
        @NotNull(message = "Effective from date is required")
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Long loanId
) {}
