package com.atlashub.pay.ledger.domain.entities;

import com.atlashub.pay.ledger.domain.events.LedgerTransactionPostedEvent;
import com.atlashub.pay.ledger.domain.exceptions.UnbalancedLedgerTransactionException;
import com.atlashub.pay.ledger.domain.valueobject.EntryType;
import com.atlashub.pay.ledger.domain.valueobject.SourceSystem;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class LedgerTransaction extends AggregateRoot<Long> {
    
    private final Long id;
    private final Long organizationId;
    private final List<LedgerEntry> entries;
    private final SourceSystem sourceSystem;
    private final String sourceReferenceId;
    private final String description;
    private final CurrencyCode currency;
    private final ZonedDateTime postedAt;
    private final String reference;

    private LedgerTransaction(Long id, Long organizationId, List<LedgerEntry> entries, SourceSystem sourceSystem, String sourceReferenceId, String description, CurrencyCode currency, ZonedDateTime postedAt, String reference) {
        this.id = id;
        this.organizationId = organizationId;
        this.entries = entries;
        this.sourceSystem = sourceSystem;
        this.sourceReferenceId = sourceReferenceId;
        this.description = description;
        this.currency = currency;
        this.postedAt = postedAt;
        this.reference = reference;
    }

    public static LedgerTransaction create(Long id, Long organizationId, List<LedgerEntry> entries, SourceSystem sourceSystem, String sourceReferenceId, String description, CurrencyCode currency, ZonedDateTime postedAt, String reference) {
        if (id == null) {
            throw new IllegalArgumentException("LedgerTransaction id cannot be null");
        }

        LedgerTransaction tx = new LedgerTransaction(id, organizationId, entries, sourceSystem, sourceReferenceId, description, currency, postedAt, reference);
        tx.validateBalance();

        List<LedgerTransactionPostedEvent.EntryPayload> entryPayloads = entries.stream()
                .map(e -> new LedgerTransactionPostedEvent.EntryPayload(
                        e.getAccountId(),
                        e.getType().name(),
                        e.getAmount().amount(),
                        e.getRunningBalance().amount()
                ))
                .collect(Collectors.toList());

        tx.registerEvent(new LedgerTransactionPostedEvent(
                UUID.randomUUID().toString(),
                id,
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new LedgerTransactionPostedEvent.Payload(
                        id,
                        organizationId,
                        reference,
                        sourceSystem.name(),
                        sourceReferenceId,
                        description,
                        currency.name(),
                        postedAt,
                        entryPayloads
                )
        ));

        return tx;
    }

    private void validateBalance() {
        BigDecimal totalDebits = entries.stream()
            .filter(e -> e.getType() == EntryType.DEBIT)
            .map(e -> e.getAmount().amount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredits = entries.stream()
            .filter(e -> e.getType() == EntryType.CREDIT)
            .map(e -> e.getAmount().amount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new UnbalancedLedgerTransactionException(
                "Ledger transaction debits (" + totalDebits + ") do not equal credits (" + totalCredits + ")");
        }
    }

    @Override
    public Long getId() {
        return id;
    }
}
