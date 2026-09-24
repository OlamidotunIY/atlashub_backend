package com.atlashub.authentication.application.command.ChangePassword;

public record ChangePasswordCommand(
        Long userId,
        String currentPassword,
        String newPassword
) {
}
