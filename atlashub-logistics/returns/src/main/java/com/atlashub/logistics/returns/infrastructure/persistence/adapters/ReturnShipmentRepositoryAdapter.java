package com.atlashub.logistics.returns.infrastructure.persistence.adapters;

import com.atlashub.logistics.returns.domain.entities.ReturnShipment;
import com.atlashub.logistics.returns.domain.repositories.ReturnShipmentRepository;
import com.atlashub.logistics.returns.infrastructure.persistence.entities.ReturnShipmentJpa;
import com.atlashub.logistics.returns.infrastructure.persistence.mappers.ReturnShipmentMapper;
import com.atlashub.logistics.returns.infrastructure.persistence.repositories.SpringDataReturnShipmentRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ReturnShipmentRepositoryAdapter
        extends JpaBaseRepository<ReturnShipment, ReturnShipmentJpa>
        implements ReturnShipmentRepository {

    private final SpringDataReturnShipmentRepository springDataRepo;

    public ReturnShipmentRepositoryAdapter(
            SpringDataReturnShipmentRepository springDataRepo,
            ReturnShipmentMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "returns_shipment_seq";
    }

    @Override
    public Optional<ReturnShipment> findByReturnTrackingNumber(String trackingNumber) {
        return springDataRepo.findByReturnTrackingNumber(trackingNumber).map(mapper::toDomain);
    }

    @Override
    public List<ReturnShipment> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ReturnShipment> findByOriginalShipmentId(Long originalShipmentId) {
        return springDataRepo.findByOriginalShipmentId(originalShipmentId).map(mapper::toDomain);
    }
}
