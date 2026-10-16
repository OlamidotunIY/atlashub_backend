package com.atlashub.hr.staff.application.query.ListEmployees;

import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;

public record ListEmployeesQuery(
        Long organizationId,
        EmployeeStatus status,
        String department,
        int page,
        int size
) {}
