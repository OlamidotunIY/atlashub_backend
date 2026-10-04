package com.atlashub.iam.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignRoleRequest(@NotNull @Positive Long roleId) {
}
