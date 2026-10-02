package com.atlashub.iam.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record AssignRoleRequest(@NotNull Long roleId) {
}
