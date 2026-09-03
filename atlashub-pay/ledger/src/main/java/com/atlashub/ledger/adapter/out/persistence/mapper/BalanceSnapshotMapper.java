package com.atlashub.ledger.adapter.out.persistence.mapper;

import com.atlashub.ledger.domain.model.BalanceSnapshot;
import com.atlashub.ledger.adapter.out.persistence.entity.BalanceSnapshotJpaEntity;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.atlashub.shared.domain.money.Money;
import org.springframework.stereotype.Component;

@Component
public class BalanceSnapshotMapper {

    public BalanceSnapshotJpaEntity toEntity(BalanceSnapshot domain) {
        if (domain == null) return null;

        return new BalanceSnapshotJpaEntity(
                domain.getId(),
                domain.getAccountId(),
                domain.getBalance().amount(),
                domain.getBalance().currency().name(),
                domain.getLastLedgerEntryId(),
                domain.getSnapshotAt(),
                null
        );
    }

    public BalanceSnapshot toDomain(BalanceSnapshotJpaEntity entity) {
        if (entity == null) return null;

        return new BalanceSnapshot(
                entity.getId(),
                entity.getAccountId(),
                Money.of(entity.getBalance(), CurrencyCode.valueOf(entity.getCurrency())),
                entity.getLastLedgerEntryId(),
                entity.getCreatedAt()
        );
    }
}
