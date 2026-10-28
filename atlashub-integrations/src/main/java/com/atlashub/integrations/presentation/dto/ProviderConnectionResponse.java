package com.atlashub.integrations.presentation.dto;

import com.atlashub.integrations.domain.valueobject.Provider;
import java.util.List;

public record ProviderConnectionResponse(Long id, Provider provider, String merchantCountry, String status,
                                         List<ProviderAuthorizationResponse> authorizations) {}
