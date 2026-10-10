package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.entities.StockTransferItem;
import com.atlashub.commerce.inventory.domain.repositories.StockTransferRepository;
import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockTransferItemJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockTransferJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockTransferItemMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockTransferMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockTransferItemRepository;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockTransferRepository;
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
public class StockTransferRepositoryAdapter implements StockTransferRepository {

    private final SpringDataStockTransferRepository transferRepo;
    private final SpringDataStockTransferItemRepository itemRepo;
    private final StockTransferMapper transferMapper;
    private final StockTransferItemMapper itemMapper;
    private final DomainSequenceGenerator sequenceGenerator;
    private final DomainEventPublisher eventPublisher;

    public StockTransferRepositoryAdapter(SpringDataStockTransferRepository transferRepo,
                                          SpringDataStockTransferItemRepository itemRepo,
                                          StockTransferMapper transferMapper,
                                          StockTransferItemMapper itemMapper,
                                          DomainSequenceGenerator sequenceGenerator,
                                          DomainEventPublisher eventPublisher) {
        this.transferRepo = Objects.requireNonNull(transferRepo, "SpringDataStockTransferRepository must not be null");
        this.itemRepo = Objects.requireNonNull(itemRepo, "SpringDataStockTransferItemRepository must not be null");
        this.transferMapper = Objects.requireNonNull(transferMapper, "StockTransferMapper must not be null");
        this.itemMapper = Objects.requireNonNull(itemMapper, "StockTransferItemMapper must not be null");
        this.sequenceGenerator = Objects.requireNonNull(sequenceGenerator, "DomainSequenceGenerator must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "DomainEventPublisher must not be null");
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("commerce_stock_transfer_seq");
    }

    @Override
    @Transactional
    public StockTransfer save(StockTransfer aggregate) {
        StockTransferJpa record = transferMapper.toPersistence(aggregate);
        StockTransferJpa savedRecord = transferRepo.save(record);

        itemRepo.deleteByTransferId(savedRecord.getId());
        List<StockTransferItemJpa> itemRecords = aggregate.getItems().stream()
                .map(itemMapper::toPersistence)
                .toList();
        List<StockTransferItemJpa> savedItems = itemRepo.saveAll(itemRecords);

        List<DomainEvent<?>> events = aggregate.pullDomainEvents();
        if (events != null) {
            for (DomainEvent<?> event : events) {
                eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
            }
        }

        List<StockTransferItem> domainItems = savedItems.stream()
                .map(itemMapper::toDomain)
                .toList();

        return transferMapper.toDomain(savedRecord, domainItems);
    }

    @Override
    public Optional<StockTransfer> findById(Long id) {
        return transferRepo.findById(id).map(transfer -> {
            List<StockTransferItemJpa> items = itemRepo.findByTransferId(id);
            List<StockTransferItem> domainItems = items.stream()
                    .map(itemMapper::toDomain)
                    .toList();
            return transferMapper.toDomain(transfer, domainItems);
        });
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        itemRepo.deleteByTransferId(id);
        transferRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return transferRepo.existsById(id);
    }

    @Override
    public List<StockTransfer> findAll() {
        List<StockTransferJpa> transfers = transferRepo.findAll();
        List<Long> ids = transfers.stream().map(StockTransferJpa::getId).toList();
        List<StockTransferItemJpa> allItems = itemRepo.findByTransferIdIn(ids);

        return transfers.stream().map(transfer -> {
            List<StockTransferItem> items = allItems.stream()
                    .filter(i -> i.getTransferId().equals(transfer.getId()))
                    .map(itemMapper::toDomain)
                    .toList();
            return transferMapper.toDomain(transfer, items);
        }).toList();
    }

    @Override
    public List<StockTransfer> findByOrganizationIdAndStatus(Long orgId, TransferStatus status) {
        List<StockTransferJpa> transfers = transferRepo.findByOrganizationIdAndStatus(orgId, status);
        List<Long> ids = transfers.stream().map(StockTransferJpa::getId).toList();
        List<StockTransferItemJpa> allItems = itemRepo.findByTransferIdIn(ids);

        return transfers.stream().map(transfer -> {
            List<StockTransferItem> items = allItems.stream()
                    .filter(i -> i.getTransferId().equals(transfer.getId()))
                    .map(itemMapper::toDomain)
                    .toList();
            return transferMapper.toDomain(transfer, items);
        }).toList();
    }

    @Override
    public PageResult<StockTransfer> findByOrganizationId(Long orgId, TransferStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StockTransferJpa> jpaPage = status != null ?
                transferRepo.findByOrganizationIdAndStatus(orgId, status, pageable) :
                transferRepo.findByOrganizationId(orgId, pageable);

        List<Long> ids = jpaPage.getContent().stream().map(StockTransferJpa::getId).toList();
        List<StockTransferItemJpa> allItems = itemRepo.findByTransferIdIn(ids);

        List<StockTransfer> domainTransfers = jpaPage.getContent().stream().map(transfer -> {
            List<StockTransferItem> items = allItems.stream()
                    .filter(i -> i.getTransferId().equals(transfer.getId()))
                    .map(itemMapper::toDomain)
                    .toList();
            return transferMapper.toDomain(transfer, items);
        }).toList();

        return new PageResult<>(
                domainTransfers,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements(),
                jpaPage.getTotalPages()
        );
    }
}
