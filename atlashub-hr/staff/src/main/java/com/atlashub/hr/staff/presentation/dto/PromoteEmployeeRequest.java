package com.atlashub.hr.staff.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PromoteEmployeeRequest(
        @NotBlank(message = "New designation is required")
        String newDesignation,
        @NotNull(message = "New salary grade ID is required")
        Long newGradeId,
        @NotNull(message = "New base salary is required")
        @Positive(message = "New base salary must be positive")
        BigDecimal newBaseSalary,
        String currency
) {}
