package com.atlashub.identity.adapter.out.persistence.mapper;

import com.atlashub.identity.adapter.out.persistence.entity.OrganizationJpaEntity;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.valueobject.ComplianceStatus;
import com.atlashub.identity.domain.valueobject.ComplianceStep;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public OrganizationJpaEntity toEntity(Organization domain) {
        if (domain == null) return null;

        return new OrganizationJpaEntity(
                domain.getId(),
                domain.getBusinessName(),
                domain.getBusinessType(),
                domain.getBusinessSize(),
                domain.getCurrency(),
                domain.getDescription(),
                domain.getLogoUrl(),
                domain.getComplianceStatus(),
                domain.getComplianceStep() != null ? domain.getComplianceStep().name() : null,
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public Organization toDomain(OrganizationJpaEntity entity) {
        if (entity == null) return null;

        return new Organization(
                entity.getId(),
                entity.getBusinessName(),
                entity.getBusinessType(),
                entity.getBusinessSize(),
                entity.getDescription(),
                entity.getCurrency(),
                entity.getLogoUrl(),
                entity.getComplianceStatus() != null ? entity.getComplianceStatus() : ComplianceStatus.NOT_STARTED,
                entity.getComplianceStep() != null ? ComplianceStep.valueOf(entity.getComplianceStep()) : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
