package com.atlashub.identity.application.command;

import com.atlashub.identity.domain.model.ApiEnvironment;
import com.atlashub.identity.domain.model.KeyType;

public record RegenerateApiKeyCommand(
    Long authenticatedOrganizationId,
    KeyType keyType,
    ApiEnvironment environment
) {}
