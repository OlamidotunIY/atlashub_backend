package com.atlashub.accounts.application.query.GetOutlet;

import com.atlashub.accounts.domain.valueobject.OutletStatus;

import java.time.ZonedDateTime;

public record OutletResult(
        Long id,
        Long organizationId,
        String name,
        String address,
        String city,
        String state,
        String country,
        String currency,
        Long managerId,
        OutletStatus status,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {}
