package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.entities.PurchaseOrder;
import com.atlashub.commerce.catalog.domain.entities.PurchaseOrderItem;
import com.atlashub.commerce.catalog.domain.repositories.PurchaseOrderRepository;
import com.atlashub.commerce.catalog.domain.valueobject.PurchaseOrderStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.PurchaseOrderItemJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.PurchaseOrderJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.PurchaseOrderItemMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.PurchaseOrderMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataPurchaseOrderItemRepository;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataPurchaseOrderRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class PurchaseOrderRepositoryAdapter implements PurchaseOrderRepository {

    private final SpringDataPurchaseOrderRepository poRepo;
    private final SpringDataPurchaseOrderItemRepository itemRepo;
    private final PurchaseOrderMapper poMapper;
    private final PurchaseOrderItemMapper itemMapper;
    private final DomainSequenceGenerator sequenceGenerator;
    private final DomainEventPublisher eventPublisher;

    public PurchaseOrderRepositoryAdapter(SpringDataPurchaseOrderRepository poRepo,
                                          SpringDataPurchaseOrderItemRepository itemRepo,
                                          PurchaseOrderMapper poMapper,
                                          PurchaseOrderItemMapper itemMapper,
                                          DomainSequenceGenerator sequenceGenerator,
                                          DomainEventPublisher eventPublisher) {
        this.poRepo = Objects.requireNonNull(poRepo, "SpringDataPurchaseOrderRepository must not be null");
        this.itemRepo = Objects.requireNonNull(itemRepo, "SpringDataPurchaseOrderItemRepository must not be null");
        this.poMapper = Objects.requireNonNull(poMapper, "PurchaseOrderMapper must not be null");
        this.itemMapper = Objects.requireNonNull(itemMapper, "PurchaseOrderItemMapper must not be null");
        this.sequenceGenerator = Objects.requireNonNull(sequenceGenerator, "DomainSequenceGenerator must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "DomainEventPublisher must not be null");
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("commerce_purchase_order_seq");
    }

    @Override
    @Transactional
    public PurchaseOrder save(PurchaseOrder aggregate) {
        PurchaseOrderJpa record = poMapper.toPersistence(aggregate);
        PurchaseOrderJpa savedRecord = poRepo.save(record);

        itemRepo.deleteByPurchaseOrderId(savedRecord.getId());
        List<PurchaseOrderItemJpa> itemRecords = aggregate.getItems().stream()
                .map(itemMapper::toPersistence)
                .toList();
        List<PurchaseOrderItemJpa> savedItems = itemRepo.saveAll(itemRecords);

        List<DomainEvent<?>> events = aggregate.pullDomainEvents();
        if (events != null) {
            for (DomainEvent<?> event : events) {
                eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
            }
        }

        List<PurchaseOrderItem> domainItems = savedItems.stream()
                .map(itemMapper::toDomain)
                .toList();

        return poMapper.toDomain(savedRecord, domainItems);
    }

    @Override
    public Optional<PurchaseOrder> findById(Long id) {
        return poRepo.findById(id).map(po -> {
            List<PurchaseOrderItemJpa> items = itemRepo.findByPurchaseOrderId(id);
            List<PurchaseOrderItem> domainItems = items.stream()
                    .map(itemMapper::toDomain)
                    .toList();
            return poMapper.toDomain(po, domainItems);
        });
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        itemRepo.deleteByPurchaseOrderId(id);
        poRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return poRepo.existsById(id);
    }

    @Override
    public List<PurchaseOrder> findAll() {
        List<PurchaseOrderJpa> pos = poRepo.findAll();
        List<Long> poIds = pos.stream().map(PurchaseOrderJpa::getId).toList();
        List<PurchaseOrderItemJpa> allItems = itemRepo.findByPurchaseOrderIdIn(poIds);

        return pos.stream().map(po -> {
            List<PurchaseOrderItem> items = allItems.stream()
                    .filter(i -> i.getPurchaseOrderId().equals(po.getId()))
                    .map(itemMapper::toDomain)
                    .toList();
            return poMapper.toDomain(po, items);
        }).toList();
    }

    @Override
    public PageResult<PurchaseOrder> findByOrganizationId(Long organizationId, PurchaseOrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrderJpa> jpaPage = status != null ?
                poRepo.findByOrganizationIdAndStatus(organizationId, status, pageable) :
                poRepo.findByOrganizationId(organizationId, pageable);

        List<Long> poIds = jpaPage.getContent().stream().map(PurchaseOrderJpa::getId).toList();
        List<PurchaseOrderItemJpa> allItems = itemRepo.findByPurchaseOrderIdIn(poIds);

        List<PurchaseOrder> domainPos = jpaPage.getContent().stream().map(po -> {
            List<PurchaseOrderItem> items = allItems.stream()
                    .filter(i -> i.getPurchaseOrderId().equals(po.getId()))
                    .map(itemMapper::toDomain)
                    .toList();
            return poMapper.toDomain(po, items);
        }).toList();

        return new PageResult<>(
                domainPos,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements(),
                jpaPage.getTotalPages()
        );
    }
}
