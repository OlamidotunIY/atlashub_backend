package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.CustomerReturn;
import com.atlashub.commerce.inventory.domain.entities.ReturnItem;
import com.atlashub.commerce.inventory.domain.valueobject.ReturnStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.CustomerReturnJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.ReturnItemJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.CustomerReturnMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.ReturnItemMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataCustomerReturnRepository;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataReturnItemRepository;
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

class CustomerReturnRepositoryAdapterTest {

    private SpringDataCustomerReturnRepository returnRepo;
    private SpringDataReturnItemRepository itemRepo;
    private CustomerReturnMapper returnMapper;
    private ReturnItemMapper itemMapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private CustomerReturnRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        returnRepo = mock(SpringDataCustomerReturnRepository.class);
        itemRepo = mock(SpringDataReturnItemRepository.class);
        returnMapper = mock(CustomerReturnMapper.class);
        itemMapper = mock(ReturnItemMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new CustomerReturnRepositoryAdapter(returnRepo, itemRepo, returnMapper, itemMapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for customer return sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_customer_return_seq")).thenReturn(88L);
        assertEquals(88L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should save customer return and items")
    void shouldSaveCustomerReturnAndItems() {
        CustomerReturn domain = mock(CustomerReturn.class);
        ReturnItem item = mock(ReturnItem.class);
        when(domain.getItems()).thenReturn(List.of(item));

        CustomerReturnJpa returnJpa = mock(CustomerReturnJpa.class);
        when(returnJpa.getId()).thenReturn(1L);
        when(returnMapper.toPersistence(domain)).thenReturn(returnJpa);
        when(returnRepo.save(returnJpa)).thenReturn(returnJpa);

        ReturnItemJpa itemJpa = mock(ReturnItemJpa.class);
        when(itemMapper.toPersistence(item)).thenReturn(itemJpa);
        when(itemRepo.saveAll(any())).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(returnMapper.toDomain(eq(returnJpa), any())).thenReturn(domain);

        CustomerReturn result = adapter.save(domain);
        assertNotNull(result);
        verify(returnRepo).save(returnJpa);
        verify(itemRepo).deleteByReturnId(1L);
        verify(itemRepo).saveAll(any());
    }

    @Test
    @DisplayName("Should find customer return by id with items")
    void shouldFindById() {
        CustomerReturnJpa returnJpa = mock(CustomerReturnJpa.class);
        ReturnItemJpa itemJpa = mock(ReturnItemJpa.class);
        CustomerReturn domain = mock(CustomerReturn.class);
        ReturnItem item = mock(ReturnItem.class);

        when(returnRepo.findById(1L)).thenReturn(Optional.of(returnJpa));
        when(itemRepo.findByReturnId(1L)).thenReturn(List.of(itemJpa));
        when(itemMapper.toDomain(itemJpa)).thenReturn(item);
        when(returnMapper.toDomain(eq(returnJpa), eq(List.of(item)))).thenReturn(domain);

        Optional<CustomerReturn> result = adapter.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should delete customer return and items by id")
    void shouldDeleteById() {
        adapter.deleteById(1L);
        verify(itemRepo).deleteByReturnId(1L);
        verify(returnRepo).deleteById(1L);
    }

    @Test
    @DisplayName("Should find customer return by sales order id")
    void shouldFindBySalesOrderId() {
        CustomerReturnJpa returnJpa = mock(CustomerReturnJpa.class);
        when(returnJpa.getId()).thenReturn(1L);
        CustomerReturn domain = mock(CustomerReturn.class);

        when(returnRepo.findByOrganizationIdAndSalesOrderId(10L, 100L))
                .thenReturn(Optional.of(returnJpa));
        when(itemRepo.findByReturnId(1L)).thenReturn(List.of());
        when(returnMapper.toDomain(eq(returnJpa), any())).thenReturn(domain);

        Optional<CustomerReturn> result = adapter.findByOrganizationIdAndSalesOrderId(10L, 100L);
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should find customer returns by status")
    void shouldFindByStatus() {
        CustomerReturnJpa returnJpa = mock(CustomerReturnJpa.class);
        when(returnJpa.getId()).thenReturn(1L);
        CustomerReturn domain = mock(CustomerReturn.class);

        when(returnRepo.findByOrganizationIdAndOutletIdAndStatus(10L, 20L, ReturnStatus.PENDING))
                .thenReturn(List.of(returnJpa));
        when(itemRepo.findByReturnIdIn(List.of(1L))).thenReturn(List.of());
        when(returnMapper.toDomain(eq(returnJpa), any())).thenReturn(domain);

        List<CustomerReturn> list = adapter.findByOrganizationIdAndOutletIdAndStatus(10L, 20L, ReturnStatus.PENDING);
        assertEquals(1, list.size());
        assertEquals(domain, list.get(0));
    }
}
