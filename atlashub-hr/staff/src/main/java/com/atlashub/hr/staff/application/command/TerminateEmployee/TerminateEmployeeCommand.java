package com.atlashub.hr.staff.application.command.TerminateEmployee;

import java.time.LocalDate;

public record TerminateEmployeeCommand(
        Long employeeId,
        LocalDate terminationDate
) {}
