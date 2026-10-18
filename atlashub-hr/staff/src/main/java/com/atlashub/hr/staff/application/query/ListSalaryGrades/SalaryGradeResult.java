package com.atlashub.hr.staff.application.query.ListSalaryGrades;

import com.atlashub.hr.staff.domain.valueobject.AllowanceType;
import com.atlashub.hr.staff.domain.valueobject.DeductionType;

import java.math.BigDecimal;
import java.util.List;

public record SalaryGradeResult(
        Long id,
        Long organizationId,
        String name,
        BigDecimal baseSalary,
        String currency,
        List<GradeAllowanceItem> allowances,
        List<GradeDeductionItem> deductions
) {
    public record GradeAllowanceItem(
            Long id,
            String name,
            AllowanceType type,
            BigDecimal value
    ) {}

    public record GradeDeductionItem(
            Long id,
            String name,
            DeductionType type,
            BigDecimal value,
            boolean isStatutory
    ) {}
}
