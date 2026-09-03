package com.atlashub.ledger.adapter.out.persistence.mapper;

import com.atlashub.ledger.adapter.out.persistence.entity.WalletChargeJpaEntity;
import com.atlashub.ledger.domain.model.WalletCharge;
import org.springframework.stereotype.Component;

@Component
public class WalletChargeMapper {
    public WalletCharge toDomain(WalletChargeJpaEntity entity) {
        return new WalletCharge(
                entity.getId(),
                entity.getInvoiceId(),
                entity.getOrganizationId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getLedgerTransactionId(),
                entity.getStatus(),
                entity.getFailureReason()
        );
    }
    public WalletChargeJpaEntity toEntity(WalletCharge domain) {
        return new WalletChargeJpaEntity(
                domain.getId(),
                domain.getInvoiceId(),
                domain.getOrganizationId(),
                domain.getAmount(),
                domain.getCurrency(),
                domain.getLedgerTransactionId(),
                domain.getStatus(),
                domain.getFailureReason()
        );
    }
}
