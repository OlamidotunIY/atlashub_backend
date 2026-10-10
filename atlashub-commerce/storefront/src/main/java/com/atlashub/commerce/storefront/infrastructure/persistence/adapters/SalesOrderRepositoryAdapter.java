package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.SalesOrderItemJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.SalesOrderJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.SalesOrderItemMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.SalesOrderMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataSalesOrderItemRepository;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataSalesOrderRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class SalesOrderRepositoryAdapter implements SalesOrderRepository {

    private final SpringDataSalesOrderRepository orderRepo;
    private final SpringDataSalesOrderItemRepository itemRepo;
    private final SalesOrderMapper orderMapper;
    private final SalesOrderItemMapper itemMapper;
    private final DomainSequenceGenerator sequenceGenerator;
    private final DomainEventPublisher eventPublisher;

    public SalesOrderRepositoryAdapter(
            SpringDataSalesOrderRepository orderRepo,
            SpringDataSalesOrderItemRepository itemRepo,
            SalesOrderMapper orderMapper,
            SalesOrderItemMapper itemMapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        this.orderRepo = Objects.requireNonNull(orderRepo, "SpringDataSalesOrderRepository must not be null");
        this.itemRepo = Objects.requireNonNull(itemRepo, "SpringDataSalesOrderItemRepository must not be null");
        this.orderMapper = Objects.requireNonNull(orderMapper, "SalesOrderMapper must not be null");
        this.itemMapper = Objects.requireNonNull(itemMapper, "SalesOrderItemMapper must not be null");
        this.sequenceGenerator = Objects.requireNonNull(sequenceGenerator, "DomainSequenceGenerator must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "DomainEventPublisher must not be null");
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("commerce_sales_order_seq");
    }

    @Override
    @Transactional
    public SalesOrder save(SalesOrder aggregate) {
        SalesOrderJpa record = orderMapper.toPersistence(aggregate);
        SalesOrderJpa savedRecord = orderRepo.save(record);

        itemRepo.deleteBySalesOrderId(savedRecord.getId());
        List<SalesOrderItemJpa> itemRecords = aggregate.getItems() == null
                ? List.of()
                : aggregate.getItems().stream().map(itemMapper::toPersistence).toList();
        List<SalesOrderItemJpa> savedItems = itemRepo.saveAll(itemRecords);

        List<DomainEvent<?>> events = aggregate.pullDomainEvents();
        if (events != null) {
            for (DomainEvent<?> event : events) {
                eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
            }
        }

        List<SalesOrderItem> domainItems = savedItems.stream()
                .map(itemMapper::toDomain)
                .toList();

        return orderMapper.toDomain(savedRecord, domainItems);
    }

    @Override
    public Optional<SalesOrder> findById(Long id) {
        return orderRepo.findById(id).map(record -> {
            List<SalesOrderItemJpa> items = itemRepo.findBySalesOrderId(id);
            List<SalesOrderItem> domainItems = items.stream().map(itemMapper::toDomain).toList();
            return orderMapper.toDomain(record, domainItems);
        });
    }

    @Override
    public Optional<SalesOrder> findByIdWithItems(Long id) {
        return findById(id);
    }

    @Override
    public Optional<SalesOrder> findByChargeReference(String chargeReference) {
        return orderRepo.findByChargeReference(chargeReference).map(record -> {
            List<SalesOrderItemJpa> items = itemRepo.findBySalesOrderId(record.getId());
            List<SalesOrderItem> domainItems = items.stream().map(itemMapper::toDomain).toList();
            return orderMapper.toDomain(record, domainItems);
        });
    }

    @Override
    public List<SalesOrder> findByOutletId(Long outletId) {
        return orderRepo.findByOutletId(outletId).stream()
                .map(record -> {
                    List<SalesOrderItemJpa> items = itemRepo.findBySalesOrderId(record.getId());
                    List<SalesOrderItem> domainItems = items.stream().map(itemMapper::toDomain).toList();
                    return orderMapper.toDomain(record, domainItems);
                })
                .toList();
    }

    @Override
    public List<SalesOrder> findByStatusAndSaleDateBefore(OrderStatus status, ZonedDateTime cutoff) {
        return orderRepo.findByStatusAndSaleDateBefore(status, cutoff).stream()
                .map(record -> {
                    List<SalesOrderItemJpa> items = itemRepo.findBySalesOrderId(record.getId());
                    List<SalesOrderItem> domainItems = items.stream().map(itemMapper::toDomain).toList();
                    return orderMapper.toDomain(record, domainItems);
                })
                .toList();
    }

    @Override
    public PageResult<SalesOrder> findTransactions(
            Long outletId,
            Long tillId,
            Long cashierId,
            ZonedDateTime from,
            ZonedDateTime to,
            int page,
            int size
    ) {
        Page<SalesOrderJpa> pageResult = orderRepo.findTransactions(
                outletId,
                tillId,
                cashierId,
                from,
                to,
                PageRequest.of(page, size)
        );

        List<SalesOrder> content = pageResult.getContent().stream()
                .map(record -> {
                    List<SalesOrderItemJpa> items = itemRepo.findBySalesOrderId(record.getId());
                    List<SalesOrderItem> domainItems = items.stream().map(itemMapper::toDomain).toList();
                    return orderMapper.toDomain(record, domainItems);
                })
                .toList();

        return new PageResult<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    @Override
    public List<SalesOrder> findAll() {
        return orderRepo.findAll().stream()
                .map(record -> {
                    List<SalesOrderItemJpa> items = itemRepo.findBySalesOrderId(record.getId());
                    List<SalesOrderItem> domainItems = items.stream().map(itemMapper::toDomain).toList();
                    return orderMapper.toDomain(record, domainItems);
                })
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return orderRepo.existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        itemRepo.deleteBySalesOrderId(id);
        orderRepo.deleteById(id);
    }
}
