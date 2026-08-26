package com.atlashub.identity.application.dto;

public record ApiKeyPairResult(
    String publicKey,
    String secretKey
) {}
