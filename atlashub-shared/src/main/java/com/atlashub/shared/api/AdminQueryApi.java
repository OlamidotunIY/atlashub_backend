package com.atlashub.shared.api;

import java.util.Optional;

public interface AdminQueryApi {
    Optional<AdminSharedDto> getAdminById(Long adminId);

    record AdminSharedDto(
        Long id,
        String username,
        String email
    ) {}
}
