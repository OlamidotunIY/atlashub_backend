package com.atlashub.analytics.application.query.GetPayrollCostTrend;

import java.math.BigDecimal;

public record MonthlyPayrollCostResult(
        String month,
        BigDecimal totalGrossPayroll,
        BigDecimal totalDeductions,
        BigDecimal totalNetPayroll,
        int headcount
) {}
