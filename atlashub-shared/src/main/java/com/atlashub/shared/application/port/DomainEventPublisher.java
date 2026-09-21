package com.atlashub.shared.application.port;

import com.atlashub.shared.domain.event.EnvelopedDomainEvent;

/**
 * Port for publishing domain events.
 * Implementations might publish directly to an in-memory bus,
 * or serialize and insert into a transactional outbox table.
 */
public interface DomainEventPublisher {
    void publish(EnvelopedDomainEvent<?> event);
}
