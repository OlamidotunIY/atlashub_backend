package com.atlashub.hr.staff.application.command.AddGradeDeduction;

import com.atlashub.hr.staff.domain.valueobject.DeductionType;

import java.math.BigDecimal;

public record AddGradeDeductionCommand(
        Long gradeId,
        String name,
        DeductionType type,
        BigDecimal value,
        boolean isStatutory
) {}
