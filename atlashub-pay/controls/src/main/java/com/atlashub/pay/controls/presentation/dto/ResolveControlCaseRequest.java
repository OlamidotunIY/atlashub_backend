package com.atlashub.pay.controls.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResolveControlCaseRequest(
        @NotNull(message = "Approval decision is required")
        Boolean approve,
        @NotBlank(message = "Resolution reason must not be blank")
        String reason
) {}
