package com.atlaspay.admin.presentation.rest.request;

import com.atlaspay.admin.domain.model.AdminPermission;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record CreateAdminRequestDto(
        @NotBlank String username,
        @NotBlank String destinationEmail,
        Set<AdminPermission> permissions
) {}
