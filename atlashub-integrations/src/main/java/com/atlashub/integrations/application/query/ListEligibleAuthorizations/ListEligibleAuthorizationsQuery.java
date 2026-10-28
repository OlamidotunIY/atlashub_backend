package com.atlashub.integrations.application.query.ListEligibleAuthorizations;

import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.shared.application.security.ApiEnvironment;

public record ListEligibleAuthorizationsQuery(Long organizationId, ApiEnvironment environment,
                                              String currency, Capability capability) {}
