package com.atlashub.hr.staff.presentation.dto;

public record UpdateEmployeeProfileRequest(
        String firstName,
        String lastName,
        String phone,
        String address,
        String nationality
) {}
