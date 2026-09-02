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

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "hub_products",
        indexes = {
                @Index(name = "idx_hub_product_key", columnList = "product_key", unique = true),
                @Index(name = "idx_hub_product_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HubProductJpaEntity {

    @Id
    private Long id;

    @Column(name = "product_key", nullable = false)
    private String productKey;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String description;

    @Setter
    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Setter
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
