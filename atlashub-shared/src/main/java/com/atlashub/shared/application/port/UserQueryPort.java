package com.atlashub.shared.application.port;

import java.util.Optional;

public interface UserQueryPort {
    Optional<UserDto> findById(Long userId);

    Optional<UserDto> findByEmail(String email);

    record UserDto(
            Long id,
            String firstName,
            String lastName,
            String email,
            String country,
            Long activeOrganizationId
    ) {
    }
}
