package com.atlashub.accounting.gl.infrastructure.persistence.mappers;

import com.atlashub.accounting.gl.domain.entities.JournalLine;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalLineJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

@Component
public class JournalLineMapper {

    public JournalLine toDomain(JournalLineJpa jpa) {
        if (jpa == null) {
            return null;
        }
        CurrencyCode currency = CurrencyCode.valueOf(jpa.getCurrency());
        Money amount = Money.of(jpa.getAmount(), currency);
        return new JournalLine(
                jpa.getId(),
                jpa.getJournalEntryId(),
                jpa.getAccountId(),
                amount,
                jpa.getType()
        );
    }

    public JournalLineJpa toPersistence(JournalLine domain) {
        if (domain == null) {
            return null;
        }
        return new JournalLineJpa(
                domain.getId(),
                domain.getJournalEntryId(),
                domain.getAccountId(),
                domain.getAmount().amount(),
                domain.getAmount().currency().name(),
                domain.getType(),
                null
        );
    }
}
