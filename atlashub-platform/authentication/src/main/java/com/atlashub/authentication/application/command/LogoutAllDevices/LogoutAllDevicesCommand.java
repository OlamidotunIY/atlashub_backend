package com.atlashub.authentication.application.command.LogoutAllDevices;

/**
 * userId is resolved from SecurityContext in the controller layer.
 */
public record LogoutAllDevicesCommand(Long userId) {
}
