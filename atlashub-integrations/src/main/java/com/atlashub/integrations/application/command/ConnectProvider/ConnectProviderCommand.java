package com.atlashub.integrations.application.command.ConnectProvider;

import com.atlashub.integrations.domain.valueobject.Provider;

public record ConnectProviderCommand(Long organizationId, Provider provider, String merchantCountry,
                                     String credential) {}
