package com.atlashub.hr.attendance.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record ClockOutRequest(
        @NotNull Long employeeId,
        Long organizationId
) {}
