package com.atlashub.logistics.shipping.infrastructure.persistence.entities;

import com.atlashub.logistics.shipping.domain.valueobject.BatchStatus;
import com.atlashub.logistics.shipping.domain.valueobject.CarrierChannel;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "logistics_consolidation_batches",
        indexes = {
                @Index(name = "idx_batch_ref", columnList = "batch_reference", unique = true),
                @Index(name = "idx_batch_route_status", columnList = "origin_state, destination_state, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ConsolidationBatchJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "batch_reference", nullable = false, unique = true, length = 64)
    private String batchReference;

    @Column(name = "origin_state", nullable = false, length = 64)
    private String originState;

    @Column(name = "origin_city", nullable = false, length = 64)
    private String originCity;

    @Column(name = "destination_state", nullable = false, length = 64)
    private String destinationState;

    @Column(name = "destination_city", nullable = false, length = 64)
    private String destinationCity;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_type", nullable = false, length = 32)
    private RouteType routeType;

    @Column(name = "shipment_ids_json", columnDefinition = "json")
    private String shipmentIdsJson;

    @Column(name = "total_weight_kg", precision = 10, scale = 2)
    private BigDecimal totalWeightKg;

    @Column(name = "latest_dispatch_at")
    private ZonedDateTime latestDispatchAt;

    @Column(name = "total_baseline_cost_amount", precision = 19, scale = 4)
    private BigDecimal totalBaselineCostAmount;

    @Column(name = "total_baseline_cost_currency", length = 3)
    private String totalBaselineCostCurrency;

    @Column(name = "actual_batch_cost_amount", precision = 19, scale = 4)
    private BigDecimal actualBatchCostAmount;

    @Column(name = "actual_batch_cost_currency", length = 3)
    private String actualBatchCostCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private BatchStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier_channel", length = 32)
    private CarrierChannel carrierChannel;

    @Column(name = "carrier_code", length = 32)
    private String carrierCode;

    @Column(name = "dispatched_at")
    private ZonedDateTime dispatchedAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;
}
