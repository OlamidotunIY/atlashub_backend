package com.atlashub.integrations.presentation.dto;

import com.atlashub.integrations.domain.valueobject.Capability;
import java.util.Set;

public record ProviderAuthorizationResponse(Long id, String environment, String externalMerchantId,
                                            String status, String health, Set<Capability> capabilities) {}
