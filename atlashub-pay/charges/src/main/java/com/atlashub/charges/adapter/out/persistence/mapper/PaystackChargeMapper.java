package com.atlashub.charges.adapter.out.persistence.mapper;

import com.atlashub.charges.adapter.out.persistence.entity.PaystackChargeJpaEntity;
import com.atlashub.charges.domain.model.PaystackCharge;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.atlashub.shared.domain.money.Money;
import org.springframework.stereotype.Component;

@Component
public class PaystackChargeMapper {
    public PaystackCharge toDomain(PaystackChargeJpaEntity entity) {
        return new PaystackCharge(
                entity.getId(),
                entity.getInvoiceId(),
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

    public PaystackChargeJpaEntity toEntity(PaystackCharge domain) {
        return new PaystackChargeJpaEntity(
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
