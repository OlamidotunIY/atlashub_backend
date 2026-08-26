package com.atlashub.auth.application.dto;

public record TotpSetupDto(
        String secret,
        String qrCodeUri
) {}
