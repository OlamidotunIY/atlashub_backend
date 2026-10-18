package com.atlashub.logistics.shipping.infrastructure.persistence.adapters;

import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ShipmentJpa;
import com.atlashub.logistics.shipping.infrastructure.persistence.repositories.SpringDataShipmentRepository;
import com.atlashub.shared.application.port.LogisticsShippingQueryPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LogisticsShippingQueryPortAdapter implements LogisticsShippingQueryPort {

    private final SpringDataShipmentRepository springDataRepo;

    public LogisticsShippingQueryPortAdapter(SpringDataShipmentRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<ShippingSnapshot> findShipmentById(Long shipmentId) {
        return springDataRepo.findById(shipmentId).map(this::toSnapshot);
    }

    @Override
    public Optional<ShippingSnapshot> findShipmentByTrackingNumber(String trackingNumber) {
        return springDataRepo.findByTrackingNumber(trackingNumber).map(this::toSnapshot);
    }

    private ShippingSnapshot toSnapshot(ShipmentJpa jpa) {
        return new ShippingSnapshot(
                jpa.getId(),
                jpa.getOrganizationId(),
                jpa.getOrderId(),
                jpa.getTrackingNumber(),
                jpa.getStatus().name(),
                jpa.getServiceTier().name(),
                jpa.getRouteType().name(),
                jpa.getCurrentMilestone() != null ? jpa.getCurrentMilestone().name() : null,
                jpa.getDeliveryCode(),
                jpa.getDeliveryWindowStart(),
                jpa.getDeliveryWindowEnd()
        );
    }
}
