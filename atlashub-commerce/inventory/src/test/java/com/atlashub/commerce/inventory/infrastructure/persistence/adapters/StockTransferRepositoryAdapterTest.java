package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.entities.StockTransferItem;
import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockTransferItemJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockTransferJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockTransferItemMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockTransferMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockTransferItemRepository;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockTransferRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StockTransferRepositoryAdapterTest {

    private SpringDataStockTransferRepository transferRepo;
    private SpringDataStockTransferItemRepository itemRepo;
    private StockTransferMapper transferMapper;
    private StockTransferItemMapper itemMapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private StockTransferRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        transferRepo = mock(SpringDataStockTransferRepository.class);
        itemRepo = mock(SpringDataStockTransferItemRepository.class);
        transferMapper = mock(StockTransferMapper.class);
        itemMapper = mock(StockTransferItemMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new StockTransferRepositoryAdapter(transferRepo, itemRepo, transferMapper, itemMapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for stock transfer sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_stock_transfer_seq")).thenReturn(44L);
        assertEquals(44L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should save stock transfer and items")
    void shouldSaveStockTransferAndItems() {
        StockTransfer domain = mock(StockTransfer.class);
        StockTransferItem item = mock(StockTransferItem.class);
        when(domain.getItems()).thenReturn(List.of(item));

        StockTransferJpa transferJpa = mock(StockTransferJpa.class);
        when(transferJpa.getId()).thenReturn(1L);
        when(transferMapper.toPersistence(domain)).thenReturn(transferJpa);
        when(transferRepo.save(transferJpa)).thenReturn(transferJpa);

        StockTransferItemJpa itemJpa = mock(StockTransferItemJpa.class);
        when(itemMapper.toPersistence(item)).thenReturn(itemJpa);
        when(itemRepo.saveAll(any())).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(transferMapper.toDomain(eq(transferJpa), any())).thenReturn(domain);

        StockTransfer result = adapter.save(domain);
        assertNotNull(result);
        verify(transferRepo).save(transferJpa);
        verify(itemRepo).deleteByTransferId(1L);
        verify(itemRepo).saveAll(any());
    }

    @Test
    @DisplayName("Should find stock transfer by id with items")
    void shouldFindById() {
        StockTransferJpa transferJpa = mock(StockTransferJpa.class);
        StockTransferItemJpa itemJpa = mock(StockTransferItemJpa.class);
        StockTransfer domain = mock(StockTransfer.class);
        StockTransferItem item = mock(StockTransferItem.class);

        when(transferRepo.findById(1L)).thenReturn(Optional.of(transferJpa));
        when(itemRepo.findByTransferId(1L)).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(transferMapper.toDomain(eq(transferJpa), eq(List.of(item)))).thenReturn(domain);

        Optional<StockTransfer> result = adapter.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should delete stock transfer and items by id")
    void shouldDeleteById() {
        adapter.deleteById(1L);
        verify(itemRepo).deleteByTransferId(1L);
        verify(transferRepo).deleteById(1L);
    }

    @Test
    @DisplayName("Should find stock transfers by organization and status")
    void shouldFindByStatus() {
        StockTransferJpa transferJpa = mock(StockTransferJpa.class);
        when(transferJpa.getId()).thenReturn(1L);
        StockTransfer domain = mock(StockTransfer.class);

        when(transferRepo.findByOrganizationIdAndStatus(10L, TransferStatus.REQUESTED))
                .thenReturn(List.of(transferJpa));
        when(itemRepo.findByTransferIdIn(List.of(1L))).thenReturn(List.of());
        when(transferMapper.toDomain(eq(transferJpa), any())).thenReturn(domain);

        List<StockTransfer> list = adapter.findByOrganizationIdAndStatus(10L, TransferStatus.REQUESTED);
        assertEquals(1, list.size());
        assertEquals(domain, list.get(0));
    }

    @Test
    @DisplayName("Should return paginated stock transfers with items")
    void shouldReturnPaginatedTransfers() {
        StockTransferJpa transferJpa = mock(StockTransferJpa.class);
        when(transferJpa.getId()).thenReturn(1L);
        StockTransfer domain = mock(StockTransfer.class);

        Page<StockTransferJpa> jpaPage = new PageImpl<>(List.of(transferJpa), PageRequest.of(0, 10), 1);
        when(transferRepo.findByOrganizationIdAndStatus(eq(10L), eq(TransferStatus.REQUESTED), any()))
                .thenReturn(jpaPage);
        when(itemRepo.findByTransferIdIn(List.of(1L))).thenReturn(List.of());
        when(transferMapper.toDomain(eq(transferJpa), any())).thenReturn(domain);

        PageResult<StockTransfer> page = adapter.findByOrganizationId(10L, TransferStatus.REQUESTED, 0, 10);
        assertNotNull(page);
        assertEquals(1, page.content().size());
        assertEquals(1, page.totalElements());
    }
}
