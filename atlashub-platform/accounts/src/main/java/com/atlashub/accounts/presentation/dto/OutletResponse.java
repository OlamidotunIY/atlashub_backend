package com.atlashub.accounts.presentation.dto;

import java.time.ZonedDateTime;

public record OutletResponse(
        Long id,
        Long organizationId,
        String name,
        String address,
        String city,
        String state,
        String country,
        String currency,
        Long managerId,
        String status,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {}
