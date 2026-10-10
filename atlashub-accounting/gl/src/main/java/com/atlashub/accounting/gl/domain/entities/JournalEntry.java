package com.atlashub.accounting.gl.domain.entities;

import com.atlashub.accounting.gl.domain.events.JournalEntryPendingApprovalEvent;
import com.atlashub.accounting.gl.domain.events.JournalEntryPostedEvent;
import com.atlashub.accounting.gl.domain.exceptions.InvalidEntryStateException;
import com.atlashub.accounting.gl.domain.exceptions.JournalUnbalancedException;
import com.atlashub.accounting.gl.domain.exceptions.SelfApprovalNotAllowedException;
import com.atlashub.accounting.gl.domain.valueobject.EntrySource;
import com.atlashub.accounting.gl.domain.valueobject.EntryType;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class JournalEntry extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String entryNumber;
    private final LocalDate date;
    private final String reference;
    private final String description;
    private final List<JournalLine> lines;
    private JournalEntryStatus status;
    private final EntrySource source;
    private Long initiatedBy;
    private Long approvedBy;
    private ZonedDateTime approvedAt;
    private final ZonedDateTime createdAt;

    public JournalEntry(Long id,
                        Long organizationId,
                        String entryNumber,
                        LocalDate date,
                        String reference,
                        String description,
                        List<JournalLine> lines,
                        JournalEntryStatus status,
                        EntrySource source,
                        Long initiatedBy,
                        Long approvedBy,
                        ZonedDateTime approvedAt,
                        ZonedDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.entryNumber = entryNumber;
        this.date = date;
        this.reference = reference;
        this.description = description;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
        this.status = status;
        this.source = source;
        this.initiatedBy = initiatedBy;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt;
    }

    public static JournalEntry create(Long id,
                                      Long organizationId,
                                      String entryNumber,
                                      LocalDate date,
                                      String reference,
                                      String description,
                                      EntrySource source,
                                      Long initiatedBy) {
        Objects.requireNonNull(id, "JournalEntry ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(date, "Date must not be null");
        Objects.requireNonNull(source, "EntrySource must not be null");
        if (entryNumber == null || entryNumber.isBlank()) {
            throw new InvalidEntryStateException("Entry number must not be blank");
        }

        return new JournalEntry(
                id,
                organizationId,
                entryNumber.trim(),
                date,
                reference,
                description,
                new ArrayList<>(),
                JournalEntryStatus.DRAFT,
                source,
                initiatedBy,
                null,
                null,
                ZonedDateTime.now()
        );
    }

    public void addLine(Long lineId, Long accountId, Money amount, EntryType type) {
        if (this.status != JournalEntryStatus.DRAFT) {
            throw new InvalidEntryStateException("Cannot add lines to a non-draft journal entry: " + this.status);
        }
        JournalLine line = JournalLine.create(lineId, this.id, accountId, amount, type);
        this.lines.add(line);
    }

    public void submitForApproval(Long initiatorId) {
        if (this.status != JournalEntryStatus.DRAFT) {
            throw new InvalidEntryStateException("Cannot submit journal entry for approval in status: " + this.status);
        }
        if (this.lines.isEmpty()) {
            throw new InvalidEntryStateException("Cannot submit empty journal entry");
        }
        if (!isBalanced()) {
            throw new JournalUnbalancedException(calculateTotalDebits(), calculateTotalCredits());
        }

        this.initiatedBy = initiatorId;
        this.status = JournalEntryStatus.PENDING_APPROVAL;

        Money totalDebits = calculateTotalDebits();
        registerEvent(JournalEntryPendingApprovalEvent.of(
                this.id,
                this.organizationId,
                this.entryNumber,
                totalDebits.amount(),
                totalDebits.currency().name(),
                initiatorId
        ));
    }

    public void approve(Long approverId) {
        if (this.status != JournalEntryStatus.PENDING_APPROVAL) {
            throw new InvalidEntryStateException("Cannot approve journal entry with status: " + this.status);
        }
        if (approverId != null && approverId.equals(this.initiatedBy)) {
            throw new SelfApprovalNotAllowedException();
        }

        this.approvedBy = approverId;
        this.approvedAt = ZonedDateTime.now();
        this.status = JournalEntryStatus.POSTED;

        Money totalDebits = calculateTotalDebits();
        registerEvent(JournalEntryPostedEvent.of(
                this.id,
                this.organizationId,
                this.entryNumber,
                this.reference,
                totalDebits.amount(),
                totalDebits.currency().name()
        ));
    }

    public void post() {
        if (this.status != JournalEntryStatus.DRAFT) {
            throw new InvalidEntryStateException("Cannot post journal entry in status: " + this.status);
        }
        if (this.lines.isEmpty()) {
            throw new InvalidEntryStateException("Cannot post empty journal entry");
        }
        if (!isBalanced()) {
            throw new JournalUnbalancedException(calculateTotalDebits(), calculateTotalCredits());
        }

        this.status = JournalEntryStatus.POSTED;

        Money totalDebits = calculateTotalDebits();
        registerEvent(JournalEntryPostedEvent.of(
                this.id,
                this.organizationId,
                this.entryNumber,
                this.reference,
                totalDebits.amount(),
                totalDebits.currency().name()
        ));
    }

    public void voidEntry(Long voidedBy, String reason) {
        if (this.status != JournalEntryStatus.POSTED) {
            throw new InvalidEntryStateException("Only posted journal entries can be voided: " + this.status);
        }
        this.status = JournalEntryStatus.VOIDED;
    }

    public Money calculateTotalDebits() {
        if (this.lines.isEmpty()) {
            return Money.zero(CurrencyCode.NGN);
        }
        CurrencyCode currency = this.lines.get(0).getAmount().currency();
        Money total = Money.zero(currency);
        for (JournalLine line : this.lines) {
            if (line.getType() == EntryType.DEBIT) {
                total = total.add(line.getAmount());
            }
        }
        return total;
    }

    public Money calculateTotalCredits() {
        if (this.lines.isEmpty()) {
            return Money.zero(CurrencyCode.NGN);
        }
        CurrencyCode currency = this.lines.get(0).getAmount().currency();
        Money total = Money.zero(currency);
        for (JournalLine line : this.lines) {
            if (line.getType() == EntryType.CREDIT) {
                total = total.add(line.getAmount());
            }
        }
        return total;
    }

    public boolean isBalanced() {
        if (this.lines.isEmpty()) {
            return false;
        }
        Money debits = calculateTotalDebits();
        Money credits = calculateTotalCredits();
        return debits.compareTo(credits) == 0;
    }

    public List<JournalLine> getLines() {
        return Collections.unmodifiableList(this.lines);
    }

    @Override
    public Long getId() {
        return id;
    }
}
