package com.atlashub.commerce.inventory.infrastructure.persistence.entities;

import com.atlashub.commerce.inventory.domain.valueobject.StockCountStatus;
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

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "commerce_stock_counts",
        indexes = {
                @Index(name = "Idx_comm_sc_org_outlet", columnList = "organization_id, outlet_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StockCountJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StockCountStatus status;

    @Column(name = "started_at", nullable = false)
    private ZonedDateTime startedAt;

    @Column(name = "reconciled_at")
    private ZonedDateTime reconciledAt;

    @Version
    private Long version;
}
