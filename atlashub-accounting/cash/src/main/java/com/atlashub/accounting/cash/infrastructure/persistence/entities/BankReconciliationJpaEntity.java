package com.atlashub.accounting.cash.infrastructure.persistence.entities;

import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Entity(name = "AccountingBankReconciliationJpa")
@Table(name = "accounting_bank_reconciliations", indexes = {
        @Index(name = "idx_bank_reconciliation_org_status", columnList = "organization_id,status"),
        @Index(name = "idx_bank_reconciliation_account_date", columnList = "organization_id,bank_account_code,statement_date")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BankReconciliationJpaEntity implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "organization_id", nullable = false) private Long organizationId;
    @Column(name = "bank_account_code", nullable = false, length = 64) private String bankAccountCode;
    @Column(name = "statement_date", nullable = false) private LocalDate statementDate;
    @Column(name = "statement_closing_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal statementClosingBalance;
    @Column(name = "book_balance", nullable = false, precision = 19, scale = 4) private BigDecimal bookBalance;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal difference;
    @Column(nullable = false, length = 3) private String currency;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unmatched_book_item_ids", nullable = false, columnDefinition = "json")
    private List<Long> unmatchedBookItemIds;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16) private ReconciliationStatus status;
    @Column(name = "created_at", nullable = false) private ZonedDateTime createdAt;
    @Column(name = "reconciled_at") private ZonedDateTime reconciledAt;
    @Version private Long version;
}
