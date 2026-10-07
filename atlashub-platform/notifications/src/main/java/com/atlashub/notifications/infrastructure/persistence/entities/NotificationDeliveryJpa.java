package com.atlashub.notifications.infrastructure.persistence.entities;

import com.atlashub.notifications.domain.valueobject.DeliveryStatus;
import com.atlashub.notifications.domain.valueobject.NotificationChannel;
import com.atlashub.notifications.domain.valueobject.RecipientType;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Entity
@Table(name = "notification_deliveries", indexes = {
        @Index(name = "idx_notification_delivery_correlation", columnList = "correlation_id", unique = true),
        @Index(name = "idx_notification_delivery_retry", columnList = "status,next_retry_at")
})
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
public class NotificationDeliveryJpa implements BaseJpaEntity {
    @Id private Long id;
    @Column(nullable = false) private String templateCode;
    @Column(name = "correlation_id", nullable = false, unique = true) private String correlationId;
    @Column(nullable = false) private Integer templateVersion;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RecipientType recipientType;
    @Column(nullable = false) private String recipientId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationChannel channel;
    @Column(nullable = false) private String provider;
    @Column(length = 500) private String renderedSubject;
    @Column(columnDefinition = "LONGTEXT", nullable = false) private String renderedBody;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private DeliveryStatus status;
    @Column(nullable = false) private Integer attemptCount;
    private String providerMessageId;
    @Column(length = 1000) private String failureReason;
    private ZonedDateTime nextRetryAt;
    private ZonedDateTime deliveredAt;
    @Column(nullable = false) private ZonedDateTime createdAt;
    @Column(nullable = false) private ZonedDateTime updatedAt;
    @Version private Long version;
}
