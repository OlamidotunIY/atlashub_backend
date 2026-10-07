package com.atlashub.notifications.application.command.SendRegistrationOtp;

public record SendRegistrationOtpCommand(String recipientEmail, String correlationId, long expiresAtEpochSeconds) { }
