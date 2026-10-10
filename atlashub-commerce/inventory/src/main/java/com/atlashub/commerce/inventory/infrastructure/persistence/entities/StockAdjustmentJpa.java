package com.atlashub.commerce.inventory.infrastructure.persistence.entities;

import com.atlashub.commerce.inventory.domain.valueobject.AdjustmentReason;
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
        name = "commerce_stock_adjustments",
        indexes = {
                @Index(name = "Idx_comm_sa_org_inv", columnList = "organization_id, inventory_id"),
                @Index(name = "Idx_comm_sa_created", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StockAdjustmentJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "adjusted_by")
    private Long adjustedBy;

    @Column(name = "previous_qty", nullable = false)
    private Integer previousQty;

    @Column(name = "new_qty", nullable = false)
    private Integer newQty;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private AdjustmentReason reason;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;
}
