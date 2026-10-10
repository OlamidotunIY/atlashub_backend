package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.StockCount;
import com.atlashub.commerce.inventory.domain.entities.StockCountItem;
import com.atlashub.commerce.inventory.domain.valueobject.StockCountStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockCountItemJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockCountJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockCountItemMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockCountMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockCountItemRepository;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockCountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

class StockCountRepositoryAdapterTest {

    private SpringDataStockCountRepository countRepo;
    private SpringDataStockCountItemRepository itemRepo;
    private StockCountMapper countMapper;
    private StockCountItemMapper itemMapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private StockCountRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        countRepo = mock(SpringDataStockCountRepository.class);
        itemRepo = mock(SpringDataStockCountItemRepository.class);
        countMapper = mock(StockCountMapper.class);
        itemMapper = mock(StockCountItemMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new StockCountRepositoryAdapter(countRepo, itemRepo, countMapper, itemMapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for stock count sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_stock_count_seq")).thenReturn(33L);
        assertEquals(33L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should save stock count and items")
    void shouldSaveStockCountAndItems() {
        StockCount domain = mock(StockCount.class);
        StockCountItem item = mock(StockCountItem.class);
        when(domain.getItems()).thenReturn(List.of(item));

        StockCountJpa countJpa = mock(StockCountJpa.class);
        when(countJpa.getId()).thenReturn(1L);
        when(countMapper.toPersistence(domain)).thenReturn(countJpa);
        when(countRepo.save(countJpa)).thenReturn(countJpa);

        StockCountItemJpa itemJpa = mock(StockCountItemJpa.class);
        when(itemMapper.toPersistence(item)).thenReturn(itemJpa);
        when(itemRepo.saveAll(any())).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(countMapper.toDomain(eq(countJpa), any())).thenReturn(domain);

        StockCount result = adapter.save(domain);
        assertNotNull(result);
        verify(countRepo).save(countJpa);
        verify(itemRepo).deleteByStockCountId(1L);
        verify(itemRepo).saveAll(any());
    }

    @Test
    @DisplayName("Should find stock count by id with items")
    void shouldFindById() {
        StockCountJpa countJpa = mock(StockCountJpa.class);
        StockCountItemJpa itemJpa = mock(StockCountItemJpa.class);
        StockCount domain = mock(StockCount.class);
        StockCountItem item = mock(StockCountItem.class);

        when(countRepo.findById(1L)).thenReturn(Optional.of(countJpa));
        when(itemRepo.findByStockCountId(1L)).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(countMapper.toDomain(eq(countJpa), eq(List.of(item)))).thenReturn(domain);

        Optional<StockCount> result = adapter.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should delete stock count and items by id")
    void shouldDeleteById() {
        adapter.deleteById(1L);
        verify(itemRepo).deleteByStockCountId(1L);
        verify(countRepo).deleteById(1L);
    }

    @Test
    @DisplayName("Should find stock counts by organization, outlet and status")
    void shouldFindByStatus() {
        StockCountJpa countJpa = mock(StockCountJpa.class);
        when(countJpa.getId()).thenReturn(1L);
        StockCount domain = mock(StockCount.class);

        when(countRepo.findByOrganizationIdAndOutletIdAndStatus(10L, 20L, StockCountStatus.OPEN))
                .thenReturn(List.of(countJpa));
        when(itemRepo.findByStockCountIdIn(List.of(1L))).thenReturn(List.of());
        when(countMapper.toDomain(eq(countJpa), any())).thenReturn(domain);

        List<StockCount> list = adapter.findByOrganizationIdAndOutletIdAndStatus(10L, 20L, StockCountStatus.OPEN);
        assertEquals(1, list.size());
        assertEquals(domain, list.get(0));
    }
}
