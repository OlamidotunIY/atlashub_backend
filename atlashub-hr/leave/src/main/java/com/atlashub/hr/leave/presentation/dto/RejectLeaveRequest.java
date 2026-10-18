package com.atlashub.hr.leave.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectLeaveRequest(
        @NotBlank String reason
) {}
