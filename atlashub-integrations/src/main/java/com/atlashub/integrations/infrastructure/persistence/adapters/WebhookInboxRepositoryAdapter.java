package com.atlashub.integrations.infrastructure.persistence.adapters;

import com.atlashub.integrations.application.port.WebhookInbox;
import com.atlashub.integrations.infrastructure.persistence.entities.WebhookInboxJpa;
import com.atlashub.integrations.infrastructure.persistence.repositories.SpringDataWebhookInboxRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.ZonedDateTime;
import java.util.HexFormat;

@Component
public class WebhookInboxRepositoryAdapter implements WebhookInbox {
    private final SpringDataWebhookInboxRepository repository;

    public WebhookInboxRepositoryAdapter(SpringDataWebhookInboxRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public boolean begin(String provider, Long connectionId, String eventId, byte[] payload) {
        var existing = repository.findByProviderAndConnectionIdAndEventId(provider, connectionId, eventId);
        if (existing.isPresent()) return !"PUBLISHED".equals(existing.get().status);
        WebhookInboxJpa row = new WebhookInboxJpa();
        row.provider = provider;
        row.connectionId = connectionId;
        row.eventId = eventId;
        row.payloadHash = hash(payload);
        row.status = "RECEIVED";
        row.receivedAt = ZonedDateTime.now();
        repository.save(row);
        return true;
    }

    @Override
    @Transactional
    public void markPublished(String provider, Long connectionId, String eventId) {
        WebhookInboxJpa row =
                repository.findByProviderAndConnectionIdAndEventId(provider, connectionId, eventId).orElseThrow();
        row.status = "PUBLISHED";
        row.publishedAt = ZonedDateTime.now();
        repository.save(row);
    }

    private String hash(byte[] payload) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload));
        } catch (Exception error) {
            throw new IllegalStateException("Could not hash webhook payload", error);
        }
    }
}
