package com.atlashub.integrations.application.query.ListAvailableProviders;

import com.atlashub.shared.application.security.ApiEnvironment;

public record ListAvailableProvidersQuery(String merchantCountry, ApiEnvironment environment) {}
