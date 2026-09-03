package com.atlashub.billing.adapter.out.persistence.mapper;

import com.atlashub.billing.adapter.out.persistence.entity.BillingInvoiceJpaEntity;
import com.atlashub.billing.domain.model.BillingInvoice;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.atlashub.shared.domain.money.Money;
import org.springframework.stereotype.Component;

@Component
public class BillingInvoiceMapper {
    public BillingInvoice toDomain(BillingInvoiceJpaEntity entity) {
        return new BillingInvoice(
                entity.getId(),
                entity.getOrganizationProductId(),
                Money.of(entity.getAmount(), CurrencyCode.valueOf(entity.getCurrency())),
                entity.getStatus(),
                entity.getDueDate(),
                entity.getPaidAt()
        );
    }

    public BillingInvoiceJpaEntity toEntity(BillingInvoice domain) {
        return new BillingInvoiceJpaEntity(
                domain.getId(),
                domain.getOrganizationProductId(),
                domain.getAmount().amount(),
                domain.getAmount().currency().name(),
                domain.getStatus(),
                domain.getDueDate(),
                domain.getPaidAt(),
                null // Version handles by JPA
        );
    }
}
