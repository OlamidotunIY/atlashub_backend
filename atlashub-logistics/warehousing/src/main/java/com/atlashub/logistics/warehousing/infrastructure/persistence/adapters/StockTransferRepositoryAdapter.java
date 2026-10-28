package com.atlashub.logistics.warehousing.infrastructure.persistence.adapters;

import com.atlashub.logistics.warehousing.domain.entities.StockTransfer;
import com.atlashub.logistics.warehousing.domain.repositories.StockTransferRepository;
import com.atlashub.logistics.warehousing.infrastructure.persistence.entities.StockTransferJpa;
import com.atlashub.logistics.warehousing.infrastructure.persistence.mappers.StockTransferMapper;
import com.atlashub.logistics.warehousing.infrastructure.persistence.repositories.SpringDataStockTransferRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component("warehousingStockTransferRepositoryAdapter")
public class StockTransferRepositoryAdapter
        extends JpaBaseRepository<StockTransfer, StockTransferJpa>
        implements StockTransferRepository {

    private final SpringDataStockTransferRepository springDataRepo;

    public StockTransferRepositoryAdapter(
            SpringDataStockTransferRepository springDataRepo,
            StockTransferMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "warehousing_transfer_seq";
    }

    @Override
    public Optional<StockTransfer> findByShipmentId(Long shipmentId) {
        return springDataRepo.findByShipmentId(shipmentId).map(mapper::toDomain);
    }

    @Override
    public List<StockTransfer> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<StockTransfer> findBySourceIdOrDestinationId(Long sourceId, Long destinationId) {
        return springDataRepo.findBySourceIdOrDestinationId(sourceId, destinationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
