package com.atlashub.identity.application.result;

import com.atlashub.identity.domain.model.User;
import java.time.ZonedDateTime;

public record UserDto(
    Long id,
    String firstName,
    String lastName,
    String email,
    String imageUrl,
    String phone,
    String country,
    ZonedDateTime createdAt
) {
    public static UserDto from(User user) {
        return new UserDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail() != null ? user.getEmail().value() : null,
            user.getImageUrl(),
            user.getPhone() != null ? user.getPhone().value() : null,
            user.getCountry() != null ? user.getCountry().name() : null,
            user.getCreatedAt()
        );
    }
}
