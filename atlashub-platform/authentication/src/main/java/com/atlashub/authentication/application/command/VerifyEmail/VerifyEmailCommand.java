package com.atlashub.authentication.application.command.VerifyEmail;

public record VerifyEmailCommand(
        String email,
        String rawOtp
) {
}
