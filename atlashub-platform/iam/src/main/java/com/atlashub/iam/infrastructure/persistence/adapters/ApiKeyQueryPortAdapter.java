package com.atlashub.iam.infrastructure.persistence.adapters;

import com.atlashub.iam.application.security.ApiKeyAuthenticationService;
import com.atlashub.shared.application.port.ApiKeyQueryPort;
import org.springframework.stereotype.Component;
import com.atlashub.iam.domain.events.ApiKeyAuthenticatedEvent;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import java.time.ZonedDateTime;
import java.util.UUID;

import java.util.Optional;

@Component
public class ApiKeyQueryPortAdapter implements ApiKeyQueryPort {
    private final ApiKeyAuthenticationService authenticationService;
    private final DomainEventPublisher eventPublisher;

    public ApiKeyQueryPortAdapter(ApiKeyAuthenticationService authenticationService,
                                  DomainEventPublisher eventPublisher) {
        this.authenticationService = authenticationService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<AuthenticatedApiKey> authenticate(String publicKey, String canonicalMessage, String signature) {
        return authenticationService.authenticate(publicKey, canonicalMessage, signature)
                .map(key -> {
                    ZonedDateTime now = ZonedDateTime.now();
                    eventPublisher.publish(EnvelopedDomainEvent.wrap(new ApiKeyAuthenticatedEvent(
                            UUID.randomUUID().toString(), key.organizationId(), now, CorrelationId.getOrCreate(),
                            new ApiKeyAuthenticatedEvent.Payload(key.organizationId(), key.publicKey(), now))));
                    return new AuthenticatedApiKey(
                            key.organizationId(), key.environment(), key.publicKey(), key.permissions());
                });
    }
}
