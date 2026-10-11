package com.atlashub.accounting.cash.domain.entities;

import com.atlashub.accounting.cash.domain.events.BankReconciliationCompletedEvent;
import com.atlashub.accounting.cash.domain.exceptions.InvalidReconciliationStateException;
import com.atlashub.accounting.cash.domain.exceptions.ReconciliationNotBalancedException;
import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class BankReconciliation extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String bankAccountCode;
    private final LocalDate statementDate;
    private final Money statementClosingBalance;
    private final Money bookBalance;
    private final Money difference;
    private final List<ReconciliationItem> matchedItems;
    private final List<BankStatementLine> unmatchedBankItems;
    private final List<Long> unmatchedBookItems;
    private ReconciliationStatus status;
    private final ZonedDateTime createdAt;
    private ZonedDateTime reconciledAt;

    public BankReconciliation(Long id,
                              Long organizationId,
                              String bankAccountCode,
                              LocalDate statementDate,
                              Money statementClosingBalance,
                              Money bookBalance,
                              Money difference,
                              List<ReconciliationItem> matchedItems,
                              List<BankStatementLine> unmatchedBankItems,
                              List<Long> unmatchedBookItems,
                              ReconciliationStatus status,
                              ZonedDateTime createdAt,
                              ZonedDateTime reconciledAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.bankAccountCode = bankAccountCode;
        this.statementDate = statementDate;
        this.statementClosingBalance = statementClosingBalance;
        this.bookBalance = bookBalance;
        this.difference = difference;
        this.matchedItems = matchedItems != null ? new ArrayList<>(matchedItems) : new ArrayList<>();
        this.unmatchedBankItems = unmatchedBankItems != null ? new ArrayList<>(unmatchedBankItems) : new ArrayList<>();
        this.unmatchedBookItems = unmatchedBookItems != null ? new ArrayList<>(unmatchedBookItems) : new ArrayList<>();
        this.status = status;
        this.createdAt = createdAt;
        this.reconciledAt = reconciledAt;
    }

    public static BankReconciliation create(Long id,
                                            Long organizationId,
                                            String bankAccountCode,
                                            LocalDate statementDate,
                                            Money statementClosingBalance,
                                            Money bookBalance,
                                            List<BankStatementLine> bankLines,
                                            List<Long> bookItems) {
        if (id == null) {
            throw new InvalidReconciliationStateException("Reconciliation ID must not be null");
        }
        if (organizationId == null) {
            throw new InvalidReconciliationStateException("Organization ID must not be null");
        }
        if (bankAccountCode == null || bankAccountCode.isBlank()) {
            throw new InvalidReconciliationStateException("Bank account code must not be blank");
        }
        if (statementDate == null) {
            throw new InvalidReconciliationStateException("Statement date must not be null");
        }
        if (statementClosingBalance == null) {
            throw new InvalidReconciliationStateException("Statement closing balance must not be null");
        }
        if (bookBalance == null) {
            throw new InvalidReconciliationStateException("Book balance must not be null");
        }
        if (!statementClosingBalance.currency().equals(bookBalance.currency())) {
            throw new InvalidReconciliationStateException("Statement closing balance and book balance currencies must match");
        }

        Money difference = statementClosingBalance.minus(bookBalance);
        ZonedDateTime now = ZonedDateTime.now();

        return new BankReconciliation(
                id,
                organizationId,
                bankAccountCode,
                statementDate,
                statementClosingBalance,
                bookBalance,
                difference,
                new ArrayList<>(),
                bankLines != null ? new ArrayList<>(bankLines) : new ArrayList<>(),
                bookItems != null ? new ArrayList<>(bookItems) : new ArrayList<>(),
                ReconciliationStatus.DRAFT,
                now,
                null
        );
    }

    public void matchItem(Long itemId, Long bankLineId, Long journalLineId) {
        if (this.status != ReconciliationStatus.DRAFT) {
            throw new InvalidReconciliationStateException("Cannot match items on a finalized reconciliation");
        }
        if (itemId == null || bankLineId == null || journalLineId == null) {
            throw new InvalidReconciliationStateException("Match item identifiers must not be null");
        }

        BankStatementLine bankLine = this.unmatchedBankItems.stream()
                .filter(line -> line.getId().equals(bankLineId))
                .findFirst()
                .orElseThrow(() -> new InvalidReconciliationStateException(
                        "Bank statement line not found in unmatched items: " + bankLineId));

        if (!this.unmatchedBookItems.contains(journalLineId)) {
            throw new InvalidReconciliationStateException(
                    "Journal line not found in unmatched items: " + journalLineId);
        }

        this.unmatchedBankItems.remove(bankLine);
        this.unmatchedBookItems.remove(journalLineId);
        this.matchedItems.add(ReconciliationItem.create(itemId, this.id, bankLineId, journalLineId));
    }

    public void finalizeReconciliation(boolean forceFinalize) {
        if (this.status != ReconciliationStatus.DRAFT) {
            throw new InvalidReconciliationStateException("Reconciliation is already finalized");
        }

        if (!this.difference.isZero() && !forceFinalize) {
            throw new ReconciliationNotBalancedException(this.difference);
        }

        this.status = ReconciliationStatus.RECONCILED;
        this.reconciledAt = ZonedDateTime.now();

        registerEvent(BankReconciliationCompletedEvent.of(
                this.id,
                this.organizationId,
                this.bankAccountCode,
                this.statementDate,
                this.statementClosingBalance.amount(),
                this.bookBalance.amount(),
                this.difference.amount(),
                this.statementClosingBalance.currency().name(),
                this.reconciledAt
        ));
    }

    public List<ReconciliationItem> getMatchedItems() {
        return Collections.unmodifiableList(matchedItems);
    }

    public List<BankStatementLine> getUnmatchedBankItems() {
        return Collections.unmodifiableList(unmatchedBankItems);
    }

    public List<Long> getUnmatchedBookItems() {
        return Collections.unmodifiableList(unmatchedBookItems);
    }

    @Override
    public Long getId() {
        return id;
    }
}
