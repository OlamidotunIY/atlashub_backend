package com.atlashub.pay.txquery.domain.entities;

import com.atlashub.pay.txquery.domain.exceptions.InvalidTransactionRecordException;
import com.atlashub.pay.txquery.domain.valueobject.TransactionDirection;
import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.pay.txquery.domain.valueobject.TransactionType;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Getter
public class TransactionRecord extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private final ApiEnvironment environment;
    private TransactionType type;
    private TransactionStatus status;
    private final Money amount;
    private Money fee;
    private Money netAmount;
    private String channel;
    private String provider;
    private final String reference;
    private String sourceSystem;
    private String sourceReferenceId;
    private String partyType;
    private String partyReferenceId;
    private Long outletId;
    private String recipientName;
    private String recipientAccountNumber;
    private String description;
    private final Map<String, String> metadata;
    private List<TransactionAccountEntry> accountEntries;
    private final ZonedDateTime createdAt;
    private ZonedDateTime completedAt;
    private ZonedDateTime updatedAt;

    public TransactionRecord(Long id, Long organizationId, ApiEnvironment environment, TransactionType type,
                             TransactionStatus status, Money amount, Money fee, Money netAmount, String channel,
                             String provider, String reference, String sourceSystem, String sourceReferenceId,
                             String partyType, String partyReferenceId, Long outletId, String recipientName,
                             String recipientAccountNumber, String description, Map<String, String> metadata,
                             List<TransactionAccountEntry> accountEntries, ZonedDateTime createdAt,
                             ZonedDateTime completedAt, ZonedDateTime updatedAt) {
        if (id == null || organizationId == null || environment == null || type == null || status == null ||
                amount == null || reference == null || reference.isBlank() || createdAt == null || updatedAt == null) {
            throw new InvalidTransactionRecordException("Complete transaction record data is required");
        }
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.type = type;
        this.status = status;
        this.amount = amount;
        this.fee = fee;
        this.netAmount = netAmount == null ? amount : netAmount;
        this.channel = channel;
        this.provider = provider;
        this.reference = reference.trim();
        this.sourceSystem = sourceSystem;
        this.sourceReferenceId = sourceReferenceId;
        this.partyType = partyType;
        this.partyReferenceId = partyReferenceId;
        this.outletId = outletId;
        this.recipientName = recipientName;
        this.recipientAccountNumber = recipientAccountNumber;
        this.description = description;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        this.accountEntries = accountEntries == null ? List.of() : List.copyOf(accountEntries);
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt;
    }

    public static TransactionRecord create(Long id, Long organizationId, ApiEnvironment environment,
                                           TransactionType type, TransactionStatus status, Money amount, String channel,
                                           String provider, String reference, String sourceSystem,
                                           String sourceReferenceId, String partyType, String partyReferenceId,
                                           Long outletId, String recipientName, String recipientAccountNumber,
                                           String description, Map<String, String> metadata, ZonedDateTime occurredAt) {
        ZonedDateTime at = occurredAt == null ? ZonedDateTime.now() : occurredAt;
        return new TransactionRecord(id, organizationId, environment, type, status, amount, null, amount, channel,
                provider, reference, sourceSystem, sourceReferenceId, partyType, partyReferenceId, outletId,
                recipientName, recipientAccountNumber, description, metadata, List.of(), at,
                terminal(status) ? at : null, at);
    }

    public void updateStatus(TransactionStatus nextStatus, ZonedDateTime occurredAt, String description) {
        this.status = nextStatus;
        this.updatedAt = occurredAt == null ? ZonedDateTime.now() : occurredAt;
        if (terminal(nextStatus)) this.completedAt = this.updatedAt;
        if (description != null && !description.isBlank()) this.description = description;
    }

    public void enrich(TransactionType transactionType, String channel, String provider, String sourceSystem,
                       String sourceReferenceId, String partyType, String partyReferenceId, Long outletId,
                       String recipientName, String recipientAccountNumber) {
        if (transactionType != null && transactionType != TransactionType.LEDGER_POST) this.type = transactionType;
        if (channel != null) this.channel = channel;
        if (provider != null) this.provider = provider;
        if (sourceSystem != null) this.sourceSystem = sourceSystem;
        if (sourceReferenceId != null) this.sourceReferenceId = sourceReferenceId;
        if (partyReferenceId != null) {
            this.partyType = partyType;
            this.partyReferenceId = partyReferenceId;
        }
        if (outletId != null) this.outletId = outletId;
        if (recipientName != null) this.recipientName = recipientName;
        if (recipientAccountNumber != null) this.recipientAccountNumber = recipientAccountNumber;
        this.updatedAt = ZonedDateTime.now();
    }

    public void applyAmounts(Money fee, Money netAmount) {
        this.fee = fee;
        this.netAmount = netAmount == null ? amount : netAmount;
        this.updatedAt = ZonedDateTime.now();
    }

    public void attachAccountEntries(List<TransactionAccountEntry> entries) {
        this.accountEntries = entries == null ? List.of() : List.copyOf(entries);
        this.updatedAt = ZonedDateTime.now();
    }

    public TransactionDirection primaryDirection() {
        if (accountEntries.isEmpty()) return TransactionDirection.INTERNAL;
        return accountEntries.getFirst().getDirection();
    }

    private static boolean terminal(TransactionStatus status) {
        return status == TransactionStatus.SUCCESSFUL || status == TransactionStatus.FAILED ||
                status == TransactionStatus.REFUNDED || status == TransactionStatus.REVERSED;
    }

    @Override
    public Long getId() {
        return id;
    }
}
