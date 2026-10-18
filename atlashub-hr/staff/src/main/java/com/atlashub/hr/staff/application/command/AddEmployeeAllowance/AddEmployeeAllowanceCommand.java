package com.atlashub.hr.staff.application.command.AddEmployeeAllowance;

import com.atlashub.hr.staff.domain.valueobject.AllowanceType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddEmployeeAllowanceCommand(
        Long employeeId,
        String name,
        AllowanceType type,
        BigDecimal value,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {}
