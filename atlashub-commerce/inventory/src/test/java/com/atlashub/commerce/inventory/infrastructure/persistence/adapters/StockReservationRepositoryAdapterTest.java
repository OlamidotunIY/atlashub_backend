package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.StockReservation;
import com.atlashub.commerce.inventory.domain.entities.StockReservationItem;
import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockReservationItemJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockReservationJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockReservationItemMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockReservationMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockReservationItemRepository;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockReservationRepository;
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

class StockReservationRepositoryAdapterTest {

    private SpringDataStockReservationRepository resRepo;
    private SpringDataStockReservationItemRepository itemRepo;
    private StockReservationMapper resMapper;
    private StockReservationItemMapper itemMapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private StockReservationRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        resRepo = mock(SpringDataStockReservationRepository.class);
        itemRepo = mock(SpringDataStockReservationItemRepository.class);
        resMapper = mock(StockReservationMapper.class);
        itemMapper = mock(StockReservationItemMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new StockReservationRepositoryAdapter(resRepo, itemRepo, resMapper, itemMapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for stock reservation sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_stock_reservation_seq")).thenReturn(55L);
        assertEquals(55L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should save stock reservation and items")
    void shouldSaveStockReservationAndItems() {
        StockReservationItem item = StockReservationItem.create(1L, 10L, 100L, 2);
        StockReservation reservation = StockReservation.create(10L, 20L, 30L, 40L, List.of(item));

        StockReservationJpa jpa = new StockReservationJpa(10L, 20L, 30L, 40L, ReservationStatus.ACTIVE, null, ZonedDateTime.now(), ZonedDateTime.now(), 0L);
        StockReservationItemJpa itemJpa = new StockReservationItemJpa(1L, 10L, 100L, 2);

        when(resMapper.toPersistence(reservation)).thenReturn(jpa);
        when(resRepo.save(jpa)).thenReturn(jpa);
        when(itemMapper.toPersistence(item)).thenReturn(itemJpa);
        when(itemRepo.saveAll(any())).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(resMapper.toDomain(eq(jpa), any())).thenReturn(reservation);

        StockReservation saved = adapter.save(reservation);

        assertNotNull(saved);
        verify(resRepo).save(jpa);
        verify(itemRepo).deleteByReservationId(10L);
        verify(itemRepo).saveAll(any());
    }

    @Test
    @DisplayName("Should find stock reservation by id with items")
    void shouldFindStockReservationByIdWithItems() {
        StockReservationJpa jpa = new StockReservationJpa(10L, 20L, 30L, 40L, ReservationStatus.ACTIVE, null, ZonedDateTime.now(), ZonedDateTime.now(), 0L);
        StockReservationItemJpa itemJpa = new StockReservationItemJpa(1L, 10L, 100L, 2);
        StockReservationItem item = StockReservationItem.create(1L, 10L, 100L, 2);
        StockReservation reservation = StockReservation.create(10L, 20L, 30L, 40L, List.of(item));

        when(resRepo.findById(10L)).thenReturn(Optional.of(jpa));
        when(itemRepo.findByReservationId(10L)).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(resMapper.toDomain(eq(jpa), any())).thenReturn(reservation);

        Optional<StockReservation> found = adapter.findById(10L);

        assertTrue(found.isPresent());
        assertEquals(10L, found.get().getId());
    }

    @Test
    @DisplayName("Should find stock reservation by sales order id")
    void shouldFindBySalesOrderId() {
        StockReservationJpa jpa = new StockReservationJpa(10L, 20L, 30L, 40L, ReservationStatus.ACTIVE, null, ZonedDateTime.now(), ZonedDateTime.now(), 0L);
        StockReservationItemJpa itemJpa = new StockReservationItemJpa(1L, 10L, 100L, 2);
        StockReservationItem item = StockReservationItem.create(1L, 10L, 100L, 2);
        StockReservation reservation = StockReservation.create(10L, 20L, 30L, 40L, List.of(item));

        when(resRepo.findBySalesOrderId(20L)).thenReturn(Optional.of(jpa));
        when(itemRepo.findByReservationId(10L)).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(resMapper.toDomain(eq(jpa), any())).thenReturn(reservation);

        Optional<StockReservation> found = adapter.findBySalesOrderId(20L);

        assertTrue(found.isPresent());
        assertEquals(20L, found.get().getSalesOrderId());
    }

    @Test
    @DisplayName("Should find stock reservations by organization id and status")
    void shouldFindByOrganizationIdAndStatus() {
        StockReservationJpa jpa = new StockReservationJpa(10L, 20L, 30L, 40L, ReservationStatus.ACTIVE, null, ZonedDateTime.now(), ZonedDateTime.now(), 0L);
        StockReservationItemJpa itemJpa = new StockReservationItemJpa(1L, 10L, 100L, 2);
        StockReservationItem item = StockReservationItem.create(1L, 10L, 100L, 2);
        StockReservation reservation = StockReservation.create(10L, 20L, 30L, 40L, List.of(item));

        when(resRepo.findByOrganizationIdAndStatus(30L, ReservationStatus.ACTIVE)).thenReturn(List.of(jpa));
        when(itemRepo.findByReservationId(10L)).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(resMapper.toDomain(eq(jpa), any())).thenReturn(reservation);

        List<StockReservation> results = adapter.findByOrganizationIdAndStatus(30L, ReservationStatus.ACTIVE);

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should delete stock reservation and items by id")
    void shouldDeleteStockReservationAndItemsById() {
        adapter.deleteById(10L);

        verify(itemRepo).deleteByReservationId(10L);
        verify(resRepo).deleteById(10L);
    }
}
