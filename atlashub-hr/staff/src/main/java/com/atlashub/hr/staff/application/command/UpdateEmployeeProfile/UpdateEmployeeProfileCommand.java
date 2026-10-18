package com.atlashub.hr.staff.application.command.UpdateEmployeeProfile;

public record UpdateEmployeeProfileCommand(
        Long employeeId,
        String firstName,
        String lastName,
        String phone,
        String address,
        String nationality
) {}
