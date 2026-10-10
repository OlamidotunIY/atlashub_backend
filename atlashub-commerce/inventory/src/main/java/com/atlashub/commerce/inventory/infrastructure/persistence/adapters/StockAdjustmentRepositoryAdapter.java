package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.StockAdjustment;
import com.atlashub.commerce.inventory.domain.repositories.StockAdjustmentRepository;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockAdjustmentJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockAdjustmentMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockAdjustmentRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockAdjustmentRepositoryAdapter
        extends JpaBaseRepository<StockAdjustment, StockAdjustmentJpa>
        implements StockAdjustmentRepository {

    private final SpringDataStockAdjustmentRepository springDataRepo;

    public StockAdjustmentRepositoryAdapter(SpringDataStockAdjustmentRepository springDataRepo,
                                           StockAdjustmentMapper mapper,
                                           DomainSequenceGenerator sequenceGenerator,
                                           DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "commerce_stock_adjustment_seq";
    }

    @Override
    public List<StockAdjustment> findByOrganizationIdAndInventoryId(Long orgId, Long inventoryId) {
        return springDataRepo.findByOrganizationIdAndInventoryId(orgId, inventoryId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
