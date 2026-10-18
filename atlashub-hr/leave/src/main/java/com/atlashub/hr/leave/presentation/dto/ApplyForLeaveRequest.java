package com.atlashub.hr.leave.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ApplyForLeaveRequest(
        @NotNull Long employeeId,
        @NotNull Long leaveTypeId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason
) {}
