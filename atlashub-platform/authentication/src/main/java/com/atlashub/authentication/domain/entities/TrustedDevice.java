package com.atlashub.authentication.domain.entities;

import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class TrustedDevice {
    private final Long id;
    private final Long userId;
    private String deviceFingerprint;
    private String deviceName;
    private String lastSeenIp;
    private ZonedDateTime trustedAt;
    private ZonedDateTime expiresAt;

    public TrustedDevice(Long id, Long userId, String deviceFingerprint, String deviceName, String lastSeenIp, ZonedDateTime trustedAt, ZonedDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.deviceFingerprint = deviceFingerprint;
        this.deviceName = deviceName;
        this.lastSeenIp = lastSeenIp;
        this.trustedAt = trustedAt;
        this.expiresAt = expiresAt;
    }

    public static TrustedDevice create(Long id, Long userId, String deviceFingerprint, String deviceName, String lastSeenIp) {
        ZonedDateTime now = ZonedDateTime.now();

        return new TrustedDevice(id, userId, deviceFingerprint, deviceName, lastSeenIp, now, now.plusDays(90));
    }
}
