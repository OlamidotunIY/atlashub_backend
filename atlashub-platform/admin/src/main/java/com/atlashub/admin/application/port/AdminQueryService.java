package com.atlashub.admin.application.port;

import java.util.Optional;

public interface AdminQueryService {
    Optional<AdminDto> getAdminById(Long adminId);

    record AdminDto(Long id, String email, String role, String username) {}
}
