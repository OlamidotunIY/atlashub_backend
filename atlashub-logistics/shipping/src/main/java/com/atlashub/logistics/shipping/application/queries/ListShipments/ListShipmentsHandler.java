package com.atlashub.logistics.shipping.application.queries.ListShipments;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.shared.application.usecase.Query;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListShipmentsHandler extends Query<ListShipmentsQuery, List<ShipmentSummaryResult>> {

    private final ShipmentRepository shipmentRepository;

    public ListShipmentsHandler(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentSummaryResult> execute(ListShipmentsQuery input) {
        List<Shipment> shipments = shipmentRepository.findByOrganizationId(input.organizationId());

        return shipments.stream()
                .filter(s -> input.status() == null || input.status().isBlank() || s.getStatus().name().equalsIgnoreCase(input.status()))
                .map(s -> new ShipmentSummaryResult(
                        s.getId(),
                        s.getOrganizationId(),
                        s.getOrderId(),
                        s.getTrackingNumber(),
                        s.getType().name(),
                        s.getServiceTier().name(),
                        s.getRouteType().name(),
                        s.getOriginAddress().city(),
                        s.getDestinationAddress().city(),
                        s.getCurrentMilestone().name(),
                        s.getStatus().name(),
                        s.getCustomerShippingFee(),
                        s.getDeliveryWindowStart(),
                        s.getDeliveryWindowEnd(),
                        s.getCreatedAt()
                ))
                .toList();
    }
}
