package com.atlashub.hr.attendance.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record ClockInRequest(
        @NotNull Long employeeId,
        Long organizationId
) {}
