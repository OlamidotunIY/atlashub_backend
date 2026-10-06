package com.atlashub.shared.application.port;

import java.util.Optional;
import java.util.Set;

public interface ApiKeyQueryPort {
    Optional<AuthenticatedApiKey> authenticate(String publicKey, String canonicalMessage, String signature);

    record AuthenticatedApiKey(
            Long organizationId,
            String environment,
            String publicKey,
            Set<String> permissions
    ) {}
}
