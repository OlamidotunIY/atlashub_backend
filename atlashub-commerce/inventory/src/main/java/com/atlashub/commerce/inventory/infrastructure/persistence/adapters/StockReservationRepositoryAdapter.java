package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.StockReservation;
import com.atlashub.commerce.inventory.domain.entities.StockReservationItem;
import com.atlashub.commerce.inventory.domain.repositories.StockReservationRepository;
import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockReservationItemJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockReservationJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockReservationItemMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockReservationMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockReservationItemRepository;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockReservationRepository;
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
public class StockReservationRepositoryAdapter implements StockReservationRepository {

    private final SpringDataStockReservationRepository resRepo;
    private final SpringDataStockReservationItemRepository itemRepo;
    private final StockReservationMapper resMapper;
    private final StockReservationItemMapper itemMapper;
    private final DomainSequenceGenerator sequenceGenerator;
    private final DomainEventPublisher eventPublisher;

    public StockReservationRepositoryAdapter(SpringDataStockReservationRepository resRepo,
                                            SpringDataStockReservationItemRepository itemRepo,
                                            StockReservationMapper resMapper,
                                            StockReservationItemMapper itemMapper,
                                            DomainSequenceGenerator sequenceGenerator,
                                            DomainEventPublisher eventPublisher) {
        this.resRepo = Objects.requireNonNull(resRepo, "SpringDataStockReservationRepository must not be null");
        this.itemRepo = Objects.requireNonNull(itemRepo, "SpringDataStockReservationItemRepository must not be null");
        this.resMapper = Objects.requireNonNull(resMapper, "StockReservationMapper must not be null");
        this.itemMapper = Objects.requireNonNull(itemMapper, "StockReservationItemMapper must not be null");
        this.sequenceGenerator = Objects.requireNonNull(sequenceGenerator, "DomainSequenceGenerator must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "DomainEventPublisher must not be null");
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("commerce_stock_reservation_seq");
    }

    @Override
    @Transactional
    public StockReservation save(StockReservation aggregate) {
        StockReservationJpa record = resMapper.toPersistence(aggregate);
        StockReservationJpa savedRecord = resRepo.save(record);

        itemRepo.deleteByReservationId(savedRecord.getId());
        List<StockReservationItemJpa> itemRecords = aggregate.getItems().stream()
                .map(itemMapper::toPersistence)
                .toList();
        List<StockReservationItemJpa> savedItems = itemRepo.saveAll(itemRecords);

        List<DomainEvent<?>> events = aggregate.pullDomainEvents();
        if (events != null) {
            for (DomainEvent<?> event : events) {
                eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
            }
        }

        List<StockReservationItem> domainItems = savedItems.stream()
                .map(itemMapper::toDomain)
                .toList();

        return resMapper.toDomain(savedRecord, domainItems);
    }

    @Override
    public Optional<StockReservation> findById(Long id) {
        return resRepo.findById(id).map(res -> {
            List<StockReservationItemJpa> items = itemRepo.findByReservationId(id);
            List<StockReservationItem> domainItems = items.stream()
                    .map(itemMapper::toDomain)
                    .toList();
            return resMapper.toDomain(res, domainItems);
        });
    }

    @Override
    public Optional<StockReservation> findBySalesOrderId(Long salesOrderId) {
        return resRepo.findBySalesOrderId(salesOrderId).map(res -> {
            List<StockReservationItemJpa> items = itemRepo.findByReservationId(res.getId());
            List<StockReservationItem> domainItems = items.stream()
                    .map(itemMapper::toDomain)
                    .toList();
            return resMapper.toDomain(res, domainItems);
        });
    }

    @Override
    public List<StockReservation> findByOrganizationIdAndStatus(Long organizationId, ReservationStatus status) {
        return resRepo.findByOrganizationIdAndStatus(organizationId, status).stream()
                .map(res -> {
                    List<StockReservationItemJpa> items = itemRepo.findByReservationId(res.getId());
                    List<StockReservationItem> domainItems = items.stream()
                            .map(itemMapper::toDomain)
                            .toList();
                    return resMapper.toDomain(res, domainItems);
                })
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        itemRepo.deleteByReservationId(id);
        resRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return resRepo.existsById(id);
    }

    @Override
    public List<StockReservation> findAll() {
        return resRepo.findAll().stream()
                .map(res -> {
                    List<StockReservationItemJpa> items = itemRepo.findByReservationId(res.getId());
                    List<StockReservationItem> domainItems = items.stream()
                            .map(itemMapper::toDomain)
                            .toList();
                    return resMapper.toDomain(res, domainItems);
                })
                .toList();
    }
}
