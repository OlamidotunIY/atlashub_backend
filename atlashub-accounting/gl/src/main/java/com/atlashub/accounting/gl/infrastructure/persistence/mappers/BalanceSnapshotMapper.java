package com.atlashub.accounting.gl.infrastructure.persistence.mappers;

import com.atlashub.accounting.gl.domain.entities.BalanceSnapshot;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.BalanceSnapshotJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class BalanceSnapshotMapper implements DomainMapper<BalanceSnapshot, BalanceSnapshotJpa> {

    @Override
    public BalanceSnapshot toDomain(BalanceSnapshotJpa record) {
        if (record == null) {
            return null;
        }
        CurrencyCode currency = CurrencyCode.valueOf(record.getBalanceCurrency());
        Money balance = Money.of(record.getBalanceAmount(), currency);
        return new BalanceSnapshot(
                record.getId(),
                record.getAccountId(),
                record.getSnapshotDate(),
                balance,
                record.getCreatedAt()
        );
    }

    @Override
    public BalanceSnapshotJpa toPersistence(BalanceSnapshot domain) {
        if (domain == null) {
            return null;
        }
        return new BalanceSnapshotJpa(
                domain.getId(),
                domain.getAccountId(),
                domain.getSnapshotDate(),
                domain.getBalance().amount(),
                domain.getBalance().currency().name(),
                domain.getCreatedAt(),
                null
        );
    }

    @Override
    public void updatePersistence(BalanceSnapshot domain, BalanceSnapshotJpa record) {
        BalanceSnapshotJpa mapped = toPersistence(domain);
        BeanUtils.copyProperties(mapped, record, "id", "version");
    }
}
