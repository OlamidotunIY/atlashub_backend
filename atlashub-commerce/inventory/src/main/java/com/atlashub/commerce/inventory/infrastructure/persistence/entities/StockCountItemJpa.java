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
        name = "commerce_stock_count_items",
        indexes = {
                @Index(name = "Idx_comm_sci_sc_id", columnList = "stock_count_id"),
                @Index(name = "Idx_comm_sci_inv_id", columnList = "inventory_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StockCountItemJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "stock_count_id", nullable = false)
    private Long stockCountId;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "system_qty", nullable = false)
    private Integer systemQty;

    @Column(name = "counted_qty")
    private Integer countedQty;
}
