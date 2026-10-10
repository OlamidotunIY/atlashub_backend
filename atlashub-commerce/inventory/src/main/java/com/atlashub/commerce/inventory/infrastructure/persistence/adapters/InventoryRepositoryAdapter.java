package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.InventoryJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.InventoryMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataInventoryRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InventoryRepositoryAdapter
        extends JpaBaseRepository<Inventory, InventoryJpa>
        implements InventoryRepository {

    private final SpringDataInventoryRepository springDataRepo;

    public InventoryRepositoryAdapter(SpringDataInventoryRepository springDataRepo,
                                     InventoryMapper mapper,
                                     DomainSequenceGenerator sequenceGenerator,
                                     DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "commerce_inventory_seq";
    }

    @Override
    public Optional<Inventory> findByOrganizationIdAndOutletIdAndProductIdAndVariantId(Long orgId, Long outletId, Long productId, Long variantId) {
        return springDataRepo.findByOrganizationIdAndOutletIdAndProductIdAndVariantId(orgId, outletId, productId, variantId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Inventory> findByOrganizationIdAndOutletIdAndProductId(Long orgId, Long outletId, Long productId) {
        return springDataRepo.findByOrganizationIdAndOutletIdAndProductId(orgId, outletId, productId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Inventory> findByOrganizationIdAndOutletId(Long orgId, Long outletId) {
        return springDataRepo.findByOrganizationIdAndOutletId(orgId, outletId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Inventory> findLowStock(Long orgId, Long outletId) {
        return springDataRepo.findLowStock(orgId, outletId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
