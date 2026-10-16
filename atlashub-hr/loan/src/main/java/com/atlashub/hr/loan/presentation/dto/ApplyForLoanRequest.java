package com.atlashub.hr.loan.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ApplyForLoanRequest(
        @NotNull Long employeeId,
        Long organizationId,
        @NotNull @DecimalMin(value = "0.01", message = "Principal amount must be positive") BigDecimal principalAmount,
        @NotNull @DecimalMin(value = "0.01", message = "Monthly deduction must be positive") BigDecimal monthlyDeduction,
        @NotBlank String currency
) {
}
