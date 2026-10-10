package com.atlashub.commerce.catalog.infrastructure.persistence.entities;

import com.atlashub.commerce.catalog.domain.valueobject.PriceLevel;
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

import java.math.BigDecimal;

@Entity
@Table(
        name = "commerce_product_prices",
        indexes = {
                @Index(name = "Idx_comm_price_prod_level", columnList = "product_id, variant_id, price_level", unique = true),
                @Index(name = "Idx_comm_price_prod_id", columnList = "product_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductPriceJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_level", nullable = false)
    private PriceLevel priceLevel;

    @Column(name = "cost_price")
    private BigDecimal costPrice;

    @Column(name = "selling_price", nullable = false)
    private BigDecimal sellingPrice;

    @Column(name = "markup")
    private BigDecimal markup;
}
