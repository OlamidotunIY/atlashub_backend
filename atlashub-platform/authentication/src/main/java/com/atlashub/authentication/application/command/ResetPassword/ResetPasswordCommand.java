package com.atlashub.authentication.application.command.ResetPassword;

public record ResetPasswordCommand(
        String email,
        String rawOtp,
        String newPassword
) {
}
