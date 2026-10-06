package com.atlashub.authentication.application.command.AuthorizeDevice;

public record AuthorizeDeviceCommand(
        String email,
        String rawOtp,
        String deviceFingerprint,
        String deviceName,
        String ipAddress
) {
}
