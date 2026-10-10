package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.StockCount;
import com.atlashub.commerce.inventory.domain.entities.StockCountItem;
import com.atlashub.commerce.inventory.domain.repositories.StockCountRepository;
import com.atlashub.commerce.inventory.domain.valueobject.StockCountStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockCountItemJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockCountJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockCountItemMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockCountMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockCountItemRepository;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockCountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class StockCountRepositoryAdapter implements StockCountRepository {

    private final SpringDataStockCountRepository countRepo;
    private final SpringDataStockCountItemRepository itemRepo;
    private final StockCountMapper countMapper;
    private final StockCountItemMapper itemMapper;
    private final DomainSequenceGenerator sequenceGenerator;
    private final DomainEventPublisher eventPublisher;

    public StockCountRepositoryAdapter(SpringDataStockCountRepository countRepo,
                                       SpringDataStockCountItemRepository itemRepo,
                                       StockCountMapper countMapper,
                                       StockCountItemMapper itemMapper,
                                       DomainSequenceGenerator sequenceGenerator,
                                       DomainEventPublisher eventPublisher) {
        this.countRepo = Objects.requireNonNull(countRepo, "SpringDataStockCountRepository must not be null");
        this.itemRepo = Objects.requireNonNull(itemRepo, "SpringDataStockCountItemRepository must not be null");
        this.countMapper = Objects.requireNonNull(countMapper, "StockCountMapper must not be null");
        this.itemMapper = Objects.requireNonNull(itemMapper, "StockCountItemMapper must not be null");
        this.sequenceGenerator = Objects.requireNonNull(sequenceGenerator, "DomainSequenceGenerator must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "DomainEventPublisher must not be null");
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("commerce_stock_count_seq");
    }

    @Override
    @Transactional
    public StockCount save(StockCount aggregate) {
        StockCountJpa record = countMapper.toPersistence(aggregate);
        StockCountJpa savedRecord = countRepo.save(record);

        itemRepo.deleteByStockCountId(savedRecord.getId());
        List<StockCountItemJpa> itemRecords = aggregate.getItems().stream()
                .map(itemMapper::toPersistence)
                .toList();
        List<StockCountItemJpa> savedItems = itemRepo.saveAll(itemRecords);

        List<DomainEvent<?>> events = aggregate.pullDomainEvents();
        if (events != null) {
            for (DomainEvent<?> event : events) {
                eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
            }
        }

        List<StockCountItem> domainItems = savedItems.stream()
                .map(itemMapper::toDomain)
                .toList();

        return countMapper.toDomain(savedRecord, domainItems);
    }

    @Override
    public Optional<StockCount> findById(Long id) {
        return countRepo.findById(id).map(count -> {
            List<StockCountItemJpa> items = itemRepo.findByStockCountId(id);
            List<StockCountItem> domainItems = items.stream()
                    .map(itemMapper::toDomain)
                    .toList();
            return countMapper.toDomain(count, domainItems);
        });
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        itemRepo.deleteByStockCountId(id);
        countRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return countRepo.existsById(id);
    }

    @Override
    public List<StockCount> findAll() {
        List<StockCountJpa> counts = countRepo.findAll();
        List<Long> ids = counts.stream().map(StockCountJpa::getId).toList();
        List<StockCountItemJpa> allItems = itemRepo.findByStockCountIdIn(ids);

        return counts.stream().map(count -> {
            List<StockCountItem> items = allItems.stream()
                    .filter(i -> i.getStockCountId().equals(count.getId()))
                    .map(itemMapper::toDomain)
                    .toList();
            return countMapper.toDomain(count, items);
        }).toList();
    }

    @Override
    public List<StockCount> findByOrganizationIdAndOutletIdAndStatus(Long orgId, Long outletId, StockCountStatus status) {
        List<StockCountJpa> counts = countRepo.findByOrganizationIdAndOutletIdAndStatus(orgId, outletId, status);
        List<Long> ids = counts.stream().map(StockCountJpa::getId).toList();
        List<StockCountItemJpa> allItems = itemRepo.findByStockCountIdIn(ids);

        return counts.stream().map(count -> {
            List<StockCountItem> items = allItems.stream()
                    .filter(i -> i.getStockCountId().equals(count.getId()))
                    .map(itemMapper::toDomain)
                    .toList();
            return countMapper.toDomain(count, items);
        }).toList();
    }
}
