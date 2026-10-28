package com.atlashub.integrations.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "integration_webhook_inbox", uniqueConstraints = @UniqueConstraint(
        name = "uk_webhook_provider_connection_event", columnNames = {"provider", "connection_id", "event_id"}))
public class WebhookInboxJpa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public String provider;
    @Column(name = "connection_id", nullable = false) public Long connectionId;
    @Column(name = "event_id", nullable = false) public String eventId;
    @Column(name = "payload_hash", nullable = false, length = 64) public String payloadHash;
    @Column(nullable = false) public String status;
    @Column(name = "received_at", nullable = false) public ZonedDateTime receivedAt;
    @Column(name = "published_at") public ZonedDateTime publishedAt;
}
