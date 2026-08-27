package com.atlashub.identity.adapter.out.persistence.mapper;

import com.atlashub.identity.adapter.out.persistence.entity.OrganizationMemberJpaEntity;
import com.atlashub.identity.domain.model.OrganizationMember;
import com.atlashub.identity.domain.valueobject.MemberStatus;
import com.atlashub.identity.domain.valueobject.OrganizationRole;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMemberMapper {

    public OrganizationMember toDomain(OrganizationMemberJpaEntity entity) {
        if (entity == null) return null;

        return OrganizationMember.reconstitute(
            entity.getId(),
            entity.getOrganizationId(),
            entity.getUserId(),
            OrganizationRole.valueOf(entity.getRole()),
            MemberStatus.valueOf(entity.getStatus()),
            entity.getJoinedAt()
        );
    }

    public OrganizationMemberJpaEntity toEntity(OrganizationMember domain) {
        if (domain == null) return null;

        return new OrganizationMemberJpaEntity(
            domain.getId(),
            domain.getOrganizationId(),
            domain.getUserId(),
            domain.getRole().name(),
            domain.getStatus().name(),
            domain.getJoinedAt()
        );
    }
}
