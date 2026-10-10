package com.atlashub.commerce.storefront.infrastructure.persistence.entities;

import com.atlashub.commerce.storefront.domain.valueobject.TableStatus;
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

@Entity
@Table(
        name = "commerce_hospitality_tables",
        indexes = {
                @Index(name = "Idx_comm_ht_outlet_table", columnList = "outlet_id, table_number", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HospitalityTableJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "table_number", nullable = false)
    private String tableNumber;

    @Column(name = "covers", nullable = false)
    private Integer covers;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TableStatus status;

    @Column(name = "current_order_id")
    private Long currentOrderId;

    @Version
    private Long version;
}
