package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.SalesOrderItemJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.SalesOrderJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.SalesOrderItemMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.SalesOrderMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataSalesOrderItemRepository;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataSalesOrderRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
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

class SalesOrderRepositoryAdapterTest {

    private SpringDataSalesOrderRepository orderRepo;
    private SpringDataSalesOrderItemRepository itemRepo;
    private SalesOrderMapper orderMapper;
    private SalesOrderItemMapper itemMapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private SalesOrderRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        orderRepo = mock(SpringDataSalesOrderRepository.class);
        itemRepo = mock(SpringDataSalesOrderItemRepository.class);
        orderMapper = mock(SalesOrderMapper.class);
        itemMapper = mock(SalesOrderItemMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new SalesOrderRepositoryAdapter(orderRepo, itemRepo, orderMapper, itemMapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for sales order sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_sales_order_seq")).thenReturn(100L);
        assertEquals(100L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should save sales order and items")
    void shouldSaveSalesOrderAndItems() {
        SalesOrderItem item = SalesOrderItem.create(
                1L, 100L, 10L, null, 1,
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN)
        );
        SalesOrder order = SalesOrder.create(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, PaymentMethod.CASH, List.of(item), null
        );

        SalesOrderJpa orderJpa = new SalesOrderJpa(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, OrderStatus.PENDING, null,
                new BigDecimal("5000.0000"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("5000.0000"),
                PaymentMethod.CASH, null, ZonedDateTime.now(), null, 0L
        );
        SalesOrderItemJpa itemJpa = new SalesOrderItemJpa(
                1L, 100L, 10L, null, 1,
                new BigDecimal("5000.0000"), new BigDecimal("5000.0000"), BigDecimal.ZERO, BigDecimal.ZERO, 0L
        );

        when(orderMapper.toPersistence(order)).thenReturn(orderJpa);
        when(orderRepo.save(orderJpa)).thenReturn(orderJpa);
        when(itemMapper.toPersistence(item)).thenReturn(itemJpa);
        when(itemRepo.saveAll(any())).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(orderMapper.toDomain(eq(orderJpa), any())).thenReturn(order);

        SalesOrder saved = adapter.save(order);

        assertNotNull(saved);
        verify(orderRepo).save(orderJpa);
        verify(itemRepo).deleteBySalesOrderId(100L);
        verify(itemRepo).saveAll(any());
    }

    @Test
    @DisplayName("Should find sales order by id with items")
    void shouldFindByIdWithItems() {
        SalesOrderJpa orderJpa = new SalesOrderJpa(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, OrderStatus.PENDING, null,
                new BigDecimal("5000.0000"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("5000.0000"),
                PaymentMethod.CASH, null, ZonedDateTime.now(), null, 0L
        );
        SalesOrderItemJpa itemJpa = new SalesOrderItemJpa(
                1L, 100L, 10L, null, 1,
                new BigDecimal("5000.0000"), new BigDecimal("5000.0000"), BigDecimal.ZERO, BigDecimal.ZERO, 0L
        );
        SalesOrderItem item = SalesOrderItem.create(
                1L, 100L, 10L, null, 1,
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN)
        );
        SalesOrder order = SalesOrder.create(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, PaymentMethod.CASH, List.of(item), null
        );

        when(orderRepo.findById(100L)).thenReturn(Optional.of(orderJpa));
        when(itemRepo.findBySalesOrderId(100L)).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(orderMapper.toDomain(eq(orderJpa), any())).thenReturn(order);

        Optional<SalesOrder> found = adapter.findById(100L);

        assertTrue(found.isPresent());
        assertEquals(100L, found.get().getId());
    }

    @Test
    @DisplayName("Should find sales order by charge reference")
    void shouldFindByChargeReference() {
        SalesOrderJpa orderJpa = new SalesOrderJpa(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, OrderStatus.PENDING, null,
                new BigDecimal("5000.0000"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("5000.0000"),
                PaymentMethod.CARD, "chg-ref-123", ZonedDateTime.now(), null, 0L
        );
        SalesOrder order = SalesOrder.create(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, PaymentMethod.CARD, List.of(), null
        );

        when(orderRepo.findByChargeReference("chg-ref-123")).thenReturn(Optional.of(orderJpa));
        when(itemRepo.findBySalesOrderId(100L)).thenReturn(List.of());
        when(orderMapper.toDomain(eq(orderJpa), any())).thenReturn(order);

        Optional<SalesOrder> found = adapter.findByChargeReference("chg-ref-123");

        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Should find transactions paged")
    void shouldFindTransactionsPaged() {
        SalesOrderJpa orderJpa = new SalesOrderJpa(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, OrderStatus.PENDING, null,
                new BigDecimal("5000.0000"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("5000.0000"),
                PaymentMethod.CASH, null, ZonedDateTime.now(), null, 0L
        );
        SalesOrder order = SalesOrder.create(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, PaymentMethod.CASH, List.of(), null
        );

        when(orderRepo.findTransactions(eq(20L), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(orderJpa), PageRequest.of(0, 10), 1));
        when(itemRepo.findBySalesOrderId(100L)).thenReturn(List.of());
        when(orderMapper.toDomain(eq(orderJpa), any())).thenReturn(order);

        PageResult<SalesOrder> result = adapter.findTransactions(20L, null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    @DisplayName("Should delete sales order and items by id")
    void shouldDeleteById() {
        adapter.deleteById(100L);

        verify(itemRepo).deleteBySalesOrderId(100L);
        verify(orderRepo).deleteById(100L);
    }
}
