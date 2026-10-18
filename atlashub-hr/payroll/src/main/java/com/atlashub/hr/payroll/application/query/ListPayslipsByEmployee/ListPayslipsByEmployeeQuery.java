package com.atlashub.hr.payroll.application.query.ListPayslipsByEmployee;

public record ListPayslipsByEmployeeQuery(
        Long employeeId,
        int year
) {
}
