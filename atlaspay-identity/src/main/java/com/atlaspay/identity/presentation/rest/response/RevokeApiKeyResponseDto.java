package com.atlaspay.identity.presentation.rest.response;

public record RevokeApiKeyResponseDto(String keyId, boolean active) {}
