package com.atlashub.iam.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record IssueApiKeyRequest(
        @NotBlank String name,
        @NotBlank String environment,
        Long boundRoleId
) {
}
