package com.atlashub.logistics.shipping.application.commands.CreateShipment;

import com.atlashub.logistics.shipping.domain.entities.CoverageZone;
import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.entities.ShipmentMilestoneLog;
import com.atlashub.logistics.shipping.domain.repositories.CoverageZoneRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentMilestoneLogRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentStatus;
import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateShipmentHandler extends Command<CreateShipmentCommand, CreateShipmentResult> {

    private final ShipmentRepository shipmentRepository;
    private final CoverageZoneRepository coverageZoneRepository;
    private final ShipmentMilestoneLogRepository milestoneLogRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public CreateShipmentHandler(
            ShipmentRepository shipmentRepository,
            CoverageZoneRepository coverageZoneRepository,
            ShipmentMilestoneLogRepository milestoneLogRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.coverageZoneRepository = coverageZoneRepository;
        this.milestoneLogRepository = milestoneLogRepository;
    }

    @Override
    @Transactional
    public CreateShipmentResult execute(CreateShipmentCommand input) {
        Long shipmentId = shipmentRepository.nextIdentity();
        String trackingNumber = "ATL-" + System.currentTimeMillis() + "-" + (100 + secureRandom.nextInt(900));

        boolean isIntraState = input.originAddress().state().trim().equalsIgnoreCase(input.destinationAddress().state().trim());
        RouteType routeType = isIntraState ? RouteType.INTRA_STATE : RouteType.INTER_STATE;

        CoverageZone zone = coverageZoneRepository.findByStateAndCity(
                input.destinationAddress().state(),
                input.destinationAddress().city()
        ).orElse(null);

        Money standardFee;
        Money urgentFee;
        int minHours;
        int maxHours;

        if (zone != null) {
            standardFee = zone.calculateStandardFee(input.weightKg());
            urgentFee = zone.calculateUrgentFee(input.weightKg());
            if (input.serviceTier() == DeliveryTier.URGENT) {
                minHours = zone.getUrgentMinHours();
                maxHours = zone.getUrgentMaxHours();
            } else if (isIntraState) {
                minHours = zone.getIntraStateStandardMinHours();
                maxHours = zone.getIntraStateStandardMaxHours();
            } else {
                minHours = zone.getInterStateStandardMinHours();
                maxHours = zone.getInterStateStandardMaxHours();
            }
        } else {
            com.atlashub.shared.domain.valueobject.CurrencyCode currency = input.declaredValue() != null ? input.declaredValue().currency() : com.atlashub.shared.domain.valueobject.CurrencyCode.NGN;
            BigDecimal weight = input.weightKg() != null && input.weightKg().compareTo(BigDecimal.ZERO) > 0 ? input.weightKg() : BigDecimal.ONE;
            standardFee = Money.of(new BigDecimal("1500.00"), currency).add(Money.of(new BigDecimal("300.00"), currency).multiply(weight));
            urgentFee = standardFee.multiply(new BigDecimal("2.50"));
            if (input.serviceTier() == DeliveryTier.URGENT) {
                minHours = 2;
                maxHours = 6;
            } else if (isIntraState) {
                minHours = 12;
                maxHours = 36;
            } else {
                minHours = 48;
                maxHours = 96;
            }
        }

        Money feeToPay = (input.serviceTier() == DeliveryTier.URGENT) ? urgentFee : standardFee;
        Money baselineCost = standardFee.multiply(new BigDecimal("0.70")); // baseline carrier transport cost

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime windowStart = now.plusHours(minHours);
        ZonedDateTime windowEnd = now.plusHours(maxHours);
        ZonedDateTime latestDispatch = (input.serviceTier() == DeliveryTier.URGENT)
                ? now.plusHours(1)
                : windowStart.minusHours(Math.min(4, minHours / 2));

        Shipment shipment = Shipment.create(
                shipmentId,
                input.organizationId(),
                input.orderId(),
                trackingNumber,
                input.type(),
                input.serviceTier(),
                input.originAddress(),
                input.destinationAddress(),
                input.senderId(),
                input.receiverId(),
                input.weightKg(),
                input.dimensions(),
                input.declaredValue(),
                feeToPay,
                baselineCost,
                windowStart,
                windowEnd,
                latestDispatch,
                input.correlationId()
        );

        shipmentRepository.save(shipment);

        Long milestoneLogId = milestoneLogRepository.nextIdentity();
        ShipmentMilestoneLog milestoneLog = ShipmentMilestoneLog.create(
                milestoneLogId,
                shipmentId,
                trackingNumber,
                routeType,
                TrackingMilestone.CREATED,
                ShipmentStatus.CREATED,
                input.originAddress().city() + ", " + input.originAddress().state(),
                input.originAddress().state(),
                "Shipment registered and scheduled for dispatch" + (input.serviceTier() == DeliveryTier.URGENT ? " (Urgent Delivery)" : ""),
                null,
                null
        );
        milestoneLogRepository.save(milestoneLog);

        return new CreateShipmentResult(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getDeliveryCode(),
                shipment.getRouteType().name(),
                shipment.getServiceTier().name(),
                shipment.getCustomerShippingFee(),
                shipment.getDeliveryWindowStart(),
                shipment.getDeliveryWindowEnd()
        );
    }
}
