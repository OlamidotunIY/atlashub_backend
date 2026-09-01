package com.atlashub.identity.adapter.out.persistence.mapper;

import com.atlashub.identity.adapter.out.persistence.entity.InvitationJpaEntity;
import com.atlashub.identity.domain.model.Invitation;
import com.atlashub.identity.domain.valueobject.InvitationStatus;
import com.atlashub.identity.domain.valueobject.OrganizationRole;
import org.springframework.stereotype.Component;

@Component
public class InvitationMapper {

    public Invitation toDomain(InvitationJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Invitation.reconstitute(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getInvitedEmail(),
                entity.getInvitedByUserId(),
                OrganizationRole.valueOf(entity.getRole()),
                entity.getToken(),
                InvitationStatus.valueOf(entity.getStatus()),
                entity.getExpiresAt(),
                entity.getCreatedAt()
        );
    }

    public InvitationJpaEntity toEntity(Invitation domain) {
        if (domain == null) {
            return null;
        }

        InvitationJpaEntity entity = new InvitationJpaEntity(
                domain.getId(), domain.getOrganizationId(), domain.getInvitedEmail(), domain.getInvitedByUserId(), domain.getRole().name(), domain.getToken(), domain.getStatus().name(), domain.getExpiresAt(), domain.getCreatedAt()
        );

        return entity;
    }
}
