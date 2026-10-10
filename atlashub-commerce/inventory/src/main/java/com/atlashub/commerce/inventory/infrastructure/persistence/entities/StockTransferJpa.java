package com.atlashub.commerce.inventory.infrastructure.persistence.entities;

import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
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
        name = "commerce_stock_transfers",
        indexes = {
                @Index(name = "Idx_comm_st_org_status", columnList = "organization_id, status"),
                @Index(name = "Idx_comm_st_source", columnList = "source_outlet_id"),
                @Index(name = "Idx_comm_st_dest", columnList = "destination_outlet_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StockTransferJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "source_outlet_id", nullable = false)
    private Long sourceOutletId;

    @Column(name = "destination_outlet_id", nullable = false)
    private Long destinationOutletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus status;

    @Column(name = "requested_at", nullable = false)
    private ZonedDateTime requestedAt;

    @Column(name = "received_at")
    private ZonedDateTime receivedAt;

    @Version
    private Long version;
}
