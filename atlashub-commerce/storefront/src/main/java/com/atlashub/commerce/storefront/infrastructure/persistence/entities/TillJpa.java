package com.atlashub.commerce.storefront.infrastructure.persistence.entities;

import com.atlashub.commerce.storefront.domain.valueobject.TillStatus;
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

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "commerce_tills",
        indexes = {
                @Index(name = "Idx_comm_till_outlet_status", columnList = "outlet_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TillJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "opening_float", nullable = false)
    private BigDecimal openingFloat;

    @Column(name = "expected_closing_balance", nullable = false)
    private BigDecimal expectedClosingBalance;

    @Column(name = "actual_closing_balance")
    private BigDecimal actualClosingBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TillStatus status;

    @Column(name = "opened_at", nullable = false)
    private ZonedDateTime openedAt;

    @Column(name = "closed_at")
    private ZonedDateTime closedAt;

    @Column(name = "opened_by", nullable = false)
    private Long openedBy;

    @Column(name = "closed_by")
    private Long closedBy;

    @Version
    private Long version;
}
