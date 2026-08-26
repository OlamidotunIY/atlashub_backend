package com.atlashub.admin.presentation.rest.request;

import jakarta.validation.constraints.NotBlank;

public record BootstrapRequestDto(@NotBlank String username) {}
