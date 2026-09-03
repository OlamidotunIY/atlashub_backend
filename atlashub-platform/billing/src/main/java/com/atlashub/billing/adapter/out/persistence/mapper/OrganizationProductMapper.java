package com.atlashub.billing.adapter.out.persistence.mapper;

import com.atlashub.billing.adapter.out.persistence.entity.OrganizationProductJpaEntity;
import com.atlashub.billing.domain.model.OrganizationProduct;
import org.springframework.stereotype.Component;

@Component
public class OrganizationProductMapper {
    public OrganizationProduct toDomain(OrganizationProductJpaEntity entity) {
        return new OrganizationProduct(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getProductId(),
                entity.getStatus(),
                entity.getBillingCycle(),
                entity.getCurrentPeriodStart(),
                entity.getCurrentPeriodEnd(),
                entity.getCanceledAt()
        );
    }

    public OrganizationProductJpaEntity toEntity(OrganizationProduct domain) {
        return new OrganizationProductJpaEntity(
                domain.getId(),
                domain.getOrganizationId(),
                domain.getProductId(),
                domain.getStatus(),
                domain.getCycle(),
                domain.getCurrentPeriodStart(),
                domain.getCurrentPeriodEnd(),
                domain.getCanceledAt(),
                null // Version handled by JPA
        );
    }
}
