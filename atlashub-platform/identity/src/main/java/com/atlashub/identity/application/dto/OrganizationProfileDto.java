package com.atlashub.identity.application.dto;

import java.time.ZonedDateTime;

public record OrganizationProfileDto(
    Long id,
    String businessName,
    String email,
    String phone,
    String kycStatus,
    ZonedDateTime createdAt
) {}
