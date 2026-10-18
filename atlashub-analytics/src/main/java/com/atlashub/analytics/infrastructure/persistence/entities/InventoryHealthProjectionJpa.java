package com.atlashub.analytics.infrastructure.persistence.entities;

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
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "analytics_inventory_health",
        indexes = {
                @Index(name = "idx_inventory_health_coverage", columnList = "organization_id, stock_coverage_days"),
                @Index(name = "idx_inventory_health_unique", columnList = "organization_id, environment, outlet_id, product_id", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InventoryHealthProjectionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "current_stock", nullable = false)
    private int currentStock;

    @Column(name = "reorder_point", nullable = false)
    private int reorderPoint;

    @Column(name = "stock_coverage_days", nullable = false)
    private int stockCoverageDays;

    @Column(name = "inventory_value", precision = 19, scale = 4)
    private BigDecimal inventoryValue;

    @Column(name = "dead_stock", nullable = false)
    private boolean deadStock;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
