package com.atlashub.identity.application.result;

public record ApiKeyPairResult(
    String publicKey,
    String secretKey
) {}
