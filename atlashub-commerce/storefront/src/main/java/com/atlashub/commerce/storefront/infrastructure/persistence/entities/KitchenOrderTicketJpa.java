package com.atlashub.commerce.storefront.infrastructure.persistence.entities;

import com.atlashub.commerce.storefront.domain.valueobject.KotStatus;
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
        name = "commerce_kitchen_order_tickets",
        indexes = {
                @Index(name = "Idx_comm_kot_order_id", columnList = "sales_order_id"),
                @Index(name = "Idx_comm_kot_outlet_status", columnList = "outlet_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class KitchenOrderTicketJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "sales_order_id", nullable = false)
    private Long salesOrderId;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private KotStatus status;

    @Column(name = "sent_at", nullable = false)
    private ZonedDateTime sentAt;

    @Version
    private Long version;
}
