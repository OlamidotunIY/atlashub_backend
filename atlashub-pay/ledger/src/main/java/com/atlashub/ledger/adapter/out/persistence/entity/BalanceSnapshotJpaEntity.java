package com.atlashub.ledger.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "balance_snapshots", indexes = {
        @Index(name = "idx_balance_account_id", columnList = "account_id", unique = true)
})
public class BalanceSnapshotJpaEntity {

    @Id
    private Long id;

    @Column(name = "account_id", nullable = false, unique = true)
    private Long accountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "last_ledger_entry_id")
    private Long lastLedgerEntryId;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;

    protected BalanceSnapshotJpaEntity() {}

    public BalanceSnapshotJpaEntity(Long id, Long accountId, BigDecimal balance, String currency, Long lastLedgerEntryId, ZonedDateTime createdAt, Long version) {
        this.id = id;
        this.accountId = accountId;
        this.balance = balance;
        this.currency = currency;
        this.lastLedgerEntryId = lastLedgerEntryId;
        this.createdAt = createdAt;
        this.version = version;
    }

    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }
    public Long getLastLedgerEntryId() { return lastLedgerEntryId; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public Long getVersion() { return version; }
    
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setLastLedgerEntryId(Long lastLedgerEntryId) { this.lastLedgerEntryId = lastLedgerEntryId; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}
