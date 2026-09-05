package com.atlashub.charges.adapter.out.persistence.mapper;

import com.atlashub.charges.adapter.out.persistence.entity.ExternalChargeJpaEntity;
import com.atlashub.charges.domain.model.ExternalCharge;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.atlashub.shared.domain.money.Money;
import org.springframework.stereotype.Component;

@Component
public class ExternalChargeMapper {
    public ExternalCharge toDomain(ExternalChargeJpaEntity entity) {
        return new ExternalCharge(
                entity.getId(),
                entity.getPurposeId(),
                entity.getOrganizationId(),
                entity.getReference(),
                entity.getCheckoutUrl(),
                new Money(
                        entity.getAmount(),
                        CurrencyCode.valueOf(entity.getCurrency())
                ),
                entity.getPurpose(),
                entity.getStatus(),
                entity.getCompletedAt(),
                entity.getCreatedAt()
        );
    }

    public ExternalChargeJpaEntity toEntity(ExternalCharge domain) {
        return new ExternalChargeJpaEntity(
                domain.getId(),
                domain.getPurposeId(),
                domain.getOrganizationId(),
                domain.getReference(),
                domain.getCheckoutUrl(),
                domain.getAmount().amount(),
                domain.getAmount().currency().name(),
                domain.getPurpose(),
                domain.getStatus(),
                domain.getCompletedAt(),
                domain.getCreatedAt(),
                null
        );
    }
}
