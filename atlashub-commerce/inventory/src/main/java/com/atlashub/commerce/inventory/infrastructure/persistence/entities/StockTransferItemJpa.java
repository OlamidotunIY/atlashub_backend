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
        name = "commerce_stock_transfer_items",
        indexes = {
                @Index(name = "Idx_comm_sti_trans_id", columnList = "transfer_id"),
                @Index(name = "Idx_comm_sti_prod_id", columnList = "product_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StockTransferItemJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "transfer_id", nullable = false)
    private Long transferId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity_requested", nullable = false)
    private Integer quantityRequested;

    @Column(name = "quantity_received")
    private Integer quantityReceived;
}
