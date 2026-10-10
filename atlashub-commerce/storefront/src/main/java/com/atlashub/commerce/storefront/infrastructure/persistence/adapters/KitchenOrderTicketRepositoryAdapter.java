package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.KitchenOrderTicket;
import com.atlashub.commerce.storefront.domain.entities.KotItem;
import com.atlashub.commerce.storefront.domain.repositories.KitchenOrderTicketRepository;
import com.atlashub.commerce.storefront.domain.valueobject.KotStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.KitchenOrderTicketJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.KotItemJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.KitchenOrderTicketMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.KotItemMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataKitchenOrderTicketRepository;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataKotItemRepository;
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
public class KitchenOrderTicketRepositoryAdapter implements KitchenOrderTicketRepository {

    private final SpringDataKitchenOrderTicketRepository kotRepo;
    private final SpringDataKotItemRepository itemRepo;
    private final KitchenOrderTicketMapper kotMapper;
    private final KotItemMapper itemMapper;
    private final DomainSequenceGenerator sequenceGenerator;
    private final DomainEventPublisher eventPublisher;

    public KitchenOrderTicketRepositoryAdapter(
            SpringDataKitchenOrderTicketRepository kotRepo,
            SpringDataKotItemRepository itemRepo,
            KitchenOrderTicketMapper kotMapper,
            KotItemMapper itemMapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        this.kotRepo = Objects.requireNonNull(kotRepo, "SpringDataKitchenOrderTicketRepository must not be null");
        this.itemRepo = Objects.requireNonNull(itemRepo, "SpringDataKotItemRepository must not be null");
        this.kotMapper = Objects.requireNonNull(kotMapper, "KitchenOrderTicketMapper must not be null");
        this.itemMapper = Objects.requireNonNull(itemMapper, "KotItemMapper must not be null");
        this.sequenceGenerator = Objects.requireNonNull(sequenceGenerator, "DomainSequenceGenerator must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "DomainEventPublisher must not be null");
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("commerce_kot_seq");
    }

    @Override
    @Transactional
    public KitchenOrderTicket save(KitchenOrderTicket aggregate) {
        KitchenOrderTicketJpa record = kotMapper.toPersistence(aggregate);
        KitchenOrderTicketJpa savedRecord = kotRepo.save(record);

        itemRepo.deleteByKotId(savedRecord.getId());
        List<KotItemJpa> itemRecords = aggregate.getItems().stream()
                .map(itemMapper::toPersistence)
                .toList();
        List<KotItemJpa> savedItems = itemRepo.saveAll(itemRecords);

        List<DomainEvent<?>> events = aggregate.pullDomainEvents();
        if (events != null) {
            for (DomainEvent<?> event : events) {
                eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
            }
        }

        List<KotItem> domainItems = savedItems.stream()
                .map(itemMapper::toDomain)
                .toList();

        return kotMapper.toDomain(savedRecord, domainItems);
    }

    @Override
    public Optional<KitchenOrderTicket> findById(Long id) {
        return kotRepo.findById(id).map(record -> {
            List<KotItemJpa> items = itemRepo.findByKotId(id);
            List<KotItem> domainItems = items.stream().map(itemMapper::toDomain).toList();
            return kotMapper.toDomain(record, domainItems);
        });
    }

    @Override
    public Optional<KitchenOrderTicket> findByIdWithItems(Long id) {
        return findById(id);
    }

    @Override
    public List<KitchenOrderTicket> findBySalesOrderId(Long salesOrderId) {
        return kotRepo.findBySalesOrderId(salesOrderId).stream()
                .map(record -> {
                    List<KotItemJpa> items = itemRepo.findByKotId(record.getId());
                    List<KotItem> domainItems = items.stream().map(itemMapper::toDomain).toList();
                    return kotMapper.toDomain(record, domainItems);
                })
                .toList();
    }

    @Override
    public List<KitchenOrderTicket> findByOutletIdAndStatus(Long outletId, KotStatus status) {
        return kotRepo.findByOutletIdAndStatus(outletId, status).stream()
                .map(record -> {
                    List<KotItemJpa> items = itemRepo.findByKotId(record.getId());
                    List<KotItem> domainItems = items.stream().map(itemMapper::toDomain).toList();
                    return kotMapper.toDomain(record, domainItems);
                })
                .toList();
    }

    @Override
    public List<KitchenOrderTicket> findAll() {
        return kotRepo.findAll().stream()
                .map(record -> {
                    List<KotItemJpa> items = itemRepo.findByKotId(record.getId());
                    List<KotItem> domainItems = items.stream().map(itemMapper::toDomain).toList();
                    return kotMapper.toDomain(record, domainItems);
                })
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return kotRepo.existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        itemRepo.deleteByKotId(id);
        kotRepo.deleteById(id);
    }
}
