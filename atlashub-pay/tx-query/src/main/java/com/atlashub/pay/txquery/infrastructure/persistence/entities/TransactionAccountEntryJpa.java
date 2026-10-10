package com.atlashub.pay.txquery.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "pay_transaction_account_entries",
        uniqueConstraints = @UniqueConstraint(name = "Uk_pay_tx_entry_record_account_type",
                columnNames = {"transaction_record_id", "ledger_account_id", "entry_type"}),
        indexes = {
                @Index(name = "Idx_pay_tx_entry_record", columnList = "transaction_record_id"),
                @Index(name = "Idx_pay_tx_entry_account", columnList = "ledger_account_id,transaction_record_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TransactionAccountEntryJpa implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "transaction_record_id", nullable = false) private Long transactionRecordId;
    @Column(name = "ledger_account_id", nullable = false) private Long ledgerAccountId;
    @Column(name = "account_type") private String accountType;
    @Column(name = "account_name") private String accountName;
    @Column(name = "entry_type", nullable = false) private String entryType;
    @Column(nullable = false) private String direction;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal amount;
    @Column(nullable = false) private String currency;
}
