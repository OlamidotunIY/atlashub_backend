package com.atlashub.accounting.cash.infrastructure.persistence.entities;

import com.atlashub.accounting.cash.domain.valueobject.EvacuationStatus;
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
import java.time.ZonedDateTime;

@Entity(name = "AccountingCashEvacuationJpa")
@Table(name = "accounting_cash_evacuations", indexes = {
        @Index(name = "idx_cash_evacuation_org_status", columnList = "organization_id,status"),
        @Index(name = "idx_cash_evacuation_till", columnList = "till_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CashEvacuationJpaEntity implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "organization_id", nullable = false) private Long organizationId;
    @Column(name = "outlet_id", nullable = false) private Long outletId;
    @Column(name = "till_id", nullable = false) private Long tillId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal amount;
    @Column(nullable = false, length = 3) private String currency;
    @Column(name = "evacuated_by", nullable = false) private Long evacuatedBy;
    @Column(name = "evacuated_at", nullable = false) private ZonedDateTime evacuatedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16) private EvacuationStatus status;
    @Column(name = "confirmed_at") private ZonedDateTime confirmedAt;
}
