package com.atlashub.integrations.application.command.StartStripeAuthorization;

import com.atlashub.shared.application.security.ApiEnvironment;

public record StartStripeAuthorizationCommand(Long organizationId, ApiEnvironment environment,
                                              String merchantCountry) {}
