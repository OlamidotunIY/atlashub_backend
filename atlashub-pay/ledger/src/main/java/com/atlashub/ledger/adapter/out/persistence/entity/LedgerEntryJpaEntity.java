package com.atlashub.ledger.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntryJpaEntity {

    @Id
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 10)
    private String type;

    @Column(nullable = false)
    private String description;

    @Column(name = "running_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal runningBalance;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_transaction_id", nullable = false)
    private LedgerTransactionJpaEntity transaction;

    protected LedgerEntryJpaEntity() {}

    public LedgerEntryJpaEntity(Long id, Long accountId, BigDecimal amount, String currency, String type, String description, BigDecimal runningBalance, ZonedDateTime createdAt, LedgerTransactionJpaEntity transaction) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.type = type;
        this.description = description;
        this.runningBalance = runningBalance;
        this.createdAt = createdAt;
        this.transaction = transaction;
    }

    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public BigDecimal getRunningBalance() { return runningBalance; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public LedgerTransactionJpaEntity getTransaction() { return transaction; }
    public void setTransaction(LedgerTransactionJpaEntity transaction) { this.transaction = transaction; }
}
