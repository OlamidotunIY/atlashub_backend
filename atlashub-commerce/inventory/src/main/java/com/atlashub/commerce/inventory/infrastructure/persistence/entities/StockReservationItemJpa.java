package com.atlashub.commerce.inventory.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "commerce_stock_reservation_items",
        indexes = {
                @Index(name = "Idx_comm_sri_res_id", columnList = "reservation_id"),
                @Index(name = "Idx_comm_sri_prod_id", columnList = "product_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StockReservationItemJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
