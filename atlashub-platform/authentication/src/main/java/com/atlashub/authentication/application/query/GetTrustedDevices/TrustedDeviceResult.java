package com.atlashub.authentication.application.query.GetTrustedDevices;

import java.time.ZonedDateTime;

public record TrustedDeviceResult(
        Long id,
        String deviceFingerprint,
        String deviceName,
        String lastSeenIp,
        ZonedDateTime trustedAt,
        ZonedDateTime expiresAt
) {
}
