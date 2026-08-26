package com.atlashub.identity.adapter.out.persistence.mapper;

import com.atlashub.identity.domain.model.SubAccount;
import com.atlashub.identity.adapter.out.persistence.entity.SubAccountJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SubAccountMapper {

    public SubAccountJpaEntity toEntity(SubAccount domain) {
        if (domain == null) return null;

        return new SubAccountJpaEntity(
                domain.getId(),
                domain.getMerchantId(),
                domain.getBankCode(),
                domain.getAccountNumber(),
                domain.getAccountName(),
                domain.getDescription(),
                domain.isActive(),
                domain.getCreatedAt()
        );
    }

    public SubAccount toDomain(SubAccountJpaEntity entity) {
        if (entity == null) return null;

        return new SubAccount(
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
