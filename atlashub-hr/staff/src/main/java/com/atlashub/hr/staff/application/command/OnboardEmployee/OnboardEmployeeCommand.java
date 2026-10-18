package com.atlashub.hr.staff.application.command.OnboardEmployee;

import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;

public record OnboardEmployeeCommand(
        Long organizationId,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String address,
        String nationality,
        LocalDate hireDate,
        String designation,
        String department,
        Long reportingManagerId,
        Long salaryGradeId,
        Money baseSalary
) {}
