package com.atlashub.logistics.shipping.domain.entities;

import com.atlashub.logistics.shipping.domain.exceptions.InvalidShipmentStatusException;
import com.atlashub.logistics.shipping.domain.valueobject.BatchStatus;
import com.atlashub.logistics.shipping.domain.valueobject.CarrierChannel;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class ConsolidationBatch extends AggregateRoot<Long> {

    private final Long id;
    private final String batchReference;
    private final String originState;
    private final String originCity;
    private final String destinationState;
    private final String destinationCity;
    private final RouteType routeType;
    private final List<Long> shipmentIds;
    private BigDecimal totalWeightKg;
    private ZonedDateTime latestDispatchAt;
    private Money totalBaselineCost;
    private Money actualBatchCost;
    private BatchStatus status;
    private CarrierChannel carrierChannel;
    private String carrierCode;
    private ZonedDateTime dispatchedAt;
    private final ZonedDateTime createdAt;

    public ConsolidationBatch(
            Long id,
            String batchReference,
            String originState,
            String originCity,
            String destinationState,
            String destinationCity,
            RouteType routeType,
            List<Long> shipmentIds,
            BigDecimal totalWeightKg,
            ZonedDateTime latestDispatchAt,
            Money totalBaselineCost,
            Money actualBatchCost,
            BatchStatus status,
            CarrierChannel carrierChannel,
            String carrierCode,
            ZonedDateTime dispatchedAt,
            ZonedDateTime createdAt
    ) {
        this.id = id;
        this.batchReference = batchReference;
        this.originState = originState;
        this.originCity = originCity;
        this.destinationState = destinationState;
        this.destinationCity = destinationCity;
        this.routeType = routeType;
        this.shipmentIds = shipmentIds != null ? new ArrayList<>(shipmentIds) : new ArrayList<>();
        this.totalWeightKg = totalWeightKg != null ? totalWeightKg : BigDecimal.ZERO;
        this.latestDispatchAt = latestDispatchAt;
        this.totalBaselineCost = totalBaselineCost;
        this.actualBatchCost = actualBatchCost;
        this.status = status;
        this.carrierChannel = carrierChannel;
        this.carrierCode = carrierCode;
        this.dispatchedAt = dispatchedAt;
        this.createdAt = createdAt;
    }

    public static ConsolidationBatch create(
            Long id,
            String batchReference,
            String originState,
            String originCity,
            String destinationState,
            String destinationCity,
            CurrencyCode currency
    ) {
        RouteType route = originState.trim().equalsIgnoreCase(destinationState.trim())
                ? RouteType.INTRA_STATE
                : RouteType.INTER_STATE;
        ZonedDateTime now = ZonedDateTime.now();
        return new ConsolidationBatch(
                id,
                batchReference,
                originState,
                originCity,
                destinationState,
                destinationCity,
                route,
                new ArrayList<>(),
                BigDecimal.ZERO,
                null,
                Money.zero(currency != null ? currency : CurrencyCode.NGN),
                null,
                BatchStatus.OPEN,
                null,
                null,
                null,
                now
        );
    }

    public void addShipment(
            Long shipmentId,
            BigDecimal weightKg,
            Money baselineCost,
            ZonedDateTime shipmentLatestDispatchAt
    ) {
        if (this.status != BatchStatus.OPEN) {
            throw new InvalidShipmentStatusException("Batch is not open for adding shipments");
        }
        if (!this.shipmentIds.contains(shipmentId)) {
            this.shipmentIds.add(shipmentId);
        }
        if (weightKg != null) {
            this.totalWeightKg = this.totalWeightKg.add(weightKg);
        }
        if (baselineCost != null) {
            this.totalBaselineCost = this.totalBaselineCost != null
                    ? this.totalBaselineCost.add(baselineCost)
                    : baselineCost;
        }
        if (shipmentLatestDispatchAt != null) {
            if (this.latestDispatchAt == null || shipmentLatestDispatchAt.isBefore(this.latestDispatchAt)) {
                this.latestDispatchAt = shipmentLatestDispatchAt;
            }
        }
    }

    public void dispatchBatch(
            CarrierChannel channel,
            String carrierCode,
            Money actualBatchCost,
            ZonedDateTime now
    ) {
        if (this.status != BatchStatus.OPEN) {
            throw new InvalidShipmentStatusException("Batch is not in OPEN status to dispatch");
        }
        if (this.shipmentIds.isEmpty()) {
            throw new InvalidShipmentStatusException("Cannot dispatch an empty consolidation batch");
        }
        this.carrierChannel = channel;
        this.carrierCode = carrierCode;
        this.actualBatchCost = actualBatchCost;
        this.dispatchedAt = now;
        this.status = BatchStatus.DISPATCHED;
    }

    public Money allocateCostForWeight(BigDecimal shipmentWeightKg) {
        if (this.actualBatchCost == null) {
            return Money.zero(this.totalBaselineCost != null ? this.totalBaselineCost.currency() : CurrencyCode.NGN);
        }
        if (this.shipmentIds.isEmpty()) {
            return this.actualBatchCost;
        }
        if (this.totalWeightKg.compareTo(BigDecimal.ZERO) <= 0 || shipmentWeightKg == null || shipmentWeightKg.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal split = BigDecimal.ONE.divide(BigDecimal.valueOf(this.shipmentIds.size()), 4, RoundingMode.HALF_UP);
            return this.actualBatchCost.multiply(split);
        }
        BigDecimal ratio = shipmentWeightKg.divide(this.totalWeightKg, 4, RoundingMode.HALF_UP);
        return this.actualBatchCost.multiply(ratio);
    }

    public Money allocateCostForShipment(BigDecimal shipmentWeightKg) {
        return allocateCostForWeight(shipmentWeightKg);
    }

    public List<Long> getShipmentIdsView() {
        return Collections.unmodifiableList(this.shipmentIds);
    }

    @Override
    public Long getId() {
        return id;
    }
}
