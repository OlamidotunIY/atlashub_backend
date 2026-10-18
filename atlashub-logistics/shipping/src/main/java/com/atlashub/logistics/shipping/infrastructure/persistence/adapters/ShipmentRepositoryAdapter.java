package com.atlashub.logistics.shipping.infrastructure.persistence.adapters;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ShipmentJpa;
import com.atlashub.logistics.shipping.infrastructure.persistence.mappers.ShipmentMapper;
import com.atlashub.logistics.shipping.infrastructure.persistence.repositories.SpringDataShipmentRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ShipmentRepositoryAdapter
        extends JpaBaseRepository<Shipment, ShipmentJpa>
        implements ShipmentRepository {

    private final SpringDataShipmentRepository springDataRepo;

    public ShipmentRepositoryAdapter(
            SpringDataShipmentRepository springDataRepo,
            ShipmentMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "logistics_shipment_seq";
    }

    @Override
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return springDataRepo.findByTrackingNumber(trackingNumber).map(mapper::toDomain);
    }

    @Override
    public List<Shipment> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Shipment> findByConsolidationBatchId(Long batchId) {
        return springDataRepo.findByConsolidationBatchId(batchId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Shipment> findDeliveredOrdersForMonth(Long organizationId, ZonedDateTime start, ZonedDateTime end) {
        return springDataRepo.findDeliveredOrdersForMonth(organizationId, start, end).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
