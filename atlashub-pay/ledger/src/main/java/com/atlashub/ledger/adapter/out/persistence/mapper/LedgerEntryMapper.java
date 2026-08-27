package com.atlashub.ledger.adapter.out.persistence.mapper;

import com.atlashub.ledger.domain.valueobject.EntryType;
import com.atlashub.ledger.domain.model.LedgerEntry;
import com.atlashub.ledger.domain.model.TransactionReference;
import com.atlashub.ledger.adapter.out.persistence.entity.LedgerEntryJpaEntity;
import com.atlashub.shared.money.CurrencyCode;
import com.atlashub.shared.money.Money;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import com.atlashub.ledger.domain.valueobject.SourceSystem;

@Component
public class LedgerEntryMapper {

    public LedgerEntryJpaEntity toEntity(LedgerEntry domain) {
        if (domain == null) return null;
        
        return new LedgerEntryJpaEntity(
                domain.getId(),
                domain.getAccountId(),
                domain.getAmount().amount(),
                domain.getAmount().currency().name(),
                domain.getType().name(),
                domain.getDescription(),
                domain.getRunningBalance() != null ? domain.getRunningBalance().amount() : BigDecimal.ZERO,
                domain.getCreatedAt(),
                null
        );
    }

    public LedgerEntry toDomain(LedgerEntryJpaEntity entity) {
        if (entity == null) return null;
        
        TransactionReference reference = null;
        if (entity.getTransaction() != null) {
            reference = new TransactionReference(
                    entity.getTransaction().getTransactionId(),
                    SourceSystem.valueOf(entity.getTransaction().getSourceSystem())
            );
        }

        return new LedgerEntry(
                entity.getId(),
                entity.getAccountId(),
                Money.of(entity.getAmount(), CurrencyCode.valueOf(entity.getCurrency())),
                EntryType.valueOf(entity.getType()),
                reference,
                entity.getDescription(),
                Money.of(entity.getRunningBalance(), CurrencyCode.valueOf(entity.getCurrency())),
                entity.getCreatedAt()
        );
    }
}
