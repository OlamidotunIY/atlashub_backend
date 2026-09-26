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
    private String templateCode;
    private Integer templateVersion;
    private RecipientType recipientType;
    private String recipientId;
    private NotificationChannel channel;
    private String provider;
    private Map<String, String> variables;
    private String renderedSubject;
    private String renderedBody;
    private DeliveryStatus status;
    private Integer attemptCount;
    private String providerMessageId;
    private String failureReason;
    private ZonedDateTime nextRetryAt;
    private ZonedDateTime deliveredAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    protected NotificationDelivery() {
        this.id = null;
    }

    private NotificationDelivery(
            Long id,
            String templateCode,
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

    public static NotificationDelivery create(
            String templateCode,
            Integer templateVersion,
            RecipientType recipientType,
            String recipientId,
            NotificationChannel channel,
            String provider,
            Map<String, String> variables,
            String renderedSubject,
            String renderedBody) {
        return new NotificationDelivery(
                null,
                templateCode,
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
