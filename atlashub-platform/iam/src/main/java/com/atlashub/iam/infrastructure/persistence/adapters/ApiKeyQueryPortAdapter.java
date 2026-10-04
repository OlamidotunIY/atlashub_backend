package com.atlashub.iam.infrastructure.persistence.adapters;

import com.atlashub.iam.application.security.ApiKeyAuthenticationService;
import com.atlashub.shared.application.port.ApiKeyQueryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApiKeyQueryPortAdapter implements ApiKeyQueryPort {
    private final ApiKeyAuthenticationService authenticationService;

    public ApiKeyQueryPortAdapter(ApiKeyAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Optional<AuthenticatedApiKey> authenticate(String publicKey, String canonicalMessage, String signature) {
        return authenticationService.authenticate(publicKey, canonicalMessage, signature)
                .map(key -> new AuthenticatedApiKey(
                        key.organizationId(), key.environment(), key.publicKey(), key.permissions()));
    }
}
