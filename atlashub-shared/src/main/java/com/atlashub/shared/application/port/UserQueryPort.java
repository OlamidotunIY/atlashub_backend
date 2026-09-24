package com.atlashub.shared.application.port;

import java.util.Optional;

public interface UserQueryPort {
    Optional<UserDto> findById(Long userId);
    Optional<UserDto> findByEmail(String email);
    boolean existsById(Long userId);
    Optional<Long> getActiveOrganizationId(Long userId);

    record UserDto(
            Long id,
            String firstName,
            String lastName,
            String email,
            String country,
            Long activeOrganizationId,
            boolean emailVerified
    ) {
    }
}
