package com.atlashub.identity.application.result;

import java.time.ZonedDateTime;
import java.util.Map;

public record UserDto(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phone,
    Map<String, String> metadata,
    ZonedDateTime createdAt
) {}
