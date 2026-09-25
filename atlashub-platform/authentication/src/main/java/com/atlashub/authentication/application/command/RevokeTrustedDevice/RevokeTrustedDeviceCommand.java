package com.atlashub.authentication.application.command.RevokeTrustedDevice;

public record RevokeTrustedDeviceCommand(
        Long userId,
        Long deviceId
) {
}
