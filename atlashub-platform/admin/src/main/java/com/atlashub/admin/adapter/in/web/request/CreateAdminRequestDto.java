package com.atlashub.admin.adapter.in.web.request;

import com.atlashub.admin.domain.model.AdminPermission;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record CreateAdminRequestDto(
        @NotBlank String username,
        @NotBlank String destinationEmail,
        Set<AdminPermission> permissions
) {}
