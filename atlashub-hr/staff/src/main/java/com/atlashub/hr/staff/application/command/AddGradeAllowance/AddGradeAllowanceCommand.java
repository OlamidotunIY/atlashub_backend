package com.atlashub.hr.staff.application.command.AddGradeAllowance;

import com.atlashub.hr.staff.domain.valueobject.AllowanceType;

import java.math.BigDecimal;

public record AddGradeAllowanceCommand(
        Long gradeId,
        String name,
        AllowanceType type,
        BigDecimal value
) {}
