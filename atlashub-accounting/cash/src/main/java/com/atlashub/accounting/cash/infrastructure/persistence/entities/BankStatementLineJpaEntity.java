package com.atlashub.accounting.cash.infrastructure.persistence.entities;

import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AccountingBankStatementLineJpa")
@Table(name = "accounting_bank_statement_lines", indexes = {
        @Index(name = "idx_bank_statement_line_reconciliation", columnList = "reconciliation_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BankStatementLineJpaEntity implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "reconciliation_id", nullable = false) private Long reconciliationId;
    @Column(name = "transaction_date", nullable = false) private LocalDate transactionDate;
    @Column private String description;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal amount;
    @Column(nullable = false, length = 3) private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8) private StatementEntryType type;
}
