package com.atlaspay.admin.application.dto;

import java.util.Set;
import java.util.stream.Collectors;

public record AdminDto(
    Long id,
    String username,
    String email,
    String role,
    String status,
    Set<String> permissions
) {
    public static AdminDto from(com.atlaspay.admin.domain.model.Admin admin) {
        return new AdminDto(
            admin.getId(),
            admin.getUsername(),
            admin.getEmail().value(),
            admin.getRole().name(),
            admin.getStatus().name(),
            admin.getPermissions().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }
}
