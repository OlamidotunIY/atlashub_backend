package com.atlashub.ledger.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ledger_transactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"transaction_id", "source_system"})
})
public class LedgerTransactionJpaEntity {

    @Id
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "posted_at", nullable = false)
    private ZonedDateTime postedAt;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LedgerEntryJpaEntity> entries = new ArrayList<>();

    protected LedgerTransactionJpaEntity() {}

    public LedgerTransactionJpaEntity(Long id, String transactionId, String sourceSystem, ZonedDateTime postedAt, List<LedgerEntryJpaEntity> entries) {
        this.id = id;
        this.transactionId = transactionId;
        this.sourceSystem = sourceSystem;
        this.postedAt = postedAt;
        this.entries = entries != null ? entries : new ArrayList<>();
    }

    public Long getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public String getSourceSystem() { return sourceSystem; }
    public ZonedDateTime getPostedAt() { return postedAt; }
    public List<LedgerEntryJpaEntity> getEntries() { return entries; }
}
