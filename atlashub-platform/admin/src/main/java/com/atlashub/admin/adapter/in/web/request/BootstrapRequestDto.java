package com.atlashub.admin.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record BootstrapRequestDto(@NotBlank String username) {}
