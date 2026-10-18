package com.atlashub.logistics.shipping.domain.entities;

import com.atlashub.logistics.shipping.domain.events.ShipmentAssignedEvent;
import com.atlashub.logistics.shipping.domain.events.ShipmentCreatedEvent;
import com.atlashub.logistics.shipping.domain.events.ShipmentDeliveredEvent;
import com.atlashub.logistics.shipping.domain.events.ShipmentDeliveryAttemptFailedEvent;
import com.atlashub.logistics.shipping.domain.events.ShipmentDispatchedEvent;
import com.atlashub.logistics.shipping.domain.events.ShipmentFailedEvent;
import com.atlashub.logistics.shipping.domain.events.ShipmentLocationUpdatedEvent;
import com.atlashub.logistics.shipping.domain.events.ShipmentMilestoneUpdatedEvent;
import com.atlashub.logistics.shipping.domain.events.ShipmentReturnInitiatedEvent;
import com.atlashub.logistics.shipping.domain.exceptions.InsufficientDeliveryAttemptsException;
import com.atlashub.logistics.shipping.domain.exceptions.InvalidDeliveryCodeException;
import com.atlashub.logistics.shipping.domain.exceptions.InvalidMilestoneTransitionException;
import com.atlashub.logistics.shipping.domain.exceptions.InvalidShipmentStatusException;
import com.atlashub.logistics.shipping.domain.exceptions.ProofOfDeliveryRequiredException;
import com.atlashub.logistics.shipping.domain.exceptions.UrgentShipmentBatchingForbiddenException;
import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.CarrierChannel;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryFaultType;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentStatus;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class Shipment extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long orderId;
    private final String trackingNumber;
    private final ShipmentType type;
    private final DeliveryTier serviceTier;
    private final RouteType routeType;
    private CarrierChannel fulfillmentChannel;
    private final Address originAddress;
    private final Address destinationAddress;
    private final Long senderId;
    private final Long receiverId;
    private final BigDecimal weightKg;
    private final Dimensions dimensions;
    private final Money declaredValue;
    private final Money customerShippingFee;
    private final Money baselineSingleCost;
    private Money allocatedActualCost;
    private Long consolidationBatchId;
    private final String deliveryCode;
    private final ZonedDateTime deliveryWindowStart;
    private final ZonedDateTime deliveryWindowEnd;
    private final ZonedDateTime latestDispatchAt;
    private TrackingMilestone currentMilestone;
    private ShipmentStatus status;
    private int deliveryAttempts;
    private DeliveryFaultType failureFaultType;
    private String failureReason;
    private Long riderId;
    private Long vehicleId;
    private String carrier3PLCode;
    private String carrier3PLReference;
    private String proofOfDeliveryUrl;
    private String currentLocation;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private boolean incentiveEligible;
    private ZonedDateTime dispatchedAt;
    private ZonedDateTime deliveredAt;
    private final ZonedDateTime createdAt;

    public Shipment(
            Long id,
            Long organizationId,
            Long orderId,
            String trackingNumber,
            ShipmentType type,
            DeliveryTier serviceTier,
            RouteType routeType,
            CarrierChannel fulfillmentChannel,
            Address originAddress,
            Address destinationAddress,
            Long senderId,
            Long receiverId,
            BigDecimal weightKg,
            Dimensions dimensions,
            Money declaredValue,
            Money customerShippingFee,
            Money baselineSingleCost,
            Money allocatedActualCost,
            Long consolidationBatchId,
            String deliveryCode,
            ZonedDateTime deliveryWindowStart,
            ZonedDateTime deliveryWindowEnd,
            ZonedDateTime latestDispatchAt,
            TrackingMilestone currentMilestone,
            ShipmentStatus status,
            int deliveryAttempts,
            DeliveryFaultType failureFaultType,
            String failureReason,
            Long riderId,
            Long vehicleId,
            String carrier3PLCode,
            String carrier3PLReference,
            String proofOfDeliveryUrl,
            String currentLocation,
            BigDecimal currentLatitude,
            BigDecimal currentLongitude,
            boolean incentiveEligible,
            ZonedDateTime dispatchedAt,
            ZonedDateTime deliveredAt,
            ZonedDateTime createdAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.type = type;
        this.serviceTier = serviceTier;
        this.routeType = routeType;
        this.fulfillmentChannel = fulfillmentChannel;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.weightKg = weightKg;
        this.dimensions = dimensions;
        this.declaredValue = declaredValue;
        this.customerShippingFee = customerShippingFee;
        this.baselineSingleCost = baselineSingleCost;
        this.allocatedActualCost = allocatedActualCost;
        this.consolidationBatchId = consolidationBatchId;
        this.deliveryCode = deliveryCode;
        this.deliveryWindowStart = deliveryWindowStart;
        this.deliveryWindowEnd = deliveryWindowEnd;
        this.latestDispatchAt = latestDispatchAt;
        this.currentMilestone = currentMilestone;
        this.status = status;
        this.deliveryAttempts = deliveryAttempts;
        this.failureFaultType = failureFaultType;
        this.failureReason = failureReason;
        this.riderId = riderId;
        this.vehicleId = vehicleId;
        this.carrier3PLCode = carrier3PLCode;
        this.carrier3PLReference = carrier3PLReference;
        this.proofOfDeliveryUrl = proofOfDeliveryUrl;
        this.currentLocation = currentLocation;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.incentiveEligible = incentiveEligible;
        this.dispatchedAt = dispatchedAt;
        this.deliveredAt = deliveredAt;
        this.createdAt = createdAt;
    }

    public static Shipment create(
            Long id,
            Long organizationId,
            Long orderId,
            String trackingNumber,
            ShipmentType type,
            DeliveryTier serviceTier,
            Address originAddress,
            Address destinationAddress,
            Long senderId,
            Long receiverId,
            BigDecimal weightKg,
            Dimensions dimensions,
            Money declaredValue,
            Money customerShippingFee,
            Money baselineSingleCost,
            ZonedDateTime deliveryWindowStart,
            ZonedDateTime deliveryWindowEnd,
            ZonedDateTime latestDispatchAt,
            String correlationId
    ) {
        if (deliveryWindowEnd.isBefore(deliveryWindowStart)) {
            throw new InvalidShipmentStatusException("deliveryWindowEnd cannot be before deliveryWindowStart");
        }
        RouteType route = originAddress.state().trim().equalsIgnoreCase(destinationAddress.state().trim())
                ? RouteType.INTRA_STATE
                : RouteType.INTER_STATE;
        String generatedCode = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        boolean eligible = (type == ShipmentType.CUSTOMER_ORDER);
        ZonedDateTime now = ZonedDateTime.now();

        Shipment shipment = new Shipment(
                id,
                organizationId,
                orderId,
                trackingNumber,
                type,
                serviceTier,
                route,
                null,
                originAddress,
                destinationAddress,
                senderId,
                receiverId,
                weightKg,
                dimensions,
                declaredValue,
                customerShippingFee,
                baselineSingleCost,
                null,
                null,
                generatedCode,
                deliveryWindowStart,
                deliveryWindowEnd,
                latestDispatchAt,
                TrackingMilestone.CREATED,
                ShipmentStatus.CREATED,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                originAddress.city() + ", " + originAddress.state(),
                null,
                null,
                eligible,
                null,
                null,
                now
        );

        shipment.registerEvent(ShipmentCreatedEvent.of(
                id,
                correlationId,
                new ShipmentCreatedEvent.Payload(
                        id,
                        organizationId,
                        orderId,
                        trackingNumber,
                        type.name(),
                        serviceTier.name(),
                        route.name(),
                        weightKg,
                        customerShippingFee,
                        deliveryWindowStart,
                        deliveryWindowEnd,
                        latestDispatchAt,
                        now
                )
        ));

        return shipment;
    }

    public void assignToBatch(Long batchId) {
        if (this.serviceTier == DeliveryTier.URGENT) {
            throw new UrgentShipmentBatchingForbiddenException();
        }
        if (this.status != ShipmentStatus.CREATED) {
            throw new InvalidShipmentStatusException("Shipment must be in CREATED status to assign to batch");
        }
        this.consolidationBatchId = batchId;
        this.status = ShipmentStatus.BATCHED;
    }

    public void assignInternalRider(Long riderId, Long vehicleId, String correlationId) {
        if (this.status != ShipmentStatus.CREATED && this.status != ShipmentStatus.BATCHED) {
            throw new InvalidShipmentStatusException("Shipment must be CREATED or BATCHED to assign internal rider");
        }
        this.fulfillmentChannel = CarrierChannel.INTERNAL_FLEET;
        this.riderId = riderId;
        this.vehicleId = vehicleId;
        this.status = ShipmentStatus.ASSIGNED;
        if (this.routeType == RouteType.INTRA_STATE) {
            this.currentMilestone = TrackingMilestone.ASSIGNED_TO_RIDER;
        }

        registerEvent(ShipmentAssignedEvent.of(
                this.id,
                correlationId,
                new ShipmentAssignedEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.trackingNumber,
                        CarrierChannel.INTERNAL_FLEET.name(),
                        riderId,
                        vehicleId,
                        null,
                        null,
                        ZonedDateTime.now()
                )
        ));
    }

    public void route3PL(String carrierCode, String carrierReference, Money actualCost, String correlationId) {
        if (this.status != ShipmentStatus.CREATED && this.status != ShipmentStatus.BATCHED) {
            throw new InvalidShipmentStatusException("Shipment must be CREATED or BATCHED to route to 3PL carrier");
        }
        this.fulfillmentChannel = CarrierChannel.PARTNER_3PL;
        this.carrier3PLCode = carrierCode;
        this.carrier3PLReference = carrierReference;
        this.allocatedActualCost = actualCost;
        this.status = ShipmentStatus.ASSIGNED;

        registerEvent(ShipmentAssignedEvent.of(
                this.id,
                correlationId,
                new ShipmentAssignedEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.trackingNumber,
                        CarrierChannel.PARTNER_3PL.name(),
                        null,
                        null,
                        carrierCode,
                        carrierReference,
                        ZonedDateTime.now()
                )
        ));
    }

    public void setAllocatedCost(Money allocatedCost) {
        this.allocatedActualCost = allocatedCost;
    }

    public void advanceMilestone(
            TrackingMilestone nextMilestone,
            String locationName,
            String state,
            String description,
            BigDecimal latitude,
            BigDecimal longitude,
            ZonedDateTime now,
            String correlationId
    ) {
        if (this.status == ShipmentStatus.DELIVERED || this.status == ShipmentStatus.FAILED) {
            throw new InvalidMilestoneTransitionException("Cannot advance milestone for delivered or failed shipment");
        }

        if (nextMilestone == TrackingMilestone.PICKED_UP_FROM_STORE && this.dispatchedAt == null) {
            this.dispatchedAt = now;
            this.status = ShipmentStatus.IN_TRANSIT;
            registerEvent(ShipmentDispatchedEvent.of(
                    this.id,
                    correlationId,
                    new ShipmentDispatchedEvent.Payload(
                            this.id,
                            this.organizationId,
                            this.trackingNumber,
                            this.fulfillmentChannel != null ? this.fulfillmentChannel.name() : CarrierChannel.INTERNAL_FLEET.name(),
                            this.originAddress.city(),
                            this.destinationAddress.city(),
                            now
                    )
            ));
        } else if (this.status == ShipmentStatus.ASSIGNED) {
            this.status = ShipmentStatus.IN_TRANSIT;
        }

        this.currentMilestone = nextMilestone;
        this.currentLocation = locationName;
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;

        registerEvent(ShipmentMilestoneUpdatedEvent.of(
                this.id,
                correlationId,
                new ShipmentMilestoneUpdatedEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.trackingNumber,
                        this.routeType.name(),
                        nextMilestone.name(),
                        this.status.name(),
                        locationName,
                        state,
                        description,
                        latitude,
                        longitude,
                        now
                )
        ));
    }

    public void updateLiveLocation(
            String locationName,
            BigDecimal latitude,
            BigDecimal longitude,
            ZonedDateTime now,
            String correlationId
    ) {
        if (this.status != ShipmentStatus.IN_TRANSIT) {
            throw new InvalidShipmentStatusException("Shipment must be IN_TRANSIT to update live GPS location");
        }
        this.currentLocation = locationName;
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;

        registerEvent(ShipmentLocationUpdatedEvent.of(
                this.id,
                correlationId,
                new ShipmentLocationUpdatedEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.trackingNumber,
                        locationName,
                        latitude,
                        longitude,
                        now
                )
        ));
    }

    public void completeDelivery(String providedCode, String podUrl, ZonedDateTime now, String correlationId) {
        if (this.status != ShipmentStatus.IN_TRANSIT && this.status != ShipmentStatus.ASSIGNED) {
            throw new InvalidShipmentStatusException("Shipment must be active to complete delivery");
        }
        if (podUrl == null || podUrl.isBlank()) {
            throw new ProofOfDeliveryRequiredException();
        }
        if (this.deliveryCode == null || !this.deliveryCode.equals(providedCode)) {
            throw new InvalidDeliveryCodeException();
        }

        this.proofOfDeliveryUrl = podUrl;
        this.status = ShipmentStatus.DELIVERED;
        this.currentMilestone = TrackingMilestone.DELIVERED;
        this.deliveredAt = now;

        registerEvent(ShipmentDeliveredEvent.of(
                this.id,
                correlationId,
                new ShipmentDeliveredEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.orderId,
                        this.trackingNumber,
                        this.type.name(),
                        this.serviceTier.name(),
                        podUrl,
                        this.customerShippingFee,
                        this.baselineSingleCost,
                        this.allocatedActualCost,
                        this.incentiveEligible,
                        now
                )
        ));
    }

    public void recordFailedAttempt(String reason, ZonedDateTime now, String correlationId) {
        if (this.status != ShipmentStatus.IN_TRANSIT && this.status != ShipmentStatus.ASSIGNED) {
            throw new InvalidShipmentStatusException("Shipment must be in transit to record a failed attempt");
        }
        this.deliveryAttempts++;
        registerEvent(ShipmentDeliveryAttemptFailedEvent.of(
                this.id,
                correlationId,
                new ShipmentDeliveryAttemptFailedEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.trackingNumber,
                        this.deliveryAttempts,
                        reason,
                        now
                )
        ));
    }

    public void markFailed(DeliveryFaultType faultType, String reason, ZonedDateTime now, String correlationId) {
        if (this.status == ShipmentStatus.DELIVERED) {
            throw new InvalidShipmentStatusException("Cannot mark delivered shipment as failed");
        }
        if (faultType == DeliveryFaultType.CUSTOMER_FAULT && this.deliveryAttempts < 2) {
            throw new InsufficientDeliveryAttemptsException(this.deliveryAttempts);
        }
        if (faultType == DeliveryFaultType.MERCHANT_FAULT) {
            this.incentiveEligible = false;
        }

        this.status = ShipmentStatus.FAILED;
        this.failureFaultType = faultType;
        this.failureReason = reason;

        registerEvent(ShipmentFailedEvent.of(
                this.id,
                correlationId,
                new ShipmentFailedEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.orderId,
                        this.trackingNumber,
                        faultType.name(),
                        reason,
                        this.deliveryAttempts,
                        now
                )
        ));
    }

    public void initiateReturn(String reason, ZonedDateTime now, String correlationId) {
        if (this.status != ShipmentStatus.FAILED) {
            throw new InvalidShipmentStatusException("Only failed shipments can initiate return");
        }
        this.status = ShipmentStatus.RETURNING;

        registerEvent(ShipmentReturnInitiatedEvent.of(
                this.id,
                correlationId,
                new ShipmentReturnInitiatedEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.trackingNumber,
                        reason,
                        now
                )
        ));
    }

    public Money netCostSavings() {
        if (!incentiveEligible || allocatedActualCost == null || status != ShipmentStatus.DELIVERED) {
            return Money.zero(customerShippingFee.currency());
        }
        if (baselineSingleCost != null && baselineSingleCost.isGreaterThan(allocatedActualCost)) {
            return baselineSingleCost.subtract(allocatedActualCost);
        }
        return Money.zero(customerShippingFee.currency());
    }

    @Override
    public Long getId() {
        return id;
    }
}
