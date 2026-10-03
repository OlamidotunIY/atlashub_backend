package com.atlashub.authentication.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SwitchEnvironmentRequest(
        @NotBlank
        @Pattern(regexp = "(?i)TEST|LIVE", message = "environment must be TEST or LIVE")
        String environment
) {
}
