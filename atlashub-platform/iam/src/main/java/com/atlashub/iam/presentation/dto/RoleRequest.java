package com.atlashub.iam.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record RoleRequest(
        @NotBlank String name,
        String description,
        @NotNull Set<Long> permissionIds
) {
}
