package com.atlashub.identity.presentation.rest.response;

public record RevokeApiKeyResponseDto(String keyId, boolean active) {}
