package com.atlashub.accounting.gl.infrastructure.persistence.entities;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity(name = "AccountingBalanceSnapshotJpa")
@Table(
        name = "accounting_balance_snapshots",
        indexes = {
                @Index(name = "idx_snapshot_account_date", columnList = "account_id, snapshot_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BalanceSnapshotJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "balance_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAmount;

    @Column(name = "balance_currency", nullable = false, length = 3)
    private String balanceCurrency;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;
}
