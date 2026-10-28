package com.atlashub.integrations.application.port;

import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.integrations.domain.valueobject.ProviderOffering;
import com.atlashub.shared.application.security.ApiEnvironment;

import java.util.List;

public interface ProviderCatalogPort {
    List<ProviderOffering> availableIn(String merchantCountry, ApiEnvironment environment);
    boolean supports(Provider provider, String merchantCountry, ApiEnvironment environment,
                     String currency, Capability capability);
}
