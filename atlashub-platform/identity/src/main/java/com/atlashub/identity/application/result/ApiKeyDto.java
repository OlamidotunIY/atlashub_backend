package com.atlashub.identity.application.result;

import java.time.ZonedDateTime;

public record ApiKeyDto(
    Long id,
    String keyType,
    String environment,
    String displayValue,
    boolean active,
    ZonedDateTime createdAt
) {}
