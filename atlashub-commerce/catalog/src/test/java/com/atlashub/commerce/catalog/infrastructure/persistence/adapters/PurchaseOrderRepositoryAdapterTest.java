package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.entities.PurchaseOrder;
import com.atlashub.commerce.catalog.domain.entities.PurchaseOrderItem;
import com.atlashub.commerce.catalog.domain.valueobject.PurchaseOrderStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.PurchaseOrderItemJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.PurchaseOrderJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.PurchaseOrderItemMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.PurchaseOrderMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataPurchaseOrderItemRepository;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataPurchaseOrderRepository;
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

class PurchaseOrderRepositoryAdapterTest {

    private SpringDataPurchaseOrderRepository poRepo;
    private SpringDataPurchaseOrderItemRepository itemRepo;
    private PurchaseOrderMapper poMapper;
    private PurchaseOrderItemMapper itemMapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private PurchaseOrderRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        poRepo = mock(SpringDataPurchaseOrderRepository.class);
        itemRepo = mock(SpringDataPurchaseOrderItemRepository.class);
        poMapper = mock(PurchaseOrderMapper.class);
        itemMapper = mock(PurchaseOrderItemMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new PurchaseOrderRepositoryAdapter(poRepo, itemRepo, poMapper, itemMapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return nextIdentity using sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_purchase_order_seq")).thenReturn(77L);
        assertEquals(77L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should save purchase order and its items")
    void shouldSavePurchaseOrderAndItems() {
        PurchaseOrder domain = mock(PurchaseOrder.class);
        PurchaseOrderItem item = mock(PurchaseOrderItem.class);
        when(domain.getItems()).thenReturn(List.of(item));

        PurchaseOrderJpa poJpa = mock(PurchaseOrderJpa.class);
        when(poJpa.getId()).thenReturn(1L);
        when(poMapper.toPersistence(domain)).thenReturn(poJpa);
        when(poRepo.save(poJpa)).thenReturn(poJpa);

        PurchaseOrderItemJpa itemJpa = mock(PurchaseOrderItemJpa.class);
        when(itemMapper.toPersistence(item)).thenReturn(itemJpa);
        when(itemRepo.saveAll(any())).thenReturn(List.of(itemJpa));

        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(poMapper.toDomain(eq(poJpa), any())).thenReturn(domain);

        PurchaseOrder result = adapter.save(domain);

        assertNotNull(result);
        verify(poRepo).save(poJpa);
        verify(itemRepo).deleteByPurchaseOrderId(1L);
        verify(itemRepo).saveAll(any());
    }

    @Test
    @DisplayName("Should find purchase order by id with items")
    void shouldFindById() {
        PurchaseOrderJpa poJpa = mock(PurchaseOrderJpa.class);
        PurchaseOrderItemJpa itemJpa = mock(PurchaseOrderItemJpa.class);
        PurchaseOrder domain = mock(PurchaseOrder.class);
        PurchaseOrderItem item = mock(PurchaseOrderItem.class);

        when(poRepo.findById(1L)).thenReturn(Optional.of(poJpa));
        when(itemRepo.findByPurchaseOrderId(1L)).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(poMapper.toDomain(eq(poJpa), eq(List.of(item)))).thenReturn(domain);

        Optional<PurchaseOrder> result = adapter.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should delete purchase order and items by id")
    void shouldDeleteById() {
        adapter.deleteById(1L);
        verify(itemRepo).deleteByPurchaseOrderId(1L);
        verify(poRepo).deleteById(1L);
    }

    @Test
    @DisplayName("Should return paginated purchase orders with items")
    void shouldReturnPaginatedPurchaseOrders() {
        PurchaseOrderJpa poJpa = mock(PurchaseOrderJpa.class);
        when(poJpa.getId()).thenReturn(1L);
        PurchaseOrderItemJpa itemJpa = mock(PurchaseOrderItemJpa.class);
        when(itemJpa.getPurchaseOrderId()).thenReturn(1L);

        PurchaseOrder domain = mock(PurchaseOrder.class);
        PurchaseOrderItem item = mock(PurchaseOrderItem.class);

        Page<PurchaseOrderJpa> jpaPage = new PageImpl<>(List.of(poJpa), PageRequest.of(0, 10), 1);
        when(poRepo.findByOrganizationIdAndStatus(eq(10L), eq(PurchaseOrderStatus.SENT), any())).thenReturn(jpaPage);
        when(itemRepo.findByPurchaseOrderIdIn(List.of(1L))).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(poMapper.toDomain(eq(poJpa), any())).thenReturn(domain);

        PageResult<PurchaseOrder> page = adapter.findByOrganizationId(10L, PurchaseOrderStatus.SENT, 0, 10);

        assertNotNull(page);
        assertEquals(1, page.content().size());
        assertEquals(1, page.totalElements());
    }
}
