package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.KitchenOrderTicket;
import com.atlashub.commerce.storefront.domain.entities.KotItem;
import com.atlashub.commerce.storefront.domain.valueobject.KotStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.KitchenOrderTicketJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.KotItemJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.KitchenOrderTicketMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.KotItemMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataKitchenOrderTicketRepository;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataKotItemRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
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

class KitchenOrderTicketRepositoryAdapterTest {

    private SpringDataKitchenOrderTicketRepository kotRepo;
    private SpringDataKotItemRepository itemRepo;
    private KitchenOrderTicketMapper kotMapper;
    private KotItemMapper itemMapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private KitchenOrderTicketRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        kotRepo = mock(SpringDataKitchenOrderTicketRepository.class);
        itemRepo = mock(SpringDataKotItemRepository.class);
        kotMapper = mock(KitchenOrderTicketMapper.class);
        itemMapper = mock(KotItemMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new KitchenOrderTicketRepositoryAdapter(kotRepo, itemRepo, kotMapper, itemMapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for KOT sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_kot_seq")).thenReturn(500L);
        assertEquals(500L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should save KOT and items")
    void shouldSaveKotAndItems() {
        KotItem item = KotItem.create(1L, 500L, 10L, "Jollof Rice", 2);
        KitchenOrderTicket kot = KitchenOrderTicket.create(500L, 100L, 5L, 20L, List.of(item));

        KitchenOrderTicketJpa kotJpa = new KitchenOrderTicketJpa(500L, 100L, 5L, 20L, KotStatus.PENDING, ZonedDateTime.now(), 0L);
        KotItemJpa itemJpa = new KotItemJpa(1L, 500L, 10L, "Jollof Rice", 2, 0L);

        when(kotMapper.toPersistence(kot)).thenReturn(kotJpa);
        when(kotRepo.save(kotJpa)).thenReturn(kotJpa);
        when(itemMapper.toPersistence(item)).thenReturn(itemJpa);
        when(itemRepo.saveAll(any())).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(kotMapper.toDomain(eq(kotJpa), any())).thenReturn(kot);

        KitchenOrderTicket saved = adapter.save(kot);

        assertNotNull(saved);
        verify(kotRepo).save(kotJpa);
        verify(itemRepo).deleteByKotId(500L);
        verify(itemRepo).saveAll(any());
    }

    @Test
    @DisplayName("Should find KOT by id with items")
    void shouldFindByIdWithItems() {
        KitchenOrderTicketJpa kotJpa = new KitchenOrderTicketJpa(500L, 100L, 5L, 20L, KotStatus.PENDING, ZonedDateTime.now(), 0L);
        KotItemJpa itemJpa = new KotItemJpa(1L, 500L, 10L, "Jollof Rice", 2, 0L);
        KotItem item = KotItem.create(1L, 500L, 10L, "Jollof Rice", 2);
        KitchenOrderTicket kot = KitchenOrderTicket.create(500L, 100L, 5L, 20L, List.of(item));

        when(kotRepo.findById(500L)).thenReturn(Optional.of(kotJpa));
        when(itemRepo.findByKotId(500L)).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(kotMapper.toDomain(eq(kotJpa), any())).thenReturn(kot);

        Optional<KitchenOrderTicket> found = adapter.findById(500L);

        assertTrue(found.isPresent());
        assertEquals(500L, found.get().getId());
    }

    @Test
    @DisplayName("Should find KOTs by sales order id")
    void shouldFindBySalesOrderId() {
        KitchenOrderTicketJpa kotJpa = new KitchenOrderTicketJpa(500L, 100L, 5L, 20L, KotStatus.PENDING, ZonedDateTime.now(), 0L);
        KotItemJpa itemJpa = new KotItemJpa(1L, 500L, 10L, "Jollof Rice", 2, 0L);
        KotItem item = KotItem.create(1L, 500L, 10L, "Jollof Rice", 2);
        KitchenOrderTicket kot = KitchenOrderTicket.create(500L, 100L, 5L, 20L, List.of(item));

        when(kotRepo.findBySalesOrderId(100L)).thenReturn(List.of(kotJpa));
        when(itemRepo.findByKotId(500L)).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(kotMapper.toDomain(eq(kotJpa), any())).thenReturn(kot);

        List<KitchenOrderTicket> results = adapter.findBySalesOrderId(100L);

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should delete KOT and items by id")
    void shouldDeleteById() {
        adapter.deleteById(500L);

        verify(itemRepo).deleteByKotId(500L);
        verify(kotRepo).deleteById(500L);
    }
}
