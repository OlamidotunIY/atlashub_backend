package com.atlashub.hr.staff.application.query.ListEmployees;

import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;

import java.time.LocalDate;

public record EmployeeResult(
        Long id,
        String employeeNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        String designation,
        String department,
        EmployeeStatus status,
        LocalDate hireDate
) {}
