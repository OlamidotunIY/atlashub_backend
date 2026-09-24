package com.atlashub.authentication.presentation.dto;

import java.time.ZonedDateTime;

public record TrustedDeviceWebResponse(
        Long id,
        String deviceFingerprint,
        String deviceName,
        String lastSeenIp,
        ZonedDateTime trustedAt,
        ZonedDateTime expiresAt
) {
}
