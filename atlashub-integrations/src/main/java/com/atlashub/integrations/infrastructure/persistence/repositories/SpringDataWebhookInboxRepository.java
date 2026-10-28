package com.atlashub.integrations.infrastructure.persistence.repositories;

import com.atlashub.integrations.infrastructure.persistence.entities.WebhookInboxJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataWebhookInboxRepository extends JpaRepository<WebhookInboxJpa, Long> {
    Optional<WebhookInboxJpa> findByProviderAndConnectionIdAndEventId(String provider, Long connectionId,
                                                                      String eventId);
}
