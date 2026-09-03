package com.atlashub.catalog.adapter.out.entity;

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
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "product_pricings",
        indexes = {
                @Index(name = "idx_pricing_hub_product", columnList = "hub_product_id"),
                @Index(name = "idx_pricing_cycle_currency", columnList = "hub_product_id, billing_cycle, currency_code", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductPricingJpaEntity {

    @Id
    private Long id;

    @Column(name = "hub_product_id", nullable = false)
    private Long hubProductId;

    @Column(name = "billing_cycle", nullable = false)
    private String billingCycle;

    @Setter
    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Setter
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
