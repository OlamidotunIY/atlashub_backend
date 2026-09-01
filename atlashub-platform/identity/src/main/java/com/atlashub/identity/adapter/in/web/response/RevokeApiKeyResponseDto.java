package com.atlashub.identity.adapter.in.web.response;

public record RevokeApiKeyResponseDto(String keyId, boolean active) {}
