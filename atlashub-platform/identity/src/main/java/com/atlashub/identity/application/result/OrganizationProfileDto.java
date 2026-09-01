package com.atlashub.identity.application.result;

import java.time.ZonedDateTime;

public record OrganizationProfileDto(
    Long id,
    String businessName,
    String description,
    String logoUrl,
    String kycStatus,
    ZonedDateTime createdAt
) {}
