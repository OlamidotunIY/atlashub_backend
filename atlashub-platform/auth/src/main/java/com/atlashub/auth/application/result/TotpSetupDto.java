package com.atlashub.auth.application.result;

public record TotpSetupDto(
        String secret,
        String qrCodeUri
) {}
