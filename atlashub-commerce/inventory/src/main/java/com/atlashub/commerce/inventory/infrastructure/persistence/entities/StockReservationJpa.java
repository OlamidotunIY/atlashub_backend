package com.atlashub.commerce.inventory.infrastructure.persistence.entities;

import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;
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
        name = "commerce_stock_reservations",
        indexes = {
                @Index(name = "Idx_comm_sr_sales_order", columnList = "sales_order_id", unique = true),
                @Index(name = "Idx_comm_sr_org_status", columnList = "organization_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StockReservationJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "sales_order_id", nullable = false)
    private Long salesOrderId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
