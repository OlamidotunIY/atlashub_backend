package com.atlashub.integrations.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthorizationSessionRequest(@NotBlank String merchantCountry) {}
