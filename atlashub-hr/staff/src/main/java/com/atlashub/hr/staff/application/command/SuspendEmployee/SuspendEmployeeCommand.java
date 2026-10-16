package com.atlashub.hr.staff.application.command.SuspendEmployee;

public record SuspendEmployeeCommand(
        Long employeeId,
        String reason
) {}
