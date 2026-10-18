package com.atlashub.hr.staff.application.query.GetEmployeeDetails;

public record GetEmployeeDetailsQuery(
        Long employeeId,
        Long organizationId
) {}
