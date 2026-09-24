package com.atlashub.accounts.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record SwitchOrganizationRequest(
        @NotNull Long organizationId
) {
}
