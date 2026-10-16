package com.atlashub.hr.staff.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateSalaryGradeRequest(
        Long organizationId,
        @NotBlank(message = "Salary grade name is required")
        String name,
        @NotNull(message = "Base salary is required")
        @Positive(message = "Base salary must be positive")
        BigDecimal baseSalary,
        String currency
) {}
