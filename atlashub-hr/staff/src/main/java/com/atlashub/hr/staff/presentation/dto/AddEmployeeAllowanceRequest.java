package com.atlashub.hr.staff.presentation.dto;

import com.atlashub.hr.staff.domain.valueobject.AllowanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddEmployeeAllowanceRequest(
        @NotBlank(message = "Allowance name is required")
        String name,
        @NotNull(message = "Allowance type is required")
        AllowanceType type,
        @NotNull(message = "Allowance value is required")
        @Positive(message = "Allowance value must be positive")
        BigDecimal value,
        @NotNull(message = "Effective from date is required")
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {}
