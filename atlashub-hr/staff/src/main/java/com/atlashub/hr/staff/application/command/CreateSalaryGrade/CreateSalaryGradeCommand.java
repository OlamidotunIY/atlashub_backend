package com.atlashub.hr.staff.application.command.CreateSalaryGrade;

import com.atlashub.shared.domain.valueobject.Money;

public record CreateSalaryGradeCommand(
        Long organizationId,
        String name,
        Money baseSalary
) {}
