package com.atlashub.hr.leave.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateLeaveTypeRequest(
        Long organizationId,
        @NotBlank String name,
        @NotNull @Positive Integer maxDaysPerYear,
        Boolean isPaid,
        Boolean requiresApproval,
        Boolean carryOverAllowed,
        Integer maxCarryOverDays
) {}
