package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;

public record MonthlyPayrollCostDto(
        String month,
        BigDecimal totalGrossPayroll,
        BigDecimal totalDeductions,
        BigDecimal totalNetPayroll,
        int headcount
) {}
