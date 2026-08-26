package com.atlashub.identity.adapter.out.persistence.mapper;

import com.atlashub.identity.domain.model.SplitRecipient;
import com.atlashub.identity.adapter.out.persistence.entity.SplitRecipientJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SplitRecipientMapper {

    public SplitRecipientJpaEntity toEntity(SplitRecipient domain) {
        if (domain == null) return null;

        return new SplitRecipientJpaEntity(
                domain.getId(),
                domain.getOrganizationId(),
                domain.getBankCode(),
                domain.getAccountNumber(),
                domain.getAccountName(),
                domain.getDescription(),
                domain.isActive(),
                domain.getCreatedAt()
        );
    }

    public SplitRecipient toDomain(SplitRecipientJpaEntity entity) {
        if (entity == null) return null;

        return new SplitRecipient(
                entity.getId(),
                entity.getIntegration(),
                entity.getBankCode(),
                entity.getAccountNumber(),
                entity.getAccountName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }
}
