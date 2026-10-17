package com.atlashub.logistics.shipping.infrastructure.persistence.adapters;

import com.atlashub.logistics.shipping.domain.entities.ShipmentMilestoneLog;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentMilestoneLogRepository;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ShipmentMilestoneLogJpa;
import com.atlashub.logistics.shipping.infrastructure.persistence.mappers.ShipmentMilestoneLogMapper;
import com.atlashub.logistics.shipping.infrastructure.persistence.repositories.SpringDataShipmentMilestoneLogRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMilestoneLogRepositoryAdapter
        extends JpaBaseRepository<ShipmentMilestoneLog, ShipmentMilestoneLogJpa>
        implements ShipmentMilestoneLogRepository {

    private final SpringDataShipmentMilestoneLogRepository springDataRepo;

    public ShipmentMilestoneLogRepositoryAdapter(
            SpringDataShipmentMilestoneLogRepository springDataRepo,
            ShipmentMilestoneLogMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "logistics_milestone_log_seq";
    }

    @Override
    public List<ShipmentMilestoneLog> findByShipmentIdOrderByOccurredAtAsc(Long shipmentId) {
        return springDataRepo.findByShipmentIdOrderByOccurredAtAsc(shipmentId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ShipmentMilestoneLog> findByTrackingNumberOrderByOccurredAtAsc(String trackingNumber) {
        return springDataRepo.findByTrackingNumberOrderByOccurredAtAsc(trackingNumber).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
