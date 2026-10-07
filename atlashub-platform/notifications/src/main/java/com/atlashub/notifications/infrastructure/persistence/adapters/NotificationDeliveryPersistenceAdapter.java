package com.atlashub.notifications.infrastructure.persistence.adapters;

import com.atlashub.notifications.domain.entities.NotificationDelivery;
import com.atlashub.notifications.domain.repositories.NotificationDeliveryRepository;
import com.atlashub.notifications.domain.valueobject.DeliveryStatus;
import com.atlashub.notifications.infrastructure.persistence.entities.NotificationDeliveryJpa;
import com.atlashub.notifications.infrastructure.persistence.mappers.NotificationDeliveryMapper;
import com.atlashub.notifications.infrastructure.persistence.repositories.SpringDataNotificationDeliveryRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class NotificationDeliveryPersistenceAdapter extends JpaBaseRepository<NotificationDelivery, NotificationDeliveryJpa> implements NotificationDeliveryRepository {
    private final SpringDataNotificationDeliveryRepository repository;

    public NotificationDeliveryPersistenceAdapter(SpringDataNotificationDeliveryRepository repository, NotificationDeliveryMapper mapper, DomainSequenceGenerator sequenceGenerator, DomainEventPublisher eventPublisher) {
        super(repository, mapper, sequenceGenerator, eventPublisher);
        this.repository = repository;
    }

    @Override
    protected String getSequenceName() {
        return "notification_delivery_seq";
    }

    @Override
    public Optional<NotificationDelivery> findByCorrelationId(String correlationId) {
        return repository.findByCorrelationId(correlationId).map(mapper::toDomain);
    }

    @Override
    public List<NotificationDelivery> findRetryDueBefore(ZonedDateTime now) {
        return repository.findByStatusAndNextRetryAtBefore(DeliveryStatus.RETRYING, now).stream().map(mapper::toDomain).toList();
    }
}
