package com.atlashub.notifications.domain.repositories;

import com.atlashub.notifications.domain.entities.NotificationDelivery;
import com.atlashub.shared.domain.repository.Repository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationDeliveryRepository extends Repository<NotificationDelivery> {
    Optional<NotificationDelivery> findByCorrelationId(String correlationId);
    List<NotificationDelivery> findRetryDueBefore(ZonedDateTime now);
}
