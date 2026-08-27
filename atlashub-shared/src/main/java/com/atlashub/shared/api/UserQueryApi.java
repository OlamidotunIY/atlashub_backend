package com.atlashub.shared.api;

import java.util.Optional;

public interface UserQueryApi {
    Optional<UserSharedDto> getUserById(Long userId);

    record UserSharedDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String country
    ) {}
}
