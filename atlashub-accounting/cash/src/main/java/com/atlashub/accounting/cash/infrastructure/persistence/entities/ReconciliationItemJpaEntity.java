package com.atlashub.accounting.cash.infrastructure.persistence.entities;

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
import lombok.Setter;

@Entity(name = "AccountingReconciliationItemJpa")
@Table(name = "accounting_reconciliation_items", indexes = {
        @Index(name = "idx_reconciliation_item_reconciliation", columnList = "reconciliation_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReconciliationItemJpaEntity implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "reconciliation_id", nullable = false) private Long reconciliationId;
    @Column(name = "bank_statement_line_id", nullable = false) private Long bankStatementLineId;
    @Column(name = "journal_line_id", nullable = false) private Long journalLineId;
}
