package com.atlashub.billing.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "billing_transaction_volume_ledgers",
        indexes = {
                @Index(name = "idx_ledger_org_month", columnList = "organization_id, ledger_month", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TransactionVolumeLedgerJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "ledger_month", nullable = false)
    private String month;

    @Column(name = "total_volume", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalVolume;

    @Column(name = "total_transaction_count", nullable = false)
    private Long totalTransactionCount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "last_updated_at", nullable = false)
    private ZonedDateTime lastUpdatedAt;

    @Version
    private Long version;
}
