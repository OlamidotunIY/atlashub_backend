package com.atlashub.pay.ledger.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "ledger_transactions",
        indexes = {
                @Index(name = "Idx_ledger_tx_org_id", columnList = "organization_id"),
                @Index(name = "Idx_ledger_tx_ref", columnList = "reference", unique = true),
                @Index(name = "Idx_ledger_tx_source", columnList = "source_system, source_reference_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LedgerTransactionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "source_reference_id", nullable = false)
    private String sourceReferenceId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String currency;

    @Column(name = "posted_at", nullable = false)
    private ZonedDateTime postedAt;

    @Column(nullable = false)
    private String reference;
}
