package com.atlashub.integrations.presentation.dto;

import java.time.ZonedDateTime;

public record AuthorizationSessionResponse(String authorizationUrl, ZonedDateTime expiresAt) {}
