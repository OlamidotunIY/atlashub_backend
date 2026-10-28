package com.atlashub.integrations.presentation.dto;

import com.atlashub.integrations.domain.valueobject.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CredentialConnectionRequest(@NotNull Provider provider, @NotBlank String merchantCountry,
                                          @NotBlank String credential) {}
