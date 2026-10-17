package com.atlashub.logistics.warehousing.domain.entities;

import com.atlashub.logistics.warehousing.domain.events.StockTransferApprovedEvent;
import com.atlashub.logistics.warehousing.domain.events.StockTransferCancelledEvent;
import com.atlashub.logistics.warehousing.domain.events.StockTransferDispatchedEvent;
import com.atlashub.logistics.warehousing.domain.events.StockTransferReceivedEvent;
import com.atlashub.logistics.warehousing.domain.events.StockTransferRequestedEvent;
import com.atlashub.logistics.warehousing.domain.exceptions.InvalidStockTransferStateException;
import com.atlashub.logistics.warehousing.domain.exceptions.WarehousingInvariantException;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.LocationType;
import com.atlashub.logistics.warehousing.domain.valueobject.StockTransferItem;
import com.atlashub.logistics.warehousing.domain.valueobject.TransferStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class StockTransfer extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long sourceId;
    private final LocationType sourceType;
    private final Address sourceAddress;
    private final Long destinationId;
    private final LocationType destinationType;
    private final Address destinationAddress;
    private final BigDecimal totalWeightKg;
    private final Money transferShippingFee;
    private TransferStatus status;
    private Long shipmentId;
    private final List<StockTransferItem> items;
    private final Long requestedBy;
    private Long approvedBy;
    private final ZonedDateTime requestedAt;
    private ZonedDateTime receivedAt;

    public StockTransfer(
            Long id,
            Long organizationId,
            Long sourceId,
            LocationType sourceType,
            Address sourceAddress,
            Long destinationId,
            LocationType destinationType,
            Address destinationAddress,
            BigDecimal totalWeightKg,
            Money transferShippingFee,
            TransferStatus status,
            Long shipmentId,
            List<StockTransferItem> items,
            Long requestedBy,
            Long approvedBy,
            ZonedDateTime requestedAt,
            ZonedDateTime receivedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.sourceAddress = sourceAddress;
        this.destinationId = destinationId;
        this.destinationType = destinationType;
        this.destinationAddress = destinationAddress;
        this.totalWeightKg = totalWeightKg;
        this.transferShippingFee = transferShippingFee;
        this.status = status;
        this.shipmentId = shipmentId;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.requestedBy = requestedBy;
        this.approvedBy = approvedBy;
        this.requestedAt = requestedAt;
        this.receivedAt = receivedAt;
    }

    public static StockTransfer create(
            Long id,
            Long organizationId,
            Long sourceId,
            LocationType sourceType,
            Address sourceAddress,
            Long destinationId,
            LocationType destinationType,
            Address destinationAddress,
            Money transferShippingFee,
            List<StockTransferItem> items,
            Long requestedBy,
            String correlationId
    ) {
        if (id == null) {
            throw new WarehousingInvariantException("Transfer ID cannot be null");
        }
        if (organizationId == null) {
            throw new WarehousingInvariantException("Organization ID cannot be null");
        }
        if (sourceId == null || destinationId == null) {
            throw new WarehousingInvariantException("Source and destination IDs are required");
        }
        if (sourceId.equals(destinationId) && sourceType == destinationType) {
            throw new WarehousingInvariantException("Source and destination cannot be the exact same location");
        }
        if (sourceAddress == null || destinationAddress == null) {
            throw new WarehousingInvariantException("Source and destination addresses are required");
        }
        if (items == null || items.isEmpty()) {
            throw new WarehousingInvariantException("Stock transfer must contain at least one item");
        }
        if (transferShippingFee == null) {
            throw new WarehousingInvariantException("Transfer shipping fee is required");
        }

        BigDecimal totalWeight = items.stream()
                .map(item -> item.weightKg().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ZonedDateTime now = ZonedDateTime.now();
        StockTransfer transfer = new StockTransfer(
                id,
                organizationId,
                sourceId,
                sourceType,
                sourceAddress,
                destinationId,
                destinationType,
                destinationAddress,
                totalWeight,
                transferShippingFee,
                TransferStatus.REQUESTED,
                null,
                items,
                requestedBy,
                null,
                now,
                null
        );

        transfer.registerEvent(StockTransferRequestedEvent.of(
                id,
                correlationId,
                new StockTransferRequestedEvent.Payload(
                        id,
                        organizationId,
                        sourceId,
                        sourceType.name(),
                        destinationId,
                        destinationType.name(),
                        totalWeight,
                        transferShippingFee,
                        requestedBy,
                        now
                )
        ));

        return transfer;
    }

    public void approve(Long approverId, Money declaredValue, String correlationId) {
        if (this.status != TransferStatus.REQUESTED) {
            throw new InvalidStockTransferStateException(
                    "Only REQUESTED transfers can be approved. Current status: " + this.status);
        }
        if (approverId == null) {
            throw new WarehousingInvariantException("Approver ID is required");
        }
        this.status = TransferStatus.APPROVED;
        this.approvedBy = approverId;

        ZonedDateTime now = ZonedDateTime.now();
        registerEvent(StockTransferApprovedEvent.of(
                this.id,
                correlationId,
                new StockTransferApprovedEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.sourceId,
                        this.sourceAddress,
                        this.destinationId,
                        this.destinationAddress,
                        this.totalWeightKg,
                        declaredValue,
                        this.requestedBy,
                        approverId,
                        now
                )
        ));
    }

    public void markDispatched(Long shipmentId, String correlationId) {
        if (this.status != TransferStatus.APPROVED) {
            throw new InvalidStockTransferStateException(
                    "Only APPROVED transfers can be dispatched. Current status: " + this.status);
        }
        if (shipmentId == null) {
            throw new WarehousingInvariantException("Shipment ID is required for dispatch");
        }
        this.status = TransferStatus.DISPATCHED;
        this.shipmentId = shipmentId;

        ZonedDateTime now = ZonedDateTime.now();
        registerEvent(StockTransferDispatchedEvent.of(
                this.id,
                correlationId,
                new StockTransferDispatchedEvent.Payload(
                        this.id,
                        this.organizationId,
                        shipmentId,
                        now
                )
        ));
    }

    public void receive(ZonedDateTime receivedAt, String correlationId) {
        if (this.status != TransferStatus.DISPATCHED) {
            throw new InvalidStockTransferStateException(
                    "Only DISPATCHED transfers can be received. Current status: " + this.status);
        }
        this.status = TransferStatus.RECEIVED;
        this.receivedAt = receivedAt != null ? receivedAt : ZonedDateTime.now();

        registerEvent(StockTransferReceivedEvent.of(
                this.id,
                correlationId,
                new StockTransferReceivedEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.destinationId,
                        this.receivedAt
                )
        ));
    }

    public void cancel(String reason, String correlationId) {
        if (this.status == TransferStatus.RECEIVED || this.status == TransferStatus.DISPATCHED) {
            throw new InvalidStockTransferStateException(
                    "Transfers in status " + this.status + " cannot be cancelled");
        }
        if (this.status == TransferStatus.CANCELLED) {
            throw new InvalidStockTransferStateException("Transfer is already cancelled");
        }
        this.status = TransferStatus.CANCELLED;

        ZonedDateTime now = ZonedDateTime.now();
        registerEvent(StockTransferCancelledEvent.of(
                this.id,
                correlationId,
                new StockTransferCancelledEvent.Payload(
                        this.id,
                        this.organizationId,
                        reason,
                        now
                )
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
