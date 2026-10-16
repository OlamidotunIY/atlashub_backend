package com.atlashub.hr.staff.application.command.PromoteEmployee;

import com.atlashub.shared.domain.valueobject.Money;

public record PromoteEmployeeCommand(
        Long employeeId,
        String newDesignation,
        Long newGradeId,
        Money newBaseSalary
) {}
