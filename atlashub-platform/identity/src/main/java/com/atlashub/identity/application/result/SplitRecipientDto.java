package com.atlashub.identity.application.result;

import java.time.ZonedDateTime;

public record SplitRecipientDto(
    Long id,
    Long integration,
    String bankCode,
    String accountNumber,
    String accountName,
    String description,
    boolean active,
    ZonedDateTime createdAt
) {}
