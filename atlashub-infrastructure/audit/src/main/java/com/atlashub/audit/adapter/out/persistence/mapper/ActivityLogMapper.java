package com.atlashub.audit.adapter.out.persistence.mapper;

import com.atlashub.audit.adapter.out.persistence.entity.ActivityLogJpaEntity;
import com.atlashub.audit.domain.model.ActivityLog;
import com.atlashub.audit.domain.valueobject.ActivityAction;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogMapper {

    public ActivityLog toDomain(ActivityLogJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return ActivityLog.reconstitute(
            entity.getId(),
            entity.getOrganizationId(),
            entity.getActorUserId(),
            ActivityAction.valueOf(entity.getAction()),
            entity.getEntityType(),
            entity.getEntityId(),
            entity.getMetadata(),
            entity.getOccurredAt()
        );
    }

    public ActivityLogJpaEntity toEntity(ActivityLog domain) {
        if (domain == null) {
            return null;
        }

        return new ActivityLogJpaEntity(
            domain.getId(),
            domain.getOrganizationId(),
            domain.getActorUserId(),
            domain.getAction().name(),
            domain.getEntityType(),
            domain.getEntityId(),
            domain.getMetadata(),
            domain.getOccurredAt()
        );
    }
}
