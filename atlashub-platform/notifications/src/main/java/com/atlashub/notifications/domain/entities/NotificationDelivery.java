package com.atlashub.notifications.domain.entities;

import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.notifications.domain.valueobject.RecipientType;
import com.atlashub.notifications.domain.valueobject.NotificationChannel;
import com.atlashub.notifications.domain.valueobject.DeliveryStatus;

import lombok.Getter;
import java.time.ZonedDateTime;
import java.util.Map;

@Getter
public class NotificationDelivery extends AggregateRoot<Long> {

    private final Long id;
    private final String templateCode;
    private final String correlationId;
    private final Integer templateVersion;
    private final RecipientType recipientType;
    private final String recipientId;
    private final NotificationChannel channel;
    private final String provider;
    private final Map<String, String> variables;
    private final String renderedSubject;
    private final String renderedBody;
    private DeliveryStatus status;
    private Integer attemptCount;
    private String providerMessageId;
    private String failureReason;
    private ZonedDateTime nextRetryAt;
    private ZonedDateTime deliveredAt;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    private NotificationDelivery(
            Long id,
            String templateCode,
            String correlationId,
            Integer templateVersion,
            RecipientType recipientType,
            String recipientId,
            NotificationChannel channel,
            String provider,
            Map<String, String> variables,
            String renderedSubject,
            String renderedBody) {
        this.id = id;
        this.templateCode = templateCode;
        this.correlationId = correlationId;
        this.templateVersion = templateVersion;
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.channel = channel;
        this.provider = provider;
        this.variables = variables;
        this.renderedSubject = renderedSubject;
        this.renderedBody = renderedBody;
        this.status = DeliveryStatus.PENDING;
        this.attemptCount = 0;
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public NotificationDelivery(Long id, String templateCode, String correlationId, Integer templateVersion,
                                RecipientType recipientType, String recipientId, NotificationChannel channel,
                                String provider, Map<String, String> variables, String renderedSubject,
                                String renderedBody, DeliveryStatus status, Integer attemptCount,
                                String providerMessageId, String failureReason, ZonedDateTime nextRetryAt,
                                ZonedDateTime deliveredAt, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id; this.templateCode = templateCode; this.correlationId = correlationId;
        this.templateVersion = templateVersion; this.recipientType = recipientType; this.recipientId = recipientId;
        this.channel = channel; this.provider = provider; this.variables = variables; this.renderedSubject = renderedSubject;
        this.renderedBody = renderedBody; this.status = status; this.attemptCount = attemptCount;
        this.providerMessageId = providerMessageId; this.failureReason = failureReason; this.nextRetryAt = nextRetryAt;
        this.deliveredAt = deliveredAt; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public static NotificationDelivery create(
            Long id,
            String templateCode,
            String correlationId,
            Integer templateVersion,
            RecipientType recipientType,
            String recipientId,
            NotificationChannel channel,
            String provider,
            Map<String, String> variables,
            String renderedSubject,
            String renderedBody) {
        return new NotificationDelivery(
                id,
                templateCode,
                correlationId,
                templateVersion,
                recipientType,
                recipientId,
                channel,
                provider,
                variables,
                renderedSubject,
                renderedBody
        );
    }

    public void markFailedAndScheduleRetry(String failureReason, ZonedDateTime nextRetryAt) {
        this.failureReason = failureReason;
        this.attemptCount++;
        this.nextRetryAt = nextRetryAt;
        this.status = DeliveryStatus.RETRYING;
        touch();
    }

    public void markDelivered(String providerMessageId) {
        this.status = DeliveryStatus.DELIVERED;
        this.providerMessageId = providerMessageId;
        this.deliveredAt = ZonedDateTime.now();
        touch();
    }

    public void markFailed(String failureReason) {
        this.status = DeliveryStatus.FAILED;
        this.failureReason = failureReason;
        this.attemptCount++;
        touch();
    }

    public void retry(ZonedDateTime nextRetryAt) {
        this.status = DeliveryStatus.RETRYING;
        this.nextRetryAt = nextRetryAt;
        this.attemptCount++;
        touch();
    }

    public void markPermanentlyFailed(String failureReason) {
        this.status = DeliveryStatus.PERMANENTLY_FAILED;
        this.failureReason = failureReason;
        this.attemptCount++;
        touch();
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
