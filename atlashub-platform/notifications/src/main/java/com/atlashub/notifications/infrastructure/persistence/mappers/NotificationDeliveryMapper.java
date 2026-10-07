package com.atlashub.notifications.infrastructure.persistence.mappers;

import com.atlashub.notifications.domain.entities.NotificationDelivery;
import com.atlashub.notifications.infrastructure.persistence.entities.NotificationDeliveryJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationDeliveryMapper implements DomainMapper<NotificationDelivery, NotificationDeliveryJpa> {
    @Override
    public NotificationDelivery toDomain(NotificationDeliveryJpa r) {
        return new NotificationDelivery(r.getId(), r.getTemplateCode(), r.getCorrelationId(), r.getTemplateVersion(), r.getRecipientType(), r.getRecipientId(), r.getChannel(), r.getProvider(), Map.of(), r.getRenderedSubject(), r.getRenderedBody(), r.getStatus(), r.getAttemptCount(), r.getProviderMessageId(), r.getFailureReason(), r.getNextRetryAt(), r.getDeliveredAt(), r.getCreatedAt(), r.getUpdatedAt());
    }

    @Override
    public NotificationDeliveryJpa toPersistence(NotificationDelivery d) {
        return new NotificationDeliveryJpa(d.getId(), d.getTemplateCode(), d.getCorrelationId(), d.getTemplateVersion(), d.getRecipientType(), d.getRecipientId(), d.getChannel(), d.getProvider(), d.getRenderedSubject(), d.getRenderedBody(), d.getStatus(), d.getAttemptCount(), d.getProviderMessageId(), d.getFailureReason(), d.getNextRetryAt(), d.getDeliveredAt(), d.getCreatedAt(), d.getUpdatedAt(), null);
    }

    @Override
    public void updatePersistence(NotificationDelivery domain, NotificationDeliveryJpa record) {
        BeanUtils.copyProperties(toPersistence(domain), record, "id", "version");
    }
}
