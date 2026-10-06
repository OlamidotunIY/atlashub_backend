package com.atlashub.authentication.presentation.dto;

import com.atlashub.shared.application.security.ApiEnvironment;
import jakarta.validation.constraints.NotNull;

public record SwitchEnvironmentRequest(
        @NotNull
        ApiEnvironment environment
) {
}
