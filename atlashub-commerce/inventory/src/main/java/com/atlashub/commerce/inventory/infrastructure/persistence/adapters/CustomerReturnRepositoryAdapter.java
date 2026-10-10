package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.CustomerReturn;
import com.atlashub.commerce.inventory.domain.entities.ReturnItem;
import com.atlashub.commerce.inventory.domain.repositories.CustomerReturnRepository;
import com.atlashub.commerce.inventory.domain.valueobject.ReturnStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.CustomerReturnJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.ReturnItemJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.CustomerReturnMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.ReturnItemMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataCustomerReturnRepository;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataReturnItemRepository;
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
public class CustomerReturnRepositoryAdapter implements CustomerReturnRepository {

    private final SpringDataCustomerReturnRepository returnRepo;
    private final SpringDataReturnItemRepository itemRepo;
    private final CustomerReturnMapper returnMapper;
    private final ReturnItemMapper itemMapper;
    private final DomainSequenceGenerator sequenceGenerator;
    private final DomainEventPublisher eventPublisher;

    public CustomerReturnRepositoryAdapter(SpringDataCustomerReturnRepository returnRepo,
                                           SpringDataReturnItemRepository itemRepo,
                                           CustomerReturnMapper returnMapper,
                                           ReturnItemMapper itemMapper,
                                           DomainSequenceGenerator sequenceGenerator,
                                           DomainEventPublisher eventPublisher) {
        this.returnRepo = Objects.requireNonNull(returnRepo, "SpringDataCustomerReturnRepository must not be null");
        this.itemRepo = Objects.requireNonNull(itemRepo, "SpringDataReturnItemRepository must not be null");
        this.returnMapper = Objects.requireNonNull(returnMapper, "CustomerReturnMapper must not be null");
        this.itemMapper = Objects.requireNonNull(itemMapper, "ReturnItemMapper must not be null");
        this.sequenceGenerator = Objects.requireNonNull(sequenceGenerator, "DomainSequenceGenerator must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "DomainEventPublisher must not be null");
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("commerce_customer_return_seq");
    }

    @Override
    @Transactional
    public CustomerReturn save(CustomerReturn aggregate) {
        CustomerReturnJpa record = returnMapper.toPersistence(aggregate);
        CustomerReturnJpa savedRecord = returnRepo.save(record);

        itemRepo.deleteByReturnId(savedRecord.getId());
        List<ReturnItemJpa> itemRecords = aggregate.getItems().stream()
                .map(itemMapper::toPersistence)
                .toList();
        List<ReturnItemJpa> savedItems = itemRepo.saveAll(itemRecords);

        List<DomainEvent<?>> events = aggregate.pullDomainEvents();
        if (events != null) {
            for (DomainEvent<?> event : events) {
                eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
            }
        }

        List<ReturnItem> domainItems = savedItems.stream()
                .map(itemMapper::toDomain)
                .toList();

        return returnMapper.toDomain(savedRecord, domainItems);
    }

    @Override
    public Optional<CustomerReturn> findById(Long id) {
        return returnRepo.findById(id).map(customerReturn -> {
            List<ReturnItemJpa> items = itemRepo.findByReturnId(id);
            List<ReturnItem> domainItems = items.stream()
                    .map(itemMapper::toDomain)
                    .toList();
            return returnMapper.toDomain(customerReturn, domainItems);
        });
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        itemRepo.deleteByReturnId(id);
        returnRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return returnRepo.existsById(id);
    }

    @Override
    public List<CustomerReturn> findAll() {
        List<CustomerReturnJpa> returns = returnRepo.findAll();
        List<Long> ids = returns.stream().map(CustomerReturnJpa::getId).toList();
        List<ReturnItemJpa> allItems = itemRepo.findByReturnIdIn(ids);

        return returns.stream().map(customerReturn -> {
            List<ReturnItem> items = allItems.stream()
                    .filter(i -> i.getReturnId().equals(customerReturn.getId()))
                    .map(itemMapper::toDomain)
                    .toList();
            return returnMapper.toDomain(customerReturn, items);
        }).toList();
    }

    @Override
    public Optional<CustomerReturn> findByOrganizationIdAndSalesOrderId(Long orgId, Long salesOrderId) {
        return returnRepo.findByOrganizationIdAndSalesOrderId(orgId, salesOrderId).map(customerReturn -> {
            List<ReturnItemJpa> items = itemRepo.findByReturnId(customerReturn.getId());
            List<ReturnItem> domainItems = items.stream()
                    .map(itemMapper::toDomain)
                    .toList();
            return returnMapper.toDomain(customerReturn, domainItems);
        });
    }

    @Override
    public List<CustomerReturn> findByOrganizationIdAndOutletIdAndStatus(Long orgId, Long outletId, ReturnStatus status) {
        List<CustomerReturnJpa> returns = returnRepo.findByOrganizationIdAndOutletIdAndStatus(orgId, outletId, status);
        List<Long> ids = returns.stream().map(CustomerReturnJpa::getId).toList();
        List<ReturnItemJpa> allItems = itemRepo.findByReturnIdIn(ids);

        return returns.stream().map(customerReturn -> {
            List<ReturnItem> items = allItems.stream()
                    .filter(i -> i.getReturnId().equals(customerReturn.getId()))
                    .map(itemMapper::toDomain)
                    .toList();
            return returnMapper.toDomain(customerReturn, items);
        }).toList();
    }
}
