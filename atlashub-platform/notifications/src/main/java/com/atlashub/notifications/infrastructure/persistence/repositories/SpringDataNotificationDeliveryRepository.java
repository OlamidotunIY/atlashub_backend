package com.atlashub.notifications.infrastructure.persistence.repositories;

import com.atlashub.notifications.domain.valueobject.DeliveryStatus;
import com.atlashub.notifications.infrastructure.persistence.entities.NotificationDeliveryJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataNotificationDeliveryRepository extends JpaRepository<NotificationDeliveryJpa, Long> {
    Optional<NotificationDeliveryJpa> findByCorrelationId(String correlationId);
    List<NotificationDeliveryJpa> findByStatusAndNextRetryAtBefore(DeliveryStatus status, ZonedDateTime now);
}
